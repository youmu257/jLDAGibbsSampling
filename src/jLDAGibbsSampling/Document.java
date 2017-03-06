package jLDAGibbsSampling;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.tartarus.snowball.SnowballStemmer;
import util.IO;

public class Document {
	private String data_path = "Data//";
	//Stopword list
	private ArrayList<String> stopword = new ArrayList<String>();
	//Distinct words in all document (word : word_id)
	public ArrayList<String> distinct_words = new ArrayList<String>();
	//Contain all words in each document(*document *word)
	public ArrayList<String[]> wordInDocument = new ArrayList<String[]>();
	//Snowball Stemming
	public SnowballStemmer stemmer = null;
	
	public void setStemming(SnowballStemmer stemm)
	{
		this.stemmer = stemm;
	}
	
	public void readStopWord() throws IOException
	{
		BufferedReader br = IO.Reader(data_path + "stopword.txt");
		String lin = "";
		while((lin = br.readLine()) != null){
			stopword.add(lin);
		}
		br.close();
		
		stopword.add("ax");
		stopword.add("re");
		stopword.add("am");
		stopword.add("im");
		stopword.add("dont");
	}
	
	public String filtering(String input_str)
	{
		input_str = input_str.toLowerCase().trim();
		
		// Stemming
		if (stemmer != null) {
			stemmer.setCurrent(input_str);
			stemmer.stem();
			input_str = stemmer.getCurrent();
		}
		
		//Filtering email
		String email_regex = "^(.+)@(.+)$";
		input_str = input_str.replaceAll(email_regex, "");
		
		//Filtering url
		String url_regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		input_str = input_str.replaceAll(url_regex, "");
		
		//remove all punctuation
		input_str = input_str.replaceAll("[^a-zA-Z0-9]", "");
		
		//remove only number
		String number_regex = "^[0-9]+$";
		input_str = input_str.replaceAll(number_regex, "");
		
		//remove stopword
		if(stopword.contains(input_str))
			return "";
		
		return input_str;
	}
	
	/**
	 * Read all document. Then setting wordset and wordInDocument array.
	 * @param directory : folder path of documents
	 */
	public void ReadCorpus(String dir_path) throws IOException
	{
		readStopWord();
		
		File dir = new File(dir_path);
        for (File file : dir.listFiles())
        {
        	BufferedReader br = IO.Reader(file.getPath());
        	ArrayList<String> tmp_arr = new ArrayList<String>();
        	String lin = "";
        	while((lin = br.readLine()) != null)
        	{
        		lin = lin.replaceAll("[#(\\[<>\\])]", " ");
        		for(String word_origin : lin.split(" "))
        		{
        			String word = filtering(word_origin);

        			if(word.length() == 0)
        				continue;
        			else if(!distinct_words.contains(word))
        				distinct_words.add(word);
        			tmp_arr.add(word);
        		}
        	}
        	br.close();
        	
        	wordInDocument.add(tmp_arr.toArray(new String[0]));
        }
	}

	public void ReadDistinctWordList(String path) throws IOException
	{
		BufferedReader br = IO.Reader(path);
		distinct_words = new ArrayList<String>(Arrays.asList(br.readLine().split("\t")));
		br.close();
	}
	
}
