/* https://dev.to/mcadariu/postgres-column-tetris-neatly-packing-your-tables-for-fun-and-profit-1j6g */

create table groups
(
    id          bigint                      not null,
    "createdAt" timestamp(6) with time zone not null,
    "updatedAt" timestamp(6) with time zone not null,
    version     integer                     not null,
    name        varchar(100)                not null,
    primary key (id),
    unique (name)
);

create table users
(
    id                   bigint                      not null,
    group_id             bigint,
    "createdAt"          timestamp(6) with time zone not null,
    "updatedAt"          timestamp(6) with time zone not null,
    birthday             date                        not null,
    version              integer                     not null,
    gender               smallint                    not null,
    "accountExpired"     boolean                     not null,
    "accountLocked"      boolean                     not null,
    "credentialsExpired" boolean                     not null,
    enabled              boolean                     not null,
    "firstName"          varchar(100)                not null,
    "lastName"           varchar(100)                not null,
    email                varchar(100)                not null,
    "phoneNumber"        varchar(100)                not null,
    password             varchar(255)                not null,
    avatar               oid,
    primary key (id),
    unique (email),
    constraint "FK8ub3jca01mi0q1srp2v207jjt"
        foreign key (group_id) references groups,
    constraint users_gender_check
        check ((gender >= 0) AND (gender <= 1))
);

create index users_group_id on users (group_id);

create table account_roles
(
    role    smallint not null,
    user_id bigint   not null,
    primary key (user_id, role),
    constraint "FKe7rym0ohpwj0flvpyd6t1vgl"
        foreign key (user_id) references users,
    constraint account_roles_role_check
        check ((role >= 0) AND (role <= 1))
);
