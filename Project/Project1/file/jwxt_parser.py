import json
import sys


# Run as: python3 jwxt_parser.py <json file>
with open(sys.argv[1], 'r', encoding='utf-8') as f:
    data = json.load(f)

for course in data: # iterate through all courses
    print(course)
    print(course['courseId'])
    for c in course['classList']: # iterate through all classtime
        print(c)
