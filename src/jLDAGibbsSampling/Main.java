package jLDAGibbsSampling;

import java.io.IOException;

import org.tartarus.snowball.ext.englishStemmer;

public class Main {
	private static String path = "data//DataInput//";
	private static String path_output = "data//Result//";
	private static int topci_num = 5;
	
	public static void main(String[] args) throws IOException
	{
		//input data
		long stime = System.currentTimeMillis();
		Document doc = new Document();
		doc.setStemming(new englishStemmer());
		doc.ReadCorpus(path);
		System.out.println("Read Data Spend "+(System.currentTimeMillis()-stime)/1000+" s");
		/////////////////////////////////////////////////////////////////////////////////
		//LDA gibbs sampling
		LDA lda = new LDA();
		/**
		 * If you need to reset parameter.
		 * Using lda.setParameter(alpha, beta, topicSize, iteration) to reset.
		 */
		lda.setParameter(50.0/topci_num, 0.1, topci_num, 1000);
		lda.LDA_Gibbs_Sampling(doc);
		lda.printWordInTopic(10);
		lda.writeResult(path_output, 10);

	}
}
