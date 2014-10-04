#!/usr/bin/env python

### Code derived from https://avro.apache.org/docs/1.7.5/gettingstartedpython.html

from avro.datafile import DataFileReader
from avro.io import DatumReader
import pandas as pd

reader = DataFileReader(open("countries.avro", "r"), DatumReader())
df = []
count = 0

for user in reader:
    df.append(pd.DataFrame(user, index=['0, 1, 2, 3, 4'])) # Read JSON data into a list of DataFrames
reader.close()

for i in df:
	if i['population'][0] > 10000000:
		count += 1

print('Number of countries with population over 10000000: {0}'.format(count))
