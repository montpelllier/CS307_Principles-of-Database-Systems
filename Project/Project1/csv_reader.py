import psycopg2
import csv
import datetime


def con():  # 创建连接
    connect = psycopg2.connect(database='postgres', user='readonly_user', password='readonly', host='127.0.0.1', port='5432')
    return connect


def insert(student_db, student_class_db, data):
    i = 1
    for record in data:
        student_list = (record[3], record[0], record[1], record[2])
        cur.execute(student_db, student_list)
        # print(i)
        for item in record[4:]:
            s_list = (i, record[3], item)
            i += 1
            cur.execute(student_class_db, s_list)


start_time = datetime.datetime.now()  # 开始计时

file = open('test1w.csv', 'r', encoding='UTF-8')
data = csv.reader(file)
data = list(data)
print("csv read done")
print(len(data))
file.close()

student_create = "create table project1.student_test (" \
                 "student_id text primary key," \
                 "name text not null ," \
                 "gender text not null," \
                 "college text not null)"

student_class_create = "create table project1.student_class_test (" \
                       "number int primary key," \
                       "student_id text," \
                       "foreign key (student_id) references project1.student_test(student_id)," \
                       "course_id text," \
                       "foreign key (course_id) references project1.course(course_id))"

student_insert = "insert into project1.student_test (student_id, name, gender, college)"\
                 "values (%s, %s, %s, %s)"

student_class_insert = "insert into project1.student_class_test (number, student_id, course_id)"\
                       "values (%s, %s, %s)"

con = con()
cur = con.cursor()

cur.execute("drop table if exists project1.student_class_test")
cur.execute("drop table if exists project1.student_test")
cur.execute(student_create)
cur.execute(student_class_create)

print(datetime.datetime.now()-start_time)
insert(student_insert, student_class_insert, data)

con.commit()
cur.close()
con.close()

end_time = datetime.datetime.now()
print(end_time - start_time)
