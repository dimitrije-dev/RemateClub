create table users (
  id uuid primary key,
  email varchar(320) not null,
  password_hash varchar(100) not null,
  first_name varchar(80) not null,
  last_name varchar(80) not null,
  role varchar(30) not null,
  status varchar(30) not null,
  created_at timestamp(6) with time zone not null,
  updated_at timestamp(6) with time zone not null,
  constraint uk_users_email unique (email),
  constraint ck_users_role check (role in ('PLAYER', 'OWNER', 'ADMIN')),
  constraint ck_users_status check (status in ('ACTIVE', 'SUSPENDED', 'DISABLED'))
);

create index idx_users_email on users (email);
create index idx_users_role on users (role);
create index idx_users_status on users (status);

alter table refresh_tokens
  add constraint fk_refresh_tokens_user_id
  foreign key (user_id) references users (id);
