#!/bin/python

import datetime
import json
import re


def parse_entry(entry: list):
    date: str = entry[0]
    class_code: str = entry[1]
    day_name: str = entry[2]
    hour: str = entry[3]
    duration: str = entry[4]
    class_name: str = entry[5]
    groups: list = entry[6].split()
    teacher: str = entry[7]
    room: str = entry[8]

    return date, day_name, hour, duration, room, class_code, class_name, groups, teacher


def parse_entry_v2(entry: list):
    day = datetime.datetime.strptime(entry[0], '%d/%m/%Y')

    duration_match = re.compile(r'^(?P<h>[0-9]{1,2})h((?P<m>[0-9]{2})min|)$').match(entry[4])
    h = int(duration_match.group('h'))
    m = 0 if duration_match.group('m') is None else int(duration_match.group('m'))
    duration = datetime.timedelta(hours=h, minutes=m)

    hour_match = re.compile(r'^(?P<h>[0-9]{2})h(?P<m>[0-9]{2})$').match(entry[3])
    hour = int(hour_match.group('h')) * 3600 + int(hour_match.group('m')) * 60

    date = {
        'day': int(day.timestamp()),
        'hour': hour,
        'duration': duration.total_seconds() / 3600
    }

    klass = {
        'code': entry[1],
        'name': entry[5],
        'teacher': entry[7],
        'room': entry[8]
    }

    groups = entry[6].split()

    return date, klass, groups


def create_entry(date: str, day_name: str, hour: str, duration: str, room: str, class_code: str, class_name: str,
                 groups: list, teacher: str):
    return {
        'date': date,
        'day_name': day_name,
        'hour': hour,
        'duration': duration,
        'room': room,
        'class_code': class_code,
        'class_name': class_name,
        'groups': groups,
        'teacher': teacher
    }


def create_entry_v2(date, klass, groups):
    return {
        'date': date,
        'class': klass,
        'groups': groups
    }


if __name__ == '__main__':
    with open('raw.json', 'r') as f:
        data: list = json.load(f, encoding='UTF-8')
        # remove the 2 table headers
        data.pop(0)
        data.pop(0)

        formatted = [create_entry_v2(*parse_entry_v2(c)) for c in data]

    with open('timetable.json', 'w') as f:
        f.write(json.dumps(formatted, indent=4, sort_keys=True))
