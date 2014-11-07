hadoop fs -mkdir bigram
hadoop fs -mkdir bigram/input
hadoop fs -put $1 bigram/input/

#OUTPUT hadoop fs -cat bigram/output/part-00000
#OUTPUT hadoop fs -cat bigram/pipe/part-00000

