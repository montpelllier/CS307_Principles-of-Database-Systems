create table if not exists project1_final.course(
    course_ID text primary key ,
    course_Name text not null ,
    course_Credit smallint not null ,--学分
    course_Dept text not null
);

create table if not exists project1_final.class(
    class_ID int primary key ,
    class_Name text not null ,
    teachers text[] ,
    course_Hour smallint not null ,--学时
    total_Capacity smallint not null ,
    course_id text references project1_final.course(course_id)
);

create table if not exists project1_final.class_List(
    class_id int references project1_final.class ,
    week_List int[] ,
    location text ,--有些课为空
    start_class_Time text not null ,
    end_class_Time text not null,
    weekday smallint not null CHECK(weekday > 0 and weekday < 8)
);

create table if not exists project1_final.prerequisite (
    record_id int primary key ,
    course_id text references project1_final.course(course_id),
    prerequisite_list_or_relation text[]
);

create table if not exists project1_final.student (
    student_id text primary key,
    name text not null ,
    gender text not null,
    college text not null
);

create table if not exists project1_final.student_class (
    number int primary key,
    student_id text,
    foreign key (student_id) references project1_final.student(student_id),
    course_id text,
    foreign key (course_id) references project1_final.course(course_id)
);