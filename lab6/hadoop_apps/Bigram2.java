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
	
	String v = "";
	while (values.hasNext()) {
		v += values.next();
		if (values.hasNext()) {
			v += " ";
		}
	}
	HashMap<String, Integer> my_map = new HashMap<String, Integer>();

	/*while (values.hasNext()) {
		String k = values.next().toString();
		String delim[] = k.split("[\\d]+$");
		my_map.put(delim[0] + " " + delim[1], Integer.parseInt(delim[2]));
	}*/

	//if (delimeter.length > 1) {
		//for (int i = 0; i < delimeter.length; i += 3) {
		//		my_map.put(delimeter[i] + " " + delimeter[i + 1], Integer.parseInt(delimeter[i + 2]));
		//}

		int min = -1;
		HashSet<String> top5 = new HashSet<String>();
		String minBigram = null;

		for (String bigram : my_map.keySet()) {
			// If we found an element with a higher count than the lowest element in the current top 5
			if (my_map.get(bigram) > min) {
				// If we need to replace an element with the new top 5 member
				if (top5.size() == 5) {
					top5.remove(minBigram);
				}
				// New lowest member of the top 5
				min = my_map.get(bigram);
				minBigram = bigram;
				// Add to set
				top5.add(bigram);
			}
		}
		
		String tostr = "";
		for (String b : top5) {
			tostr += (b + " ");
		}
		my_value.set(tostr);
		output.collect(key, my_value);
	//} else {
		/*String tostr = "ERROR (len=" + String.valueOf(delimeter.length) + ")\t";
		for (String b : delimeter) {
			tostr += (b + " ");
		}
		my_value.set(tostr);
		output.collect(key, my_value);		*/
	//}
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

