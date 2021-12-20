-- create type CourseGrading  as enum('PASS_OR_FAIL', 'HUNDRED_MARK_SCORE');
-- create type DayOfWeek AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY');--UN

create table project2.Instructor(
                                    id            int primary key ,
                                    first_name    text,
                                    last_name     text
);

create table project2.Semester(
                                  id            serial primary key ,
                                  name          text unique ,
                                  end_date      date ,
                                  begin_date    date
);

create table project2.Department(
                                    id      serial primary key ,
                                    name    text unique not null
);

create table project2.Major(
                               id              serial primary key ,
                               name            text unique ,
                               department_id   int references project2.Department(id)
);

create table project2.Student(
                                 id              int primary key ,
                                 first_name      text,
                                 last_name       text,
                                 enrolledDate    date,
                                 major_id        int references project2.Major(id)
);

create table project2.Course(
                                id          text primary key ,
                                name        text,
                                credit      int,
                                classHour   int,
                                grading     text
);

create table project2.CourseSection(
                                       id              SERIAL primary key,
                                       course_id       text references project2.Course(id),
                                       semester_id     int references project2.Semester(id),
                                       name            text,
                                       totalCapacity   int,
                                       leftCapacity    int
);

create table project2.CourseSectionClass(
                                            id              SERIAL primary key,
                                            instructor_id   int references project2.Instructor(id),
                                            dayOfWeek       text,
                                            weekList        text,
                                            classBegin      smallint,
                                            classEnd        smallint,
                                            location        text,
                                            section_id      int references project2.CourseSection(id)
);

create table project2.course_major(
                                      id          serial primary key ,
                                      course_id   text references project2.Course(id),
                                      major_id    int references project2.Major(id),
                                      IsCompulsory        bool--是否为必修
);

create table project2.Student_CourseSection (
                                                id                  serial primary key ,
                                                student_id          int references project2.Student(id),
                                                coursesection_id    int references project2.CourseSection(id),
                                                grade               int
);

-- create table project2.CourseSearchEntry(
--                                            course_id           text references project2.Course(id),
--                                            section_id          int references project2.CourseSection(id),
--                                            sectionClasses      int references project2.CourseSectionClass(id),
--                                            conflictCourseNames text[]
-- );

-- create table project2.CourseTable(
--     id              serial primary key ,
--     course_id       text references Course(id),
--     section_id      int references CourseSection(id),
--     instructor_id   int references Instructor(id)
-- );

create table if not exists project2.prerequisite(
                                           course_id varchar,
                                           node_id serial,
                                           value varchar,
                                           children varchar,
                                           primary key (course_id,node_id),
                                           constraint fk_coi foreign key (course_id) references project2.course(id)
);

-- select grade from project2.course join project2.CourseSection on project2.course.id = project2.coursesection.course_id
--     join project2.Student_CourseSection on coursesection.id = student_coursesection.coursesection_id
--     where project2.course.id = ? and student_id = ?;
--
-- select grade from project2.student_coursesection where coursesection_id = ? and student_id = ?;
-- update project2.CourseSection set leftCapacity = leftCapacity-1 where id = ?;

-- update project2.Student_CourseSection set grade = ? where student_id = ? and coursesection_id = ?;
-- select grading
-- from project2.course join project2.CourseSection on project2.Course.id = project2.CourseSection.course_id
-- where project2.CourseSection.id = ?;
--
-- select grade, grading, course_id from project2.course join project2.CourseSection on course.id = coursesection.course_id
-- join project2.student_coursesection on coursesection.id = student_coursesection.coursesection_id
-- where student_id = ? and semester_id = ?;
--
-- select grade from project2.Student_CourseSection where student_id = ? and coursesection_id=?;
--
-- update project2.coursesection set leftCapacity = leftCapacity-1 where id = ?;

truncate project2.student_coursesection;
truncate project2.coursesectionclass;
truncate project2.coursesection;


