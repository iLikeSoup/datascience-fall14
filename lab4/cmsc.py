import pandas as pd
import re

with open('cmsc.txt', 'r') as f:
    text = f.read()
classes = re.split('CMSC[\d]{3}', text)
del classes[0]
compile_courseNum = re.compile('CMSC[\d]{3}')
compile_sectionNum = re.compile('\n([\d]{4})')
compile_instructor = re.compile('\n((?:[A-Za-z-.:]+)(?: [A-Za-z-]+)+)')
compile_seats_open_waitlist = re.compile('Seats \(Total: ([\d]{1,3}), Open: ([\d]{1,3}), Waitlist: ([\d]{1,3})\)')
compile_days_time = re.compile('\n([MWFTuh]+) ([\d]{1,2}:[\d]{1,2}(?:a|p)m - [\d]{1,2}:[\d]{1,2}(?:a|p)m)')
compile_building_room = re.compile('\n([A-Z]{3})  ([\d]{4})')

courseNum = compile_courseNum.findall(text)
length = len(courseNum)
df = []
for i in range(length):
    sectionNum = compile_sectionNum.findall(classes[i])
    instructor = compile_instructor.findall(classes[i])
    seats_open_waitlist = compile_seats_open_waitlist.findall(classes[i])
    days_time = compile_days_time.findall(classes[i])
    building_room = compile_building_room.findall(classes[i])
    numRows = len(sectionNum) # length will be the same for any of the above (within the for loop)
    df_courseNum = pd.DataFrame({'Course No.': courseNum[i]}, index=[str(x) for x in range(numRows)])
    df_sectionNum = pd.DataFrame({'Section No.': sectionNum}, index=[str(x) for x in range(numRows)])
    df_instructor = pd.DataFrame({'Instructor': instructor}, index=[str(x) for x in range(numRows)])
    df_seats = pd.DataFrame({'Seats': [x[0] for x in seats_open_waitlist]}, index=[str(x) for x in range(numRows)])
    df_open = pd.DataFrame({'Open': [x[1] for x in seats_open_waitlist]}, index=[str(x) for x in range(numRows)])
    df_waitlist = pd.DataFrame({'Waitlist': [x[2] for x in seats_open_waitlist]}, index=[str(x) for x in range(numRows)])
    df_days = pd.DataFrame({'Days': [x[0] for x in days_time]}, index=[str(x) for x in range(numRows)])
    df_time = pd.DataFrame({'Time': [x[1] for x in days_time]}, index=[str(x) for x in range(numRows)])
    df_building = pd.DataFrame({'Building': [x[0] for x in building_room]}, index=[str(x) for x in range(numRows)])
    df_room = pd.DataFrame({'Room': [x[1] for x in building_room]}, index=[str(x) for x in range(numRows)])
    df.append(df_courseNum.join([df_sectionNum, df_instructor, df_seats, df_open, df_waitlist, df_days, df_time, df_building, df_room], 
                                how='outer'))
table = pd.concat([df[x] for x in range(length)], keys=[x for x in range(length)])

