#!/usr/bin/env python

import re

with open('products_out.csv', 'r') as f:
	my_output = f.read().split('\n')
with open('product_mapping.csv', 'r') as f:
	their_output = f.read().split()

canon = {}
patt = re.compile('\"?([a-z0-9]+)\"?,\"([a-z0-9:/.]+)\"')
for i in their_output[1:]:
	match = patt.findall(i)[0]
	canon[match[0]] = match[1]

a_patt = re.compile('amazon,([a-z0-9]+)')
g_patt = re.compile('google,([a-z0-9:/.]+)')
correct = 0
for i in my_output[1:]:
	amaz = a_patt.findall(i)
	goog = g_patt.findall(i)
	#print('amazon: {} --- google: {}'.format(amaz, goog)) 
	
	if len(amaz) > 0 and len(goog) > 0:
		if canon.has_key(amaz[0]) and canon[amaz[0]] == goog[0]:
			correct += 1
			#print('amazon: {} --- google: {}'.format(amaz, goog)) 

print('percentage correct: {0:.{1}f}%'.format((float(correct) / len(their_output) * 100), 3))

