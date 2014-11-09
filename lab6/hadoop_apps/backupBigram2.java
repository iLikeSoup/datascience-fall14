package org.myorg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class Bigram2 {

public static class BigramCount implements Comparable<BigramCount>{
	private String s1;
	private String s2;
	private int count;

	public BigramCount(String s1, String s2, int count) {
		this.s1 = s1;
		this.s2 = s2;
		this.count = count;
	}

	@Override
	public int compareTo(BigramCount o) {
		return (o.count - this.count);
	}
	
	public String toString() {
		return s1 + " " + s2 + ": " + count;
	}
}

// public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
// public void map(LongWritable key, Text value, OutputCollector output, Reporter reporter) throws IOException {


public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
	private IntWritable a = new IntWritable(1);
    private Text word = new Text();

    public void map(LongWritable key, Text value, OutputCollector output, Reporter reporter) throws IOException, FileNotFoundException {
	String line = value.toString();
	StringTokenizer tokenizer = new StringTokenizer(line);
	HashMap<String, ArrayList<BigramCount>> my_map = new HashMap<String, ArrayList<BigramCount>>();
	Set<String> allWords = new HashSet<String>();

	ArrayList<String> my_input = new ArrayList<String>();
	//String input = "";	
	//int i = 0;

	// populate data structures
	while (tokenizer.hasMoreTokens()) {
		my_input.add(tokenizer.nextToken());
	}
	/*for (; i < my_input.size() - 1; i++) {
		input += (my_input.get(i) + " ");
	}
	input += my_input.get(i);
	word.set("input: " + input);
	output.collect(word, a);*/

			//for (String i : my_input) {
				//word.set(i);
				//output.collect(word, a);
			//}
		/*} else {
				for (int x = 0; x < 2; x++) {
				if (my_map.containsKey(my_input[x])) {
					my_map.get(my_input[x]).add(new BigramCount(my_input[0], my_input[1], Integer.parseInt(my_input[2])));
				} else {
					ArrayList<BigramCount> tmp = new ArrayList<BigramCount>();
					tmp.add(new BigramCount(my_input[0], my_input[1], Integer.parseInt(my_input[2])));
					my_map.put(my_input[x], tmp);
				}
				allWords.add(my_input[x]);
			}
		}*/
	//}
	
	for (String s : allWords) {
		word.set(s);
		//output.collect(word, my_map.get(s));
	}

    }
}
//public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
//public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {

public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
    public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
	/*while (values.hasNext()) {
		sum -= values.next().get();
	}*/
	int sum = values.next().get();
	output.collect(key, new IntWritable(sum));
/*
	    	ArrayList<BigramCount> list = values.next();
                int min = -1;
                BigramCount minBigram = null;
                Set<BigramCount> top5 = new TreeSet<BigramCount>();
                
                for (BigramCount b : list) {
                        // If we found an element with a higher count than the lowest element in the current top 5
                        if (b.count > min) {
                                // If we need to replace an element with the new top 5 member
                                if (top5.size() == 5) {
                                        top5.remove(minBigram);
                                }
                                // New lowest member of the top 5
                                min = b.count;
                                minBigram = b;
                                // Add to set
                                top5.add(b);
                        }
                }
                
                String tostr = "";
                for (BigramCount b : top5) {
                        tostr += (b + " | ");
		}
		word.set(tostr);
		output.collect(key, tostr);
*/
	}
}

   public static void main(String[] args) throws Exception {
     JobConf conf = new JobConf(Bigram2.class);
     conf.setJobName("bigram2");

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

