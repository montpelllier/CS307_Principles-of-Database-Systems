import csv
import pypinyin



import datetime

file = open('C:\\Users\\Lenovo\\Desktop\\project\\data\\select_course.csv', 'r', encoding='UTF-8')
data = csv.reader(file)
data = list(data)

starttime = datetime.datetime.now()
data2=sorted(data, key=lambda data:data[3], reverse=True)
for i in data2:
    print(i[3]+','+i[0]+','+i[1]+','+i[2])
endtime = datetime.datetime.now()
print(endtime - starttime)


starttime = datetime.datetime.now()
data=sorted(data, key=lambda data:len(data), reverse=True)
for i in data:
    print(i[3]+','+str(len(i)-4))
endtime = datetime.datetime.now()
print(endtime - starttime)