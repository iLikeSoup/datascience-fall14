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


def main():
    cluster = Cluster(['127.0.0.1'])
    session = cluster.connect()

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
    ins[0] = [3, 4, 1, 36]
    ins[1] = [1, 1, 1, 10]
    ins[2] = [1, 2, 0, 5]
    ins[3] = [1, 3, 1, 20]
    ins[4] = [1, 4, 0, 15]
    ins[5] = [2, 1, 0, 10]
    ins[6] = [2, 2, 0, 55]
    ins[7] = [2, 3, 0, 13]
    ins[8] = [2, 4, 0, 21]
    ins[9] = [3, 1, 1, 32]
    ins[10] = [3, 2, 0, 23]
    ins[11] = [3, 3, 2, 44]

    for i in range(len(ins)):
        log.info("inserting row %d" % i)
        session.execute(prepared, (ins[i][0], ins[i][1], ins[i][2], ins[i][3]))

    future = session.execute_async("SELECT * FROM drwho")

    log.info("key\tcol1\tcol2")
    log.info("---\t----\t----")

    try:
        rows = future.result()
    except Exception:
        log.exeception()

    for row in rows:
        print row[0], row[1], row[2]
        log.info('\t'.join(row))

    session.execute("DROP KEYSPACE " + KEYSPACE)

if __name__ == "__main__":
    main()
