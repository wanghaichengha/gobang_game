create database if not exists gobang_game;

use gobang_game;

drop tables if exists user;
create table user (
    userId int primary key auto_increment,
    username varchar(50) unique,
    password varchar(50),
    score int,
    totalCount int,
    winCount int
);
insert into user values(null,'zhangsan','123',1000,0,0);
insert into user values(null,'lisi','123',1000,0,0);
insert into user values(null,'wangwu','123',1000,0,0);