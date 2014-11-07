./delete_bigram_output.sh
./bigram1_java.sh
./bigram2_java.sh
hadoop jar bigram1.jar org.myorg.Bigram1 bigram/input bigram/pipe
hadoop jar bigram2.jar org.myorg.Bigram2 bigram/pipe bigram/output
