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
	private int K;
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
	//Cumulative Topic Result in each last 50 iteration
	private HashMap<Integer, HashMap<String,Integer>> CumulativeTopicResult = new HashMap<Integer,HashMap<String,Integer>>();
	
	public void LDA_Gibbs_Sampling(Document doc)
	{
		this.doc = doc;
		InitParameter(doc.distinct_words.size(), doc.wordInDocument.size());
		long stime = System.currentTimeMillis();
		BuildMatrix(doc);
		ModelInference();
		System.out.println("LDA done!\nSpend : "+(System.currentTimeMillis()-stime)/1000+" s");
		
		// update theta and phi
		updateTheta();
		updatePhi();
	}
	
	public void InitParameter(int V, int M){
		this.V = V;
		this.M = M;
		this.K = this.topicSize;
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
					 samplingNewTopic(doc_index, word_index);
				}
			}
			
			//cumulative last 50 topic result to count that can reduce random sampling influence.
			if(iter >= this.iteration-50)
				updateCumulativeTopicResult();
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
		int wid = z.get(m).get(n).word_id;
		
		UpdateNormalCounter(wid, m, origin_topic, -1);

		double[] p = new double[K];
		for(int topic_index = 0; topic_index < K ; topic_index++)
		{
			//_theta are represented co-occurrence influences
			double _theta = (ndt[m][topic_index] + this.alpha) / (ndtSum[m] + K * this.alpha);
			//_phi are represented the probability that a word will appear under each topic 
			double _phi = (ntw[topic_index][wid] + this.beta) / (ntwSum[topic_index] + V * this.beta);
			p[topic_index] = _theta * _phi ;
		}
		
		int newtopic = this.sampleMultinomial(p);
		//update matrix by new topic
		z.get(m).get(n).topic = newtopic;
		UpdateNormalCounter(wid, m, newtopic, 1);
		
		return newtopic;
	}
	
	public void UpdateNormalCounter(int word_id, int doc_id, int topic, int flag)
	{
		ntw[topic][word_id] += flag;
		ndt[doc_id][topic] += flag;
		ntwSum[topic] += flag;
		ndtSum[doc_id] += flag;
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
	 * cumulative topic result to count that can reduce random sampling influence.
	 */
	public void updateCumulativeTopicResult()
	{
		updatePhi();
		for(int topic_index = 0; topic_index < phi.length; topic_index++)	
		{	
			if(!CumulativeTopicResult.containsKey(topic_index))
				CumulativeTopicResult.put(topic_index, new HashMap<String,Integer>());
			Map<String,Double> tmp_map = new HashMap<String,Double>();
			for(int word_index = 0; word_index < phi[topic_index].length; word_index++)
			{
				tmp_map.put(doc.distinct_words.get(word_index), phi[topic_index][word_index]);
			}
			LinkedHashMap<String, Double> tmp = new LinkedHashMap<String, Double>();
			tmp.putAll(MyJavaUtil.sortByComparatorDouble(tmp_map));
			
			//only cumulative top 20
			int flag = 0;
			for(Map.Entry<String, Double> e : tmp.entrySet())
			{
				String key = e.getKey();
				if(CumulativeTopicResult.get(topic_index).containsKey(key))
					CumulativeTopicResult.get(topic_index).put(key, CumulativeTopicResult.get(topic_index).get(key)+1);
				else
					CumulativeTopicResult.get(topic_index).put(key, 1);

				flag++;
				if(flag>20) break;
			}
		}
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
		System.out.println("-----------------------------------------------------------------------");
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
				sb.append(MyJavaUtil.round(phi[topic_index][word_index], 5)+"\t");
			}
			bw.write(sb.toString()+"\n");
		}
		bw.close();
		
		//write theta matrix
		bw = IO.Writer(path_result + "theta.txt");
		for(int doc_index = 0; doc_index < theta.length; doc_index++)
		{
			StringBuilder sb = new StringBuilder();
			for(int topic_index = 0; topic_index < K; topic_index++)
			{
				sb.append(MyJavaUtil.round(theta[doc_index][topic_index], 5)+"\t");
			}
			bw.write(sb.toString()+"\n");
		}
		bw.close();
		
		//write parameter matrix
		bw = IO.Writer(path_result + "parameter.txt");
		bw.write("alpha:"+alpha+"\n");
		bw.write("beta:"+beta+"\n");
		bw.write("topicSize:"+topicSize+"\n");
		bw.write("iteration:"+iteration+"\n"); 
		bw.close();
		
		//write word in each topic in last iteration
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
		
		//write word in each topic over last 50 iteration
		bw = IO.Writer(path_result + "CumulativeTopicResult.txt");
		for(int topic_index = 0; topic_index < CumulativeTopicResult.size(); topic_index++)	
		{	
			LinkedHashMap<String, Integer> tmp = new LinkedHashMap<String, Integer>();
			tmp.putAll(MyJavaUtil.sortByComparatorInt(CumulativeTopicResult.get(topic_index)));
		
			bw.write(topic_index+":\t");
			int flag = 1;
			for(Map.Entry<String, Integer> e: tmp.entrySet())
			{
				bw.write(e.getKey()+"\t");
				flag++;
				if(flag>top) break;
			}
			bw.write("\n");
		}
		
		bw.close();
	}
	
}
