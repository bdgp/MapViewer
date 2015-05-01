create table somstruct (
	id int primary key not null,
	x float,
	y float,
	name varchar(64),
	somtitle_id int
);

create table somtitle (
	id int primary key not null auto_increment,
	name varchar(256),
	source_type varchar(64)
);

create table somoverlay (
	somstruct_id int,
	value float,
	somoverlay_info_id int
);

create table somoverlay_info (
	id int primary key not null auto_increment,
	name varchar(256),
	variant int,
	variant_name varchar(32),
	color varchar(16),
	type varchar(64),
	decorator varchar(64),
	somtitle_id int
);

create table sominfo_short (
	somstruct_id int,
	html text,
	variant int
);

create table sominfo_long (
	somstruct_id int,
	html text
);
