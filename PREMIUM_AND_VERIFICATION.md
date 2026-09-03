# Premium and account verification

Vibes models two separate concepts:

- **Identity reviewed**: a trust decision after a human-reviewed authenticity request and an appeal path.
- **Premium**: an optional membership entitlement with an optional Premium badge and an ad-free product promise.

Payment never grants the identity-reviewed badge. A client cannot write verification_status, account_status, premium_status or Premium entitlement rows. Those fields are reserved for a trusted moderation or billing backend.

## Before accepting payments

1. Create a subscription product in Google Play Console or a supported payment provider.
2. Verify every purchase, renewal and cancellation on a trusted server using the provider API.
3. Write the resulting entitlement using the Supabase service role only.
4. Configure provider notifications or webhooks and retry/idempotency handling.
5. Publish price, renewal, cancellation, refund and privacy disclosures.
6. Test trial, renewal, cancellation, refund, grace-period, account deletion and device-change cases.

The included app deliberately shows setup-required messaging instead of a fake or client-trusted checkout.

## Authenticity rules

- A missing profile photo is allowed.
- A report alone never disables an account.
- Moderators can move suspicious accounts to under_review or disabled; restrictive RLS then blocks social data access immediately.
- Users can submit a review or appeal request.
- No hidden face recognition or biometric upload is implemented.
- If liveness verification is added later, require explicit consent, a disclosed specialist provider, strict retention/deletion rules, security review and a human appeal.
