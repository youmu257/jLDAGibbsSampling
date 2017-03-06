package jLDAGibbsSampling;

import java.io.IOException;

import org.tartarus.snowball.ext.englishStemmer;

public class Test {
	private static String path = "data//TestData//";
	private static String path_output = "data//Result//";
	
	public static void main(String[] args) throws IOException {
		//read model(distinct_word_list.txt, phi.txt and parameter.txt)
		Document doc = new Document();
		doc.ReadDistinctWordList(path_output + "distinct_word_list.txt");
		
		LDA lda_testing = new LDA();
		lda_testing.ReadModel(path_output, doc);
		
		//input testing data
		long stime = System.currentTimeMillis();
		
		doc.setStemming(new englishStemmer());
		doc.ReadCorpus(path);
		System.out.println("Read Data Spend "+(System.currentTimeMillis()-stime)/1000+" s");
		/////////////////////////////////////////////////////////////////////////////////
		lda_testing.TestingDocument(path_output+"pred_theta.txt", doc);
	}
}
