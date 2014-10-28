import nltk
import re

patt = re.compile('([A-Z]+) ([\w]+)\/NNP')
for i in range(1, 10):
	with open("mn{}.html".format(i), "r") as myfile:
	    data = myfile.read()
	sentences = nltk.sent_tokenize(data)
	sentences = [nltk.word_tokenize(sent) for sent in sentences]
	sentences = [nltk.pos_tag(sent) for sent in sentences]
	chunk = [nltk.ne_chunk(sent) for sent in sentences]
	for c in chunk:
	    regex = patt.findall(str(c))
	    if regex:
		for r in regex:
		    print('{}, {}'.format(r[1], r[0]))
