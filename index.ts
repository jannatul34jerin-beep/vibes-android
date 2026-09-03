import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import postgres from "npm:postgres@3.4.7";

type InstagramMedia = {
  id: string;
  caption?: string;
  media_type?: "IMAGE" | "VIDEO" | "CAROUSEL_ALBUM";
  media_url?: string;
  permalink?: string;
  thumbnail_url?: string;
  timestamp?: string;
  username?: string;
};

const json = (body: unknown, status = 200) => new Response(JSON.stringify(body), {
  status,
  headers: {
    "content-type": "application/json; charset=utf-8",
    "cache-control": "no-store",
    "access-control-allow-origin": "*",
    "access-control-allow-headers": "authorization, x-client-info, apikey, content-type",
  },
});

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") return json({ ok: true });
  if (req.method !== "POST") return json({ error: "method_not_allowed" }, 405);

  const accessToken = Deno.env.get("INSTAGRAM_ACCESS_TOKEN");
  const databaseUrl = Deno.env.get("SUPABASE_DB_URL");
  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const publishableKeys = JSON.parse(Deno.env.get("SUPABASE_PUBLISHABLE_KEYS") || "{}");
  const publishableKey = publishableKeys.default || Deno.env.get("SUPABASE_ANON_KEY");
  const missing = [
    !accessToken ? "INSTAGRAM_ACCESS_TOKEN" : null,
    !databaseUrl ? "SUPABASE_DB_URL" : null,
    !supabaseUrl ? "SUPABASE_URL" : null,
    !publishableKey ? "SUPABASE_PUBLISHABLE_KEYS" : null,
  ].filter(Boolean);
  if (missing.length) return json({ error: "server_not_configured", missing }, 500);

  const authorization = req.headers.get("authorization") || "";
  if (!authorization.toLowerCase().startsWith("bearer ")) return json({ error: "authentication_required" }, 401);
  const userResponse = await fetch(`${supabaseUrl}/auth/v1/user`, {
    headers: { authorization, apikey: publishableKey },
  });
  if (!userResponse.ok) return json({ error: "authentication_required" }, 401);
  const user = await userResponse.json();
  if (!user?.id) return json({ error: "authentication_required" }, 401);

  const sql = postgres(databaseUrl!, { prepare: false, max: 1 });
  try {
    const [lock] = await sql`select pg_try_advisory_lock(98151616) as acquired`;
    if (!lock?.acquired) return json({ imported: 0, skipped: "sync_in_progress" });
    const [latest] = await sql`select max(imported_at) as imported_at from public.external_posts where platform = 'instagram'`;
    if (latest?.imported_at && Date.now() - new Date(latest.imported_at).getTime() < 300000) {
      return json({ imported: 0, skipped: "recently_synced" });
    }

    const fields = "id,caption,media_type,media_url,permalink,thumbnail_url,timestamp,username";
    const endpoint = new URL("https://graph.instagram.com/me/media");
    endpoint.searchParams.set("fields", fields);
    endpoint.searchParams.set("limit", "50");
    endpoint.searchParams.set("access_token", accessToken!);

    const platformResponse = await fetch(endpoint, { headers: { accept: "application/json" } });
    const platformBody = await platformResponse.json();
    if (!platformResponse.ok) {
      console.error("Instagram API error", platformResponse.status, platformBody?.error?.type || "unknown");
      return json({ error: "instagram_request_failed", status: platformResponse.status }, 502);
    }

    const media: InstagramMedia[] = Array.isArray(platformBody.data) ? platformBody.data : [];
    const rows = media
      .filter((item) => item.id && item.permalink && item.timestamp)
      .map((item) => ({
        platform: "instagram",
        external_id: item.id,
        creator_name: item.username || "Instagram creator",
        creator_handle: item.username ? `@${item.username}` : "",
        caption: item.caption || "",
        media_type: item.media_type === "VIDEO" ? "video" : item.media_type === "CAROUSEL_ALBUM" ? "carousel" : "image",
        media_url: item.media_url || null,
        thumbnail_url: item.thumbnail_url || null,
        permalink: item.permalink,
        published_at: item.timestamp,
        is_available: true,
        imported_at: new Date().toISOString(),
      }));

    if (!rows.length) return json({ imported: 0 });
    await sql`
      insert into public.external_posts ${sql(rows,
        "platform",
        "external_id",
        "creator_name",
        "creator_handle",
        "caption",
        "media_type",
        "media_url",
        "thumbnail_url",
        "permalink",
        "published_at",
        "is_available",
        "imported_at"
      )}
      on conflict (platform, external_id) do update set
        creator_name = excluded.creator_name,
        creator_handle = excluded.creator_handle,
        caption = excluded.caption,
        media_type = excluded.media_type,
        media_url = excluded.media_url,
        thumbnail_url = excluded.thumbnail_url,
        permalink = excluded.permalink,
        published_at = excluded.published_at,
        is_available = excluded.is_available,
        imported_at = excluded.imported_at
    `;
    return json({ imported: rows.length });
  } catch (error) {
    console.error("Database upsert failed", error instanceof Error ? error.name : "unknown");
    return json({ error: "database_write_failed" }, 500);
  } finally {
    await sql.end();
  }
});
