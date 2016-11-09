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
		 * If you need to reset parameter.
		 * Using lda.setParameter(alpha, beta, topicSize, iteration) to reset.
		 */
		lda.setParameter(0.5, 0.1, 10, 100);
		
		lda.LDA_Gibbs_Sampling(doc);
		lda.printWordInTopic(10);
		lda.writeResult(path_output, 10);
	}
}
