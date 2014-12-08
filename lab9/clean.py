import re
import sys

# To delete table: MATCH (a:State) OPTIONAL MATCH (a)-[r1]-() DELETE a,r1

with open('tmp2.txt', 'r') as f:
	text = f.read()
edges = [x.strip() for x in text.split('),')]
patt = re.compile('([\d]+L), ([\d]+L), ([\d]+\.[\d]+)')
d = {}

for i in edges:
	vals = patt.search(i).groups()
	v0 = 's' + vals[0]
	v1 = 's' + vals[1]
	src = '({})-[:BORDERS {{dist: {}}}]->({}),'.format(v0, vals[2], v1)
	dst = '({})-[:BORDERS {{dist: {}}}]->({}),'.format(v1, vals[2], v0)

	if d.has_key(vals[0]):
		d[vals[0]].append(src)
	else:
		d[vals[0]] = [src]

	if d.has_key(vals[1]):
		if dst not in d[vals[1]]:
			d[vals[1]].append(dst)
	else:
		d[vals[1]] = [dst]

keyorder = ['{}L'.format(x) for x in xrange(1, 51)]
sorted_d = sorted(d.items(), key=lambda i:keyorder.index(i[0]))

with open("query3.txt", "a") as myfile:
	for i in sorted_d:
		for j in i:
			if isinstance(j, list):
				for k in j:
					myfile.write(k + '\n')