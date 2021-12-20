import psycopg2
import json
import datetime


def read_json(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)


def connect():  # 创建连接
    connection = psycopg2.connect(database='postgres', user='postgres', password='',
                                  host='127.0.0.1', port='5432')
    return connection


def close():
    cur.close()
    con.close()


start_time = datetime.datetime.now()

json_path = 'course_info.json'
json_data = read_json(json_path)

course_insert = "insert into project1_final.course " \
                "(course_id, course_name, course_credit, course_dept) " \
                "values (%s,%s,%s,%s) on conflict (course_id) do nothing "

class_insert = "insert into project1_final.class " \
               "(course_id, teachers, class_name, course_Hour, total_capacity, class_id) " \
               "values (%s,%s,%s,%s,%s,%s)"

classList_insert = "insert into project1_final.class_list " \
                   "(class_id, location, start_class_Time, end_class_Time, weekday, week_list) " \
                   "values (%s,%s,%s,%s,%s,%s)"

prerequisite_insert = "insert into project1_final.prerequisite " \
                      "(record_id, course_id, prerequisite_list_or_relation)" \
                      "values(%s, %s, %s)"

con = connect()
cur = con.cursor()

i = 0
num = 0
past = []
exceptions = ['BIO304', 'CH306', 'CH314', 'CH304']
for course in json_data:
    i += 1
    data_list = (
        course['courseId'], course['courseName'].strip(), course['courseCredit'], course['courseDept']
    )
    cur.execute(course_insert, data_list)  # course表插入

    if course['teacher'] is not None:
        data_list = (
            course['courseId'], course['teacher'].strip().split(','), course['className'].strip(),
            course['courseHour'], course['totalCapacity'], i
        )
    else:
        data_list = (
            course['courseId'], course['teacher'], course['className'].strip(),
            course['courseHour'], course['totalCapacity'], i
        )
    cur.execute(class_insert, data_list)  # class表插入

    for c in course['classList']:
        nums = [int(num) for num in c['weekList']]
        time_list = c['classTime'].split('-')
        data_list = (i, c['location'], time_list[0], time_list[1], c['weekday'], nums)
        cur.execute(classList_insert, data_list)  # class_list表插入

    data_list = (course['courseId'], course['prerequisite'])
    if data_list[1] is None or data_list[0] in exceptions or data_list[0] == past:
        continue
    else:
        past = data_list[0]
        data_list_new = data_list[1].replace('(上)', '1').replace('(下)', '2')
        data_list_new = data_list_new.split('并且')
        for item in data_list_new:
            num += 1
            input_list = []
            item_new = item.replace('(', '').replace(')', '').split('或者')
            for item_new_new in item_new:
                input_list.append(item_new_new.replace('1', '(上)').replace('2', '(下)').
                                  replace('大学物理B(上)', '大学物理 B(上)').
                                  replace('大学物理A(下)', '大学物理A（下）').strip())
            # print([num, data_list[0], list(set(input_list))])
            cur.execute(prerequisite_insert, [num, data_list[0], list(set(input_list))])  # 先修课插入

cur.execute(prerequisite_insert, [285, 'BIO304', ['普通生物学']])
cur.execute(prerequisite_insert, [286, 'BIO304', ['概率论与数理统计', '概率论']])
cur.execute(prerequisite_insert, [287, 'BIO304', ['概率论与数理统计', '数理统计']])

cur.execute(prerequisite_insert, [288, 'CH306', ['物理化学I']])
cur.execute(prerequisite_insert, [289, 'CH306', ['无机化学II', '金属有机化学']])
cur.execute(prerequisite_insert, [290, 'CH306', ['无机化学II', '配位化学']])

cur.execute(prerequisite_insert, [291, 'CH314', ['有机化学II']])
cur.execute(prerequisite_insert, [292, 'CH314', ['现代策略合成', '现代策略合成中的金属有机化学']])
cur.execute(prerequisite_insert, [293, 'CH314', ['现代策略合成', '现代策略合成中的高等有机化学']])

cur.execute(prerequisite_insert, [294, 'CH304', ['物理化学I']])
cur.execute(prerequisite_insert, [295, 'CH304', ['无机化学II', '金属有机化学']])
cur.execute(prerequisite_insert, [296, 'CH304', ['无机化学II', '配位化学']])  # 特殊处理

con.commit()
close()
print('成功写入数据库')

end_time = datetime.datetime.now()
print('import into database takes: ' + str(end_time - start_time))
