create table refresh_tokens (
  id uuid primary key,
  user_id uuid not null,
  token_hash varchar(128) not null,
  expires_at timestamp(6) with time zone not null,
  created_at timestamp(6) with time zone not null,
  revoked_at timestamp(6) with time zone,
  replaced_by_token_id uuid,
  constraint uk_refresh_tokens_token_hash unique (token_hash)
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
create index idx_refresh_tokens_expires_at on refresh_tokens (expires_at);
create index idx_refresh_tokens_revoked_at on refresh_tokens (revoked_at);
