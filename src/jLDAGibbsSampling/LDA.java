package jLDAGibbsSampling;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import util.IO;
import util.MyJavaUtil;


class word_class{
	int word_id;
	int topic;
	public word_class(int id, int topic){
		this.word_id = id;
		this.topic = topic;
	}
}

public class LDA extends TopicModel{
	// K is topic size
	private int K = this.topicSize;
	// V is number of distinct words
	private int V;
	// M is number of document
	private int M;
	// z is topic of word in document
	ArrayList<ArrayList<word_class>> z = new ArrayList<ArrayList<word_class>>();
	// ntw is topic-word dice(words in topic)
	private int[][] ntw;
	// ndt is doc-topic dice(topic in document)
	private int[][] ndt;
	// ntwSum is sum of word in topic
	private int[] ntwSum;
	//nwtSum is sum of word in document
	private int[] ndtSum;
	//theta in all document
	private double[][] theta;
	//phi in all document
	private double[][] phi;
	//global variable
	public Document doc;
	
	public void LDA_Gibbs_Sampling(Document doc)
	{
		this.doc = doc;
		InitParameter(doc.distinct_words.size(), doc.wordInDocument.size());
		BuildMatrix(doc);
		ModelInference();
		
		// update theta and phi
		updateTheta();
		updatePhi();
	}
	
	public void InitParameter(int V, int M){
		this.V = V;
		this.M = M;
		ntw = new int[K][V];
		ndt = new int[M][K];
		ntwSum = new int[K];
		ndtSum = new int[M];
		theta = new double[M][K];
		phi = new double[K][V];
	}
	
	public void BuildMatrix(Document doc)
	{
		//random initial topic of word
		for(String[] word_in_doc : doc.wordInDocument)
		{
			ArrayList<word_class> tmp_z = new ArrayList<word_class>();
			for(String word : word_in_doc)
			{
				int random_topic = (int)(Math.random()*K);
				word_class new_word = new word_class(doc.distinct_words.indexOf(word), random_topic);
				tmp_z.add(new_word);
			}
			z.add(tmp_z);
		}
		
		//building word-topic matrix and doc-topic matrix
		for(int doc_index = 0; doc_index < M ; doc_index++)
		{
			for(word_class tmp_w : z.get(doc_index))
			{
				ntw[tmp_w.topic][tmp_w.word_id]++;
				ndt[doc_index][tmp_w.topic]++;
				ntwSum[tmp_w.topic]++;
			}
			ndtSum[doc_index] = z.get(doc_index).size();
		}
	}

	public void ModelInference()
	{
		//Running model inference until iteration finish
		for(int iter = 0; iter < this.iteration; iter++)
		{
			for(int doc_index = 0; doc_index < this.M; doc_index++)
			{
				for(int word_index = 0;word_index < z.get(doc_index).size(); word_index++)
				{
					 int newtopic = samplingNewTopic(doc_index, word_index);//z.get(doc_index).get(word_index).word_id
					 z.get(doc_index).get(word_index).topic = newtopic;
				}
			}
		}
	}
	
	/**
	 * @param m : document id
	 * @param n : word position in document(not word id)
	 * @return new topic
	 */
	public int samplingNewTopic(int m, int n)
	{
		//remove word in document now
		int origin_topic = z.get(m).get(n).topic;

		--ntw[origin_topic][n];
		--ndt[m][origin_topic];
		--ntwSum[origin_topic];
		--ndtSum[m];
		
		double[] p = new double[K];
		for(int topic_index = 0; topic_index < K ; topic_index++)
		{
			//_theta are represented co-occurrence influences
			double _theta = (ndt[m][topic_index] + this.alpha) / (ndtSum[m] + K * this.alpha);
			//_phi are represented the probability that a word will appear under each topic 
			double _phi = (ntw[topic_index][z.get(m).get(n).word_id] + this.beta) / (ntwSum[topic_index] + V * this.beta);
			p[topic_index] = _theta * _phi ;
		}
		
		int newtopic = this.sampleMultinomial(p);
		//update matrix by new topic
		++ntw[newtopic][z.get(m).get(n).word_id];
		++ndt[m][newtopic];
		++ntwSum[newtopic];
		++ndtSum[m];
		
		return newtopic;
	}
	
