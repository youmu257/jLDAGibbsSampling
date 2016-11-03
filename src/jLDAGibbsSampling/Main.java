package jLDAGibbsSampling;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.tartarus.snowball.ext.englishStemmer;

import util.IO;

public class Main {
	private static String path = "data//DataInput//";
	private static String path_output = "data//Result//";
	public static void main(String[] args) throws IOException
	{
		//input data
		Document doc = new Document();
		doc.setStemming(new englishStemmer());
		doc.ReadCorpus(path);
		/////////////////////////////////////////////////////////////////////////////////
		//LDA gibbs sampling
		LDA lda = new LDA();
		/**
		 * If you change parameter in file, e.g alpha, beta, topicSize, iteration.
		 * You need to reload new parameter.
		 * Using lda.readParameter("data//parameter.txt") to reload.
		 */
		lda.readParameter("data//Parameter//parameter.txt");
		
		lda.LDA_Gibbs_Sampling(doc);
		lda.printWordInTopic();
	}
	
	public static void writePreprocessing(Document doc) throws IOException
	{
		//test
		BufferedWriter bw = IO.Writer(path_output+"output_wordInDocument.txt");
		int doc_id = 0;
		for(String[] tmp_arr: doc.wordInDocument)
		{
			bw.write(doc_id+" : \n");
			for(String w : tmp_arr){
				bw.write(w+" , ");
			}
			bw.write("\n");
			doc_id++;
		}
		bw.close();
		
		//word frequency
		bw = IO.Writer(path_output+"output_word_frequency.txt");
		HashMap<String, Integer> map = new HashMap<String,Integer>();
		for(String[] tmp_arr: doc.wordInDocument)
		{
			for(String w : tmp_arr){
				if(map.containsKey(w)) map.put(w, map.get(w)+1);
				else map.put(w,1);
			}
		}
		for(Map.Entry<String,Integer> e : map.entrySet())
			bw.write(e.getKey()+"\t"+e.getValue()+"\n");
		bw.close();
	}
}
