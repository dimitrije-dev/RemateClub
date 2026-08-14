create table court_blocks (
  id uuid primary key,
  court_id uuid not null,
  start_at timestamp(6) with time zone not null,
  end_at timestamp(6) with time zone not null,
  reason varchar(300),
  created_at timestamp(6) with time zone not null,
  constraint fk_court_blocks_court_id foreign key (court_id) references courts (id),
  constraint ck_court_blocks_period check (start_at < end_at)
);

create index idx_court_blocks_court_period on court_blocks (court_id, start_at, end_at);
