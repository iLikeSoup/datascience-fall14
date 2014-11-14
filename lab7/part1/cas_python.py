#!/usr/bin/env python

import logging

log = logging.getLogger()
#log.setLevel('DEBUG')
log.setLevel('WARN')
handler = logging.StreamHandler()
handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(name)s: %(message)s"))
log.addHandler(handler)

from cassandra import ConsistencyLevel
from cassandra.cluster import Cluster
from cassandra.query import SimpleStatement

KEYSPACE = "drwho"

def exe(query, num):
    print('**********Printing query {}*****************'.format(num))
    try:
        rows = query.result()
    except Exception:
        log.exception(0)

    return rows


def exe_query1(query, num):
    rows = exe(query, num)
    for row in rows:
	print('(OwnerId: {}, AdId: {}) --- {}'.format(row[0], row[1], float(row[2]) / row[3]))
    print('')	


def exe_query2(query, num):
    rows = exe(query, num) 
    d = {}
    for row in rows:
	if d.has_key(row[0]):
		d[row[0]] += float(row[2]) / row[3]
	else:
		d[row[0]] = float(row[2]) / row[3]	

    if num == '2':
	    for k,v in d.items():
		print('(OwnerId: {}) --- {}'.format(k, v))
    else:
	print('(OwnerId: {}) --- {}'.format(2, d[2]))
    print('')


def exe_query3(query, num):
	rows = exe(query, num)
	for row in rows:
		print('(OwnerId: {}, AdId: {}) --- {}'.format(row[0], row[1], float(row[2]) / row[3]))
	print('')


def main():
    cluster = Cluster(['127.0.0.1'])
    session = cluster.connect()

    #session.execute("DROP KEYSPACE " + KEYSPACE)

    log.info("creating keyspace...")
    session.execute("""
        CREATE KEYSPACE IF NOT EXISTS %s
        WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '2' }
        """ % KEYSPACE)

    log.info("setting keyspace...")
    session.set_keyspace(KEYSPACE)

    log.info("creating table...")
    session.execute("""
	CREATE TABLE denormalized (
	    OwnerId int,
	    AdId int,
	    numClicks int,
	    numImpressions int,
	    PRIMARY KEY (OwnerId, AdId)
	)
        """)

    prepared = session.prepare("""
        INSERT INTO denormalized (OwnerId, AdId, numClicks, numImpressions)
        VALUES (?, ?, ?, ?)
        """)

    ins = []
    ins.append([3, 4, 1, 36])
    ins.append([1, 1, 1, 10])
    ins.append([1, 2, 0, 5])
    ins.append([1, 3, 1, 20])
    ins.append([1, 4, 0, 15])
    ins.append([2, 1, 0, 10])
    ins.append([2, 2, 0, 55])
    ins.append([2, 3, 0, 13])
    ins.append([2, 4, 0, 21])
    ins.append([3, 1, 1, 32])
    ins.append([3, 2, 0, 23])
    ins.append([3, 3, 2, 44])

    for i in range(len(ins)):
        log.info("inserting row %d" % i)
        session.execute(prepared, (ins[i][0], ins[i][1], ins[i][2], ins[i][3]))

    one = session.execute_async("select * from denormalized")
    two = session.execute_async("select * from denormalized where OwnerId = 1 and AdId = 3")
    exe_query1(one, '1')
    exe_query2(one, '2')
    exe_query3(two, '3')
    exe_query2(one, '4')


    session.execute("DROP KEYSPACE " + KEYSPACE)

if __name__ == "__main__":
    main()
