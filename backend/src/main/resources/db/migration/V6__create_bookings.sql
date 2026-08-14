alter table courts
  add column hourly_price numeric(12, 2) not null default 3000.00;

alter table courts
  alter column hourly_price drop default;

create table bookings (
  id uuid primary key,
  player_id uuid not null,
  court_id uuid not null,
  start_at timestamp(6) with time zone not null,
  end_at timestamp(6) with time zone not null,
  status varchar(30) not null,
  total_price numeric(12, 2) not null,
  cancelled_at timestamp(6) with time zone,
  created_at timestamp(6) with time zone not null,
  updated_at timestamp(6) with time zone not null,
  constraint fk_bookings_player_id foreign key (player_id) references users (id),
  constraint fk_bookings_court_id foreign key (court_id) references courts (id),
  constraint ck_bookings_period check (start_at < end_at),
  constraint ck_bookings_status check (status in ('CONFIRMED', 'COMPLETED', 'CANCELLED')),
  constraint ck_bookings_total_price check (total_price >= 0)
);

create index idx_bookings_court_period on bookings (court_id, start_at, end_at);
create index idx_bookings_player_start on bookings (player_id, start_at);
create index idx_bookings_status on bookings (status);
