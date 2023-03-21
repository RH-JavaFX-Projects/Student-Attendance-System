CREATE TABLE IF NOT EXISTS User
(
    username  varchar(50) primary key,
    password  varchar(100) not null,
    full_name varchar(100) not null
);


CREATE TABLE IF NOT EXISTS Student (
    id varchar(20) primary key ,
    name varchar(100) not null

);

CREATE TABLE IF NOT EXISTS Picture
(
    student_id varchar(20) primary key,
    picture    mediumblob not null,
    foreign key (student_id) references Student (id)
);


CREATE TABLE IF NOT EXISTS Attendance
(
    id         int primary key auto_increment,
    status     enum ('IN','OUT') not null,
    stamp      datetime          not null,
    student_id varchar(20) not null ,
    foreign key (student_id) references Student (id)
);

