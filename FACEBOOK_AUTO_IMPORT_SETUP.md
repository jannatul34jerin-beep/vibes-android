# Authorized Facebook Page auto-import

Vibes imports only Pages that an administrator explicitly authorizes. It does not scrape, import personal profiles or bypass Meta review.

## Supabase secrets

Set these under Edge Functions secrets:

- `FACEBOOK_PAGES_JSON`: JSON array such as `[{"page_id":"PAGE_ID","access_token":"PAGE_ACCESS_TOKEN","page_name":"Page name"}]`
- `FACEBOOK_APP_SECRET`: Meta app secret used only to verify signed webhook requests
- `FACEBOOK_WEBHOOK_VERIFY_TOKEN`: a long random value you create
- `FACEBOOK_GRAPH_VERSION`: optional version such as `v23.0`

For one Page, `FACEBOOK_PAGE_ID` and `FACEBOOK_PAGE_ACCESS_TOKEN` may be used instead of the JSON array. Never put any of these values in the Android app or Git.

## Meta dashboard

1. Use the Meta app that owns the authorized Page integration.
2. Add the Facebook Page Webhooks product and set the callback URL to:
   `https://darafsrmyyslbvhpgfoo.supabase.co/functions/v1/facebook-page-webhook`
3. Enter the exact `FACEBOOK_WEBHOOK_VERIFY_TOKEN`.
4. Subscribe the Page to the `feed` field and complete the Page access-token connection.
5. Request only the Page permissions needed for the selected use case. Depending on Meta's current API/review rules this can include Page listing/read permissions and may require App Review or Business Verification.
6. Publish a new Page post and confirm a signed webhook produces a row in `public.external_posts`.

The app also invokes the authenticated `sync-facebook-posts` function after login as a rate-limited fallback. Webhooks provide near-realtime refresh; delivery is not guaranteed to be instantaneous during provider outages or token expiry.
