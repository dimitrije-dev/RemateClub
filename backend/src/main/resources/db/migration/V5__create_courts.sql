create table courts (
  id uuid primary key,
  club_id uuid not null,
  name varchar(120) not null,
  type varchar(30) not null,
  active boolean not null,
  created_at timestamp(6) with time zone not null,
  updated_at timestamp(6) with time zone not null,
  constraint fk_courts_club_id foreign key (club_id) references clubs (id),
  constraint ck_courts_type check (type in ('STANDARD', 'PANORAMIC', 'SINGLE'))
);

create index idx_courts_club_id on courts (club_id);
create index idx_courts_club_active on courts (club_id, active);
create index idx_courts_type on courts (type);
