package jLDAGibbsSampling;

import java.io.IOException;

import org.tartarus.snowball.ext.englishStemmer;

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
		lda.printWordInTopic(10);
		lda.writeResult(path_output, 10);
	}
}
