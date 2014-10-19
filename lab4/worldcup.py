import pandas as pd
import re

with open('worldcup.txt', 'r') as f:
    inp = f.read()
text = re.split('[A-Z]{3}\}', inp)
del text[0] # deletes extraneous part of text file
length = len(text)
coun = re.findall('([A-Z]{3})\}', inp)
comp_num = re.compile('\|([\d]) \(\[\[')
comp_years = re.compile('\|([\d]{4})\]')
df = []

for i in range(length):
    #print text[i]
    num = comp_num.findall(text[i])
    num = [int(x) for x in num]
    years = comp_years.findall(text[i])
    places = []
    counter = 1
    for a in range(len(num)):
        for b in range(num[a]):
            places.append(counter)
        counter += 1    
    my_ind = [str(x) for x in range(len(years))]
    df.append(pd.DataFrame({'Country': coun[i], 'Year': years, 'Place': places}, index=my_ind))
table = pd.concat([df[x] for x in range(length)], keys=[x for x in range(length)])
print(table.head())

# World Cup Part 2
table2 = table.pivot('Country', 'Year', 'Place')
print(table2.head())
