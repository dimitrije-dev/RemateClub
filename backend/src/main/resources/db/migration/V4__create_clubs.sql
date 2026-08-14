create table clubs (
  id uuid primary key,
  owner_id uuid not null,
  name varchar(160) not null,
  city varchar(120) not null,
  status varchar(30) not null,
  created_at timestamp(6) with time zone not null,
  updated_at timestamp(6) with time zone not null,
  constraint fk_clubs_owner_id foreign key (owner_id) references users (id),
  constraint ck_clubs_status check (status in ('PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'SUSPENDED'))
);

create index idx_clubs_owner_id on clubs (owner_id);
create index idx_clubs_city on clubs (city);
create index idx_clubs_status on clubs (status);
