#!/usr/bin/env python

import re

with open('products_out.csv', 'r') as f:
	my_output = f.read().strip().split('\n')
with open('product_mapping.csv', 'r') as f:
	their_output = f.read().split()

canon = {}
patt = re.compile('\"?([a-z0-9]+)\"?,\"([a-z0-9:/.]+)\"')
for i in their_output[1:]:
	match = patt.findall(i)[0] # Remove the list "outer shell" to leave just a tuple 
	canon[match[0]] = match[1]

cluster = {}
matched = {}
a_unmatched = {}
g_unmatched = {}
duplicates = 0
tp = 0
fp = 0
fn = 0
cluster_id_patt = re.compile('([\d]+),(amazon|google)')
amaz_patt = re.compile('amazon,([a-z0-9]+)')
goog_patt = re.compile('google,([a-z0-9:/.]+)')
for i in my_output[1:]: # first element is the keys
	cluster_id = cluster_id_patt.findall(i)[0] # Remove the list "outer shell" to leave just a tuple 
	cid = cluster_id[0]
	name = cluster_id[1]
	amaz = amaz_patt.findall(i)
	goog = goog_patt.findall(i)
	comp = amaz if len(amaz) > 0 else goog
	comp = comp[0]
	
	if cluster.has_key(cid):
		if cluster[cid][0] == name:
			duplicates += 1 # duplicate from same source (i.e., there were 2 amazon or 2 google products present
		else:
			if name == 'amazon': # we know anything past the first clause isn't a duplicate
				matched[comp] = cluster[cid][1]
			else:
				matched[cluster[cid][1]] = comp
			del cluster[cid] # remove any matched cluster_ids from the cluster dict
	else:
		cluster[cid] = (name, comp)
# number of total entries = 3626, number of amazon = 1234, number of google = 2392
for i in cluster.values():
	if i[0] == 'amazon':
		a_unmatched[i[1]] = 0 # arbitrary value to take adv of hash table lookup
	else:
		g_unmatched[i[1]] = 0 # "						"
# find true and false positives
for k,v in matched.items():
	if canon.has_key(k) and canon[k] == v:
		tp += 1
	else:
		fp += 1
# find false negatives and values that aren't in the canonical
for k in a_unmatched.keys():
	if canon.has_key(k):
		fn += 1
		if g_unmatched.has_key(canon[k]):
			del g_unmatched[canon[k]]
	'''else:
		print("{} isn't in canonical".format(k))'''
for k in g_unmatched.keys():
	if k in canon.values():
		fn += 1
	'''else:
		print("{} isn't in canonical".format(k))'''
print('Duplicates: {}'.format(duplicates))
print('Precision: {}'.format(float(tp) / (tp + fp)))
print('Recall: {}'.format(float(tp) / (tp + fn)))