	public void updateTheta()
	{
		for(int doc_index = 0; doc_index < M; doc_index++)
			for(int topic_index = 0;topic_index < K; topic_index++)
				theta[doc_index][topic_index] = (ndt[doc_index][topic_index] + this.alpha) / (ndtSum[doc_index] + K * this.alpha);
	}
	
	public void updatePhi()
	{
		for(int topic_index = 0; topic_index < K; topic_index++)
			for(int word_index = 0;word_index < V; word_index++)
				phi[topic_index][word_index] = (ntw[topic_index][word_index] + this.beta) / (ntwSum[topic_index] + V * this.beta);
	}
	
	/**
	 * Print top k word in each topic
	 * @param top : top k number 
	 */
	public void printWordInTopic(int top)
	{
		for(int topic_index = 0; topic_index < phi.length; topic_index++)	
		{	
			System.out.print(topic_index+":\t");
			Map<String,Double> tmp_map = new HashMap<String,Double>();
			for(int word_index = 0; word_index < phi[topic_index].length; word_index++)
			{
				tmp_map.put(doc.distinct_words.get(word_index), phi[topic_index][word_index]);
			}
			LinkedHashMap<String, Double> tmp = new LinkedHashMap<String, Double>();
			tmp.putAll(MyJavaUtil.sortByComparatorDouble(tmp_map));
			
			int flag = 1;
			for(Map.Entry<String, Double> e : tmp.entrySet())
			{
				System.out.print(e.getKey()+"\t");//+","+e.getValue()
				flag++;
				if(flag>top) break;
			}
			System.out.println();
		}
	}
	
	/**
	 * Write phi matrix, theta matrix and top k topic words
	 * @param path_result : output folder path
	 * @param top : top k number
	 */
	public void writeResult(String path_result, int top) throws IOException
	{
		IO.mkdir(path_result);
		//write phi matrix
		BufferedWriter bw = IO.Writer(path_result + "phi.txt");
		for(int topic_index = 0; topic_index < phi.length; topic_index++)
		{
			StringBuilder sb = new StringBuilder();
			for(int word_index = 0; word_index < phi[topic_index].length; word_index++)
			{
				sb.append(phi[topic_index][word_index]+"\t");
			}
			bw.write(sb.toString()+"\n");
		}
		bw.close();
		
		//write theta matrix
		bw = IO.Writer(path_result + "theta.txt");
		for(int doc_index = 0; doc_index < theta.length; doc_index++)
		{
			StringBuilder sb = new StringBuilder();
			for(int topic_index = 0; topic_index < theta[doc_index].length; topic_index++)
			{
				sb.append(theta[doc_index][topic_index]+"\t");
			}
			bw.write(sb.toString()+"\n");
		}
		bw.close();
		
		//write word in each topic
		bw = IO.Writer(path_result + "result.txt");
		for(int topic_index = 0; topic_index < phi.length; topic_index++)	
		{	
			bw.write(topic_index+":\t");
			Map<String,Double> tmp_map = new HashMap<String,Double>();
			for(int word_index = 0; word_index < phi[topic_index].length; word_index++)
			{
				tmp_map.put(doc.distinct_words.get(word_index), phi[topic_index][word_index]);
			}
			LinkedHashMap<String, Double> tmp = new LinkedHashMap<String, Double>();
			tmp.putAll(MyJavaUtil.sortByComparatorDouble(tmp_map));
			
			int flag = 1;
			for(Map.Entry<String, Double> e : tmp.entrySet())
			{
				bw.write(e.getKey()+"\t");//+","+e.getValue()
				flag++;
				if(flag>top) break;
			}
			bw.write("\n");
		}
		bw.close();
	}
}
