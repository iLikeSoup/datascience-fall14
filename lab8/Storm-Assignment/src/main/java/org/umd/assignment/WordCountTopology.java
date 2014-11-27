/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.umd.assignment;

import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.FileReader;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.task.ShellBolt;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import org.umd.assignment.spout.RandomSentenceSpout;
import org.umd.assignment.spout.TwitterSampleSpout;

import java.util.*;

/**
 * This topology demonstrates Storm's stream groupings and multilang capabilities.
 */
public class WordCountTopology {

	private static int threadNum = 0;

  public static class SplitSentence extends ShellBolt implements IRichBolt {

    public SplitSentence() {
      super("python", "splitsentence.py");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields("word"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
      return null;
    }
  }

  public static class WordCount extends BaseBasicBolt {
    Map<String, Integer> counts = new HashMap<String, Integer>();

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
      
		// ----------------- Task 2	---------------------------------
		//
		//
		//	Modify this code to exclude stop-words from counting.
		//  Stopword list is provided in the lab folder. 
		//
		//
		// ---------------------------------------------------------


    	
		String word = tuple.getString(0);
    	try {
			Scanner scan = new Scanner(new FileReader("/home/terrapin/myGithub/datascience-fall14/lab8/Stopwords.txt"));
	    	while (scan.hasNext()) {
	    		if (word.equals(scan.next())) {
	    			return;
	    		}
	    	}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Integer count = counts.get(word);
		if (count == null)
			count = 0;
		count++;
		counts.put(word, count);
		collector.emit(new Values(word, count));


    }

	// code taken from http://www.mkyong.com/java/how-to-sort-a-map-in-java/
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}


	@Override
	public void cleanup()
	{
		// ------------------------  Task 3 ---------------------------------------
		//
		//
		//	This function gets called when the Stream processing finishes.
		//	MODIFY this function to print the most frequent words that co-occur 
		//	with Obama [The TwitterSimpleSpout already gives you Tweets that contain
		//  the word obama].
		//
		//	Since multiple threads will be doing the same cleanup operation, writing the
		//	output to a file might not work as desired. One way to do this would be
		//  print the output (using System.out.println) and do a grep/awk/sed on that.
		//
		//--------------------------------------------------------------------------
		int counter = 0;
		Map<String, Integer> sortedMap = sortByComparator(counts);

		System.out.println("myoutput: Thread " + threadNum++);
		for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
			if (counter > 9) {
				break;
			}
			System.out.println("myoutput(" + counter++ + "):\t" + entry.getKey() + " --- " + entry.getValue());
		}
	}

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields("word", "count"));
    }
  }

  public static void main(String[] args) throws Exception {

    TopologyBuilder builder = new TopologyBuilder();

	// ---------------------------- Task 1 -------------------------------------
	//
	//		You need to use TwitterSampleSpout() for the assignemt. But, it won't work
	//		unless you set up the access token correctly in the TwitterSampleSpout.java
	//
	//		RandomSentenceSpout() simply spits out a random sentence. 
	//
	//--------------------------------------------------------------------------

	// Setting up a spout
    builder.setSpout("spout", new TwitterSampleSpout(), 3);

	// Setting up bolts
    builder.setBolt("split", new SplitSentence(), 3).shuffleGrouping("spout");
    builder.setBolt("count", new WordCount(), 3).fieldsGrouping("split", new Fields("word"));

    Config conf = new Config();
    conf.setDebug(true);


    if (args != null && args.length > 0) {
      conf.setNumWorkers(3);

      StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
    }
    else {
      conf.setMaxTaskParallelism(3);

      LocalCluster cluster = new LocalCluster();
      cluster.submitTopology("word-count", conf, builder.createTopology());

	  // --------------------------- Task 4 ---------------------------------
	  //
	  //	The sleep time simply indicates for how long you want to keep your
	  //	system up and running. 10000 (miliseconds) here means 10 seconds.
	  // 	
	  //
	  // ----------------------------------------------------------------------
      Thread.sleep(10000);
      cluster.shutdown(); // blot "cleanup" function is called when cluster is shutdown (only works in local mode)
    }
  }
}
