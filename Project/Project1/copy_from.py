import psycopg2
from io import StringIO
import csv
import datetime

start_time1 = datetime.datetime.now()
file = open('select_course.csv', 'r', encoding='UTF-8')
data = csv.reader(file)
data = list(data)
print('size of data is : ' + str(len(data)))


f = StringIO()
f1 = StringIO()
i = 1
for record in data:
    s = str(record[3])+"\t"+str(record[0])+"\t"+record[1]+"\t"+str(record[2])+"\n"
    f.write(s)
    for j in record[4:]:
        s = str(i)+"\t"+record[3]+"\t"+j+"\n"
        f1.write(s)
        i += 1


end_time1 = datetime.datetime.now()
print('read and write into file takes: ' + str(end_time1 - start_time1))

f.seek(0)
f1.seek(0)

conn = psycopg2.connect(host='127.0.0.1', user="postgres", password="", database="postgres")
cur = conn.cursor()
print("student table")
cur.copy_from(f, 'project1.student2', columns=('student_id', 'name', 'gender', 'college'))
print("student_class table")
cur.copy_from(f1, 'project1.student_class2', columns=('number', 'student_id', 'course_id'))

conn.commit()
cur.close()
conn.close()
print('成功写入数据库')

end_time = datetime.datetime.now()
print('whole script takes: ' + str(end_time - start_time1))
