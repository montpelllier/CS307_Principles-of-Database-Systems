import psycopg2
import json

def read_json(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)


def connect():  # 创建连接
    connection = psycopg2.connect(database='postgres', user='postgres', password='521459', host='127.0.0.1',
                                  port='5432')
    return connection

json_path = 'C:\\Users\\Lenovo\\Desktop\\project\\data\\course_info.json'
json_data = read_json(json_path)

search_list_courseName = ['遥感原理', 'MATLAB程序设计', '量子计算', '病理学', '遗传学']
search_list_courseID = ['MED307', 'ESE329', 'PHY442', 'PHY442', 'BIO301']

# 寻找课程名字
for course in json_data:
    if course['courseName'] in search_list_courseName:
        print(course)
print()
#寻找课程编码
for course in json_data:
    if course['courseId'] in search_list_courseID:
        print(course)
print()
#寻找上课时长
for course in json_data:
    if course['courseHour'] == 64:
        print(course)