import psycopg2
import csv
import datetime

starttime = datetime.datetime.now() # 开始计时

file = open('test60w.csv', 'r', encoding='UTF-8')
data = csv.reader(file)
data = list(data)
print("csv read done")
print(len(data))
file.close() # csv文件读入


def con():  # 创建连接
    connect = psycopg2.connect(database='postgres', user='postgres', password='', host='127.0.0.1', port='5432')
    return connect


def close():  # 退出连接
    cur.close()
    con.close()


con = con()
cur = con.cursor()

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

student_insert = "insert into project1.student_test (name, gender, college, student_id)" \
                 "values (%s, %s, %s, %s)"

student_class_insert = "insert into project1.student_class_test (number, student_id, course_id)" \
                       "values (%s, %s, %s)"

student_data = []
student_class_data = []
i = 1
for record in data:
    # print(record)
    student_data.append(record[:4])
    for item in record[4:]:
        # print(item)
        student_class_data.append([i, record[3], item])
        i += 1

cur.execute("drop table if exists project1.student_class_test")
cur.execute("drop table if exists project1.student_test")
cur.execute(student_create)
cur.execute(student_class_create)

print(datetime.datetime.now()-starttime)
print("start student table insertion")
cur.executemany(student_insert, student_data)
print(datetime.datetime.now()-starttime)
print("start student_class table insertion")
cur.executemany(student_class_insert, student_class_data)

con.commit()
close()

endtime = datetime.datetime.now()
print(endtime - starttime)
# use exexutemany(), takes 50 mins.