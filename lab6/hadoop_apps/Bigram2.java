package org.myorg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.*;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class Bigram2 {

// public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
// public void map(LongWritable key, Text value, OutputCollector output, Reporter reporter) throws IOException {


public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
    private Text my_key = new Text();
    private Text my_value = new Text();

    public void map(LongWritable key, Text value, OutputCollector output, Reporter reporter) throws IOException, FileNotFoundException {
	String line = value.toString();
	StringTokenizer tokenizer = new StringTokenizer(line);
	ArrayList<String> inputList = new ArrayList<String>();

	while (tokenizer.hasMoreTokens()) {
		inputList.add(tokenizer.nextToken());
	}
	my_key.set(inputList.get(0));
	my_value.set(inputList.get(0) + " " + inputList.get(1) + " " + inputList.get(2));
	output.collect(my_key, my_value);
    }
}
//public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
//public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {

public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
    private Text my_value = new Text();

    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
	HashMap<String, Integer> my_map = new HashMap<String, Integer>();
	String v = "";
	while (values.hasNext()) {
		v += values.next();
		v += "|";
	}

	/* This works in normal java, but for some reason not hadoop....@#%$@%!@$##!@$
	String v_arr[] = v.split("\\|");
	for (String tmp : v_arr) {
		Matcher matcher = Pattern.compile("(.+)([\\d]$)").matcher(tmp);
		if (matcher.find()) {
			my_map.put(matcher.group(1).trim(), Integer.parseInt(matcher.group(2)));
		}
	}*/
	
	int min = -1;
	ArrayList<String> top5 = new ArrayList<String>();
	String minBigram = null;
	int pipe = -1;
	int count = 0;

	while ((pipe = v.indexOf("|")) != -1) {
		String bigram = v.substring(0, pipe);

		try {
			count = Integer.parseInt(bigram.substring(bigram.length() - 1));
			//count = Integer.parseInt(v);
		} catch (NumberFormatException e){
			count = Integer.parseInt(v);
		}
		//int count = Integer.parseInt(v.substring(pipe+1));
		if (count >= min) {
			if (top5.size() == 5) {
				top5.remove(minBigram);
			}
			// New lowest member of the top 5
			min = count;
			minBigram = bigram;
			// Add to set
			top5.add(bigram);
	
		}
		v = v.substring(pipe + 1);
	}
	
	String tostr = "";
	for (String b : top5) {
		//tostr += (b + "~~~" + String.valueOf(my_map.get(b)) + "\n");
		tostr += (b + " ");
	}
	tostr = tostr.trim();
	//tostr += null;

	my_value.set(tostr);
	output.collect(key, my_value);
    }
}

   public static void main(String[] args) throws Exception {
     JobConf conf = new JobConf(Bigram2.class);
     conf.setJobName("bigram2");

     conf.setOutputKeyClass(Text.class);
     conf.setOutputValueClass(Text.class);

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
