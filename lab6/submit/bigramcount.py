from pyspark import SparkContext, SparkConf
import re

def bigram_split(line):
	ret = []
	split = line.strip().split(' ')
	for i in range(len(split) - 1):
		ret.append(split[i] + ' ' + split[i+1])
	return ret

def group_by_key(bigram):
	words = bigram[0]
	key = words.split(' ')[0]
	return (key, [bigram])

def reduction(a, b):
	c = []
	for i in a:
		c.append(i)
	for i in b:
		c.append(i)
	return c

def top5(bigram):
	mincount = -1
	minbigram_index = -1
	ret = []
	for i in bigram[1]:
		if i[1] >= mincount:
			if len(ret) == 5:
				del ret[minbigram_index]
			mincount = i[1]
			ret.append(i)
			minbigram_index = len(ret) - 1
	return [bigram[0], ret]
			
conf = SparkConf().setAppName("BigramCount").setMaster("local")

sc = SparkContext(conf=conf)

textFile = sc.textFile("bible+shakes.nopunc")

counts = textFile.flatMap(bigram_split).map(lambda word: (word, 1)).reduceByKey(lambda a, b: a + b)
bigrams = counts.map(group_by_key).reduceByKey(reduction).map(top5)

size = int(bigrams.count())
for i in bigrams.take(size):
	print i
#counts.saveAsTextFile("output")
