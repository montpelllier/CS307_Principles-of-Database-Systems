import psycopg2
from io import StringIO
import csv


import datetime

# starttime = datetime.datetime.now()
# endtime = datetime.datetime.now()
# print(endtime - starttime)
file = open('C:\\Users\\Lenovo\\Desktop\\project\\data\\select_course.csv', 'r', encoding='UTF-8')
data = csv.reader(file)
data = list(data)
# print(data)


# select_student_name = ['雷社富', '俞作群', '姚英经', '俞市意', '禹钟两', '常团境']
# select_student_id = ['11010010', '12010010', '12910010', '13410010', '14010010', '14990919']
result = []

# #寻找学生名字
# starttime = datetime.datetime.now()
# i = 0
# for j in data:
#     if '邹' in j[0]:
#         i += 1
#         result.append(j)
#         # print(j)
# print('名字中含"邹"的学生个数: '+str(i))
# endtime = datetime.datetime.now()
# print('程序执行时长: '+str(endtime - starttime))
# # print(result)
# result.clear()

# #寻找学生编号
# starttime = datetime.datetime.now()
# i = 0
# for j in data:
#     if '7' in j[3]:
#         i += 1
#         result.append(j)
#         # print(j)
# print('编号中有7的学生数量: '+str(i))
# endtime = datetime.datetime.now()
# print('程序执行时长: '+str(endtime - starttime))
# # print(result)
# result.clear()
#
# #计算性别
# starttime = datetime.datetime.now()
# i = 0
# for j in data:
#     if j[1] == 'F':
#         i += 1
#         result.append(j)
# print('男性学生个数: '+str(i))
# endtime = datetime.datetime.now()
# print('程序执行时长: '+str(endtime - starttime))
# # print(result)
# result.clear()
#
# 计算选了物理课(PHY)的人数
starttime = datetime.datetime.now()
i = 0
count = 0
for j in data:
    for k in j[4:]:
        if 'CH' in k:
            i += 1
            break
    if i != 0:
        count += 1
        result.append(j)
    i = 0
print('选择CH课学生个数: '+str(count))
endtime = datetime.datetime.now()
print('程序执行时长: '+str(endtime - starttime))
# print(result)
result.clear()