package org.myorg;

	import java.io.IOException;
	import java.util.*;
	
	import org.apache.hadoop.fs.Path;
	import org.apache.hadoop.conf.*;
	import org.apache.hadoop.io.*;
	import org.apache.hadoop.mapred.*;
	import org.apache.hadoop.util.*;
	
	public class Bigram1 {
	
        public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
            private final static IntWritable count = new IntWritable(1);
            private Text word = new Text();

            public void map(LongWritable key, Text value, OutputCollector output, Reporter reporter) throws IOException {
                String line = value.toString();
		StringTokenizer tokenizer = new StringTokenizer(line);
		String one = null, two = null;
		if (tokenizer.hasMoreTokens()) {
			one = tokenizer.nextToken();
		}		

		while (tokenizer.hasMoreTokens()) {
			two = tokenizer.nextToken();
			word.set(one + " " + two);
			output.collect(word, count); 
			one = two;
		}

            }
        }
	
        public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
            public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
                int sum = 0;
                while (values.hasNext()) {
                    sum += values.next().get();
                }
                output.collect(key, new IntWritable(sum));
            }
        }
	
	   public static void main(String[] args) throws Exception {
	     JobConf conf = new JobConf(Bigram1.class);
	     conf.setJobName("bigram1");
	
	     conf.setOutputKeyClass(Text.class);
	     conf.setOutputValueClass(IntWritable.class);
	
	     conf.setMapperClass(Map.class);
	     conf.setCombinerClass(Reduce.class);
	     conf.setReducerClass(Reduce.class);
	
	     conf.setInputFormat(TextInputFormat.class);
	     conf.setOutputFormat(TextOutputFormat.class);
	
	     FileInputFormat.setInputPaths(conf, new Path(args[0]));
	     FileOutputFormat.setOutputPath(conf, new Path(args[1]));
	
	     JobClient.runJob(conf);
	   }
	}
	
