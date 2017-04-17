package jLDAGibbsSampling;

import java.io.IOException;

import org.tartarus.snowball.ext.englishStemmer;

public class Main {
	private static String path = "data//DataInput//";
	private static String path_output = "data//Result//";
	private static int topci_num = 3;
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("LDA start");
		//input data
		long stime = System.currentTimeMillis();
		Document doc = new Document();
		doc.setStemming(new englishStemmer());
//		doc.ReadCorpus(path);
		doc.ReadCorpusChinese("data//chinese//");
		System.out.println("Read Data Spend "+(System.currentTimeMillis()-stime)/1000+" s");
		/////////////////////////////////////////////////////////////////////////////////
		//LDA gibbs sampling
		LDA lda = new LDA();
		/**
		 * If you need to reset parameter.
		 * Using lda.setParameter(alpha, beta, topicSize, iteration) to reset.
		 */
		lda.setParameter(50.0/topci_num, 0.01, topci_num, 1000, 50);
		lda.LDA_Gibbs_Sampling(doc);
		lda.printWordInTopic(30);
		lda.writeResult(path_output, 10);

	}
}
