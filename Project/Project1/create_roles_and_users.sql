create table project1_final.s1 (
    number int primary key ,
    student_id text references project1_final.student(student_id),
    course_id text
);
create table project1_final.s2 (
    number int primary key ,
    student_id text references project1_final.student(student_id),
    course_id text
);
create table project1_final.s3 (
    number int primary key ,
    student_id text references project1_final.student(student_id),
    course_id text
);

insert into project1_final.s1 values ('1', '11099967', 'FMA302');
insert into project1_final.s2 values ('1', '11099966', 'CS307');
insert into project1_final.s2 values ('2', '11099966', 'EE332');
insert into project1_final.s3 values ('1', '11099963', 'CS322');
insert into project1_final.s3 values ('2', '11099963', 'OCE408');
insert into project1_final.s3 values ('3', '11099963', 'EE320-15');
insert into project1_final.s3 values ('4', '11099963', 'EE334');

create role student;
grant connect on database postgres to student;
grant usage on schema project1_final to student;

grant select on table project1_final.course to student;

create user student1 with password 'readonly';
create user student2 with password 'readonly';
create user student3 with password 'readonly';
grant student to student1;
grant student to student2;
grant student to student3;

grant select on table project1_final.s1 to student1;
grant select on table project1_final.s2 to student2;
grant select on table project1_final.s3 to student3;


create role teacher;
grant connect on database postgres to teacher;
grant usage on schema project1_final to teacher;

grant update on table project1_final.course to teacher;
grant update on table project1_final.class to teacher;
grant update on table project1_final.prerequisite to teacher;
grant update on table project1_final.class_list to teacher;
grant update on table project1_final.student to teacher;
grant update on table project1_final.student_class to teacher;

create user teacher1 with password 'teacher';
create user teacher2 with password 'teacher';
grant teacher to teacher1;
grant teacher to teacher2;