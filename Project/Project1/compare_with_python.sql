select count(*) from project1_final.student where name like '%邹%';
select count(*) from project1_final.student where student_id like '%7%';
select count(*) from project1_final.student where gender = 'F';
select count(*) from (select student_id from project1_final.student_class where course_id like '%CH%' group by student_id) s;
select * from (select student_id, count(*) num from project1_final.student_class group by student_id) s order by num desc;
select * from project1_final.student order by student_id desc;