import postgres from "npm:postgres@3.4.7";

type PageConfig = { page_id: string; access_token: string; page_name?: string };
type FacebookAttachment = {
  type?: string;
  url?: string;
  media?: { image?: { src?: string } };
  subattachments?: { data?: FacebookAttachment[] };
};
type FacebookPost = {
  id?: string;
  message?: string;
  story?: string;
  created_time?: string;
  permalink_url?: string;
  full_picture?: string;
  from?: { id?: string; name?: string };
  attachments?: { data?: FacebookAttachment[] };
};

function pageConfigs(): PageConfig[] {
  const json = Deno.env.get("FACEBOOK_PAGES_JSON") || "";
  if (json) {
    try {
      const parsed = JSON.parse(json);
      if (Array.isArray(parsed)) return parsed.filter((p) => p?.page_id && p?.access_token);
    } catch {
      throw new Error("invalid_facebook_pages_json");
    }
  }
  const page_id = Deno.env.get("FACEBOOK_PAGE_ID") || "";
  const access_token = Deno.env.get("FACEBOOK_PAGE_ACCESS_TOKEN") || "";
  return page_id && access_token ? [{ page_id, access_token }] : [];
}

function mediaFor(post: FacebookPost) {
  const first = post.attachments?.data?.[0];
  const type = String(first?.type || "").toLowerCase();
  const childCount = first?.subattachments?.data?.length || 0;
  const preview = first?.media?.image?.src || post.full_picture || null;
  if (childCount > 1 || type.includes("album") || type.includes("multi_share")) {
    return { media_type: "carousel", media_url: preview, thumbnail_url: preview };
  }
  if (type.includes("video")) return { media_type: "video", media_url: null, thumbnail_url: preview };
  if (type.includes("photo") || type.includes("image")) return { media_type: "image", media_url: preview, thumbnail_url: preview };
  if (first?.url || type.includes("share") || type.includes("link")) return { media_type: "link", media_url: preview, thumbnail_url: preview };
  return { media_type: "text", media_url: null, thumbnail_url: null };
}

export async function syncFacebookPages(options: { pageId?: string; force?: boolean } = {}) {
  const databaseUrl = Deno.env.get("SUPABASE_DB_URL");
  if (!databaseUrl) throw new Error("missing_supabase_db_url");
  const configured = pageConfigs();
  if (!configured.length) throw new Error("facebook_not_configured");
  const pages = options.pageId ? configured.filter((p) => p.page_id === options.pageId) : configured;
  if (!pages.length) return { imported: 0, ignored: "page_not_configured" };

  const requestedVersion = Deno.env.get("FACEBOOK_GRAPH_VERSION") || "v23.0";
  const graphVersion = /^v\d+\.\d+$/.test(requestedVersion) ? requestedVersion : "v23.0";
  const sql = postgres(databaseUrl, { prepare: false, max: 1 });
  let imported = 0;
  try {
    for (const page of pages) {
      const [lock] = await sql`select pg_try_advisory_lock(hashtext(${`vibes-facebook-${page.page_id}`})) as acquired`;
      if (!lock?.acquired) continue;
      if (!options.force) {
        const [latest] = await sql`select max(imported_at) as imported_at from public.external_posts where platform='facebook' and source_id=${page.page_id}`;
        if (latest?.imported_at && Date.now() - new Date(latest.imported_at).getTime() < 120000) continue;
      }

      const base = `https://graph.facebook.com/${graphVersion}`;
      const pageUrl = new URL(`${base}/${encodeURIComponent(page.page_id)}`);
      pageUrl.searchParams.set("fields", "id,name,username");
      pageUrl.searchParams.set("access_token", page.access_token);
      const feedUrl = new URL(`${base}/${encodeURIComponent(page.page_id)}/feed`);
      feedUrl.searchParams.set("fields", "id,message,story,created_time,permalink_url,full_picture,from{id,name},attachments{media,type,url,subattachments.limit(10){media,type,url}}");
      feedUrl.searchParams.set("limit", "50");
      feedUrl.searchParams.set("access_token", page.access_token);

      const [pageResponse, feedResponse] = await Promise.all([
        fetch(pageUrl, { headers: { accept: "application/json" } }),
        fetch(feedUrl, { headers: { accept: "application/json" } }),
      ]);
      const pageBody = await pageResponse.json().catch(() => ({}));
      const feedBody = await feedResponse.json().catch(() => ({}));
      if (!feedResponse.ok) {
        console.error("Facebook API error", feedResponse.status, feedBody?.error?.type || "unknown");
        throw new Error("facebook_request_failed");
      }

      const rows = (Array.isArray(feedBody.data) ? feedBody.data : [])
        .filter((post: FacebookPost) => post.id && post.created_time)
        .map((post: FacebookPost) => {
          const media = mediaFor(post);
          const creatorName = post.from?.name || page.page_name || pageBody?.name || "Facebook Page";
          return {
            platform: "facebook",
            source_id: page.page_id,
            external_id: post.id!,
            creator_name: creatorName,
            creator_handle: pageBody?.username ? `@${pageBody.username}` : "",
            caption: post.message || post.story || "",
            media_type: media.media_type,
            media_url: media.media_url,
            thumbnail_url: media.thumbnail_url,
            permalink: post.permalink_url || `https://www.facebook.com/${post.id}`,
            published_at: post.created_time!,
            is_available: true,
            imported_at: new Date().toISOString(),
          };
        });

      if (rows.length) {
        await sql`
          insert into public.external_posts ${sql(rows,
            "platform","source_id","external_id","creator_name","creator_handle","caption",
            "media_type","media_url","thumbnail_url","permalink","published_at","is_available","imported_at"
          )}
          on conflict (platform,external_id) do update set
            source_id=excluded.source_id,
            creator_name=excluded.creator_name,
            creator_handle=excluded.creator_handle,
            caption=excluded.caption,
            media_type=excluded.media_type,
            media_url=excluded.media_url,
            thumbnail_url=excluded.thumbnail_url,
            permalink=excluded.permalink,
            published_at=excluded.published_at,
            is_available=excluded.is_available,
            imported_at=excluded.imported_at
        `;
        imported += rows.length;
      }
    }
    return { imported };
  } finally {
    await sql.end();
  }
}
