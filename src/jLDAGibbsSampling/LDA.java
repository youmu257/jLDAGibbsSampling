package jLDAGibbsSampling;

import java.io.BufferedReader;
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
	// K_alpha is K * alpha
	private double K_alpha;
	// V_beta is V * beta
	private double V_beta;
	// z is topic of word in document
	word_class[][] z;
	// ntw is topic-word dice(words in topic)
	private int[][] ntw;
	// ndt is doc-topic dice(topic in document)
	private int[][] ndt;
	// ntwSum is sum of word in topic
	private int[] ntwSum;
	//nwtSum is sum of word in document
	private int[] ndtSum;
	//theta is doc-topic dice
	private double[][] theta;
	//phi is topic-word dice
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
		this.V_beta = this.beta * V;
		this.K_alpha = this.alpha * K;
		ntw = new int[K][V];
		ndt = new int[M][K];
		ntwSum = new int[K];
		ndtSum = new int[M];
		theta = new double[M][K];
		phi = new double[K][V];
	}
	
	public void BuildMatrix(Document doc)
	{
		long stime = System.currentTimeMillis();
		//random initial topic of word
		int doc_flag = 0;
		z = new word_class[doc.wordInDocument.size()][];
		for(String[] word_in_doc : doc.wordInDocument)
		{
			ArrayList<word_class> tmp_z = new ArrayList<word_class>();
			for(String word : word_in_doc)
			{
				int random_topic = (int)(Math.random()*K);
				word_class new_word = new word_class(doc.distinct_words.indexOf(word), random_topic);
				tmp_z.add(new_word);
				
				ntw[new_word.topic][new_word.word_id]++;
				ndt[doc_flag][new_word.topic]++;
				ntwSum[new_word.topic]++;
				ndtSum[doc_flag]++;
			}
			z[doc_flag] = tmp_z.toArray(new word_class[0]);
			doc_flag++;
		}
		System.out.println("Building Matrix Part 1 done! Spend " + (System.currentTimeMillis()-stime)/1000 + "s");
		stime = System.currentTimeMillis();
	}

	public void ModelInference()
	{
		long stime = System.currentTimeMillis();
		//Running model inference until iteration finish
		for(int iter = 0; iter < this.iteration; iter++)
		{
			for(int doc_index = 0; doc_index < this.M; doc_index++)
			{
				for(int word_index = 0;word_index < z[doc_index].length; word_index++)
				{
					 samplingNewTopic(doc_index, word_index);
				}
			}
			if(iter%100==0){
				System.out.println("iteration "+iter + " sepnd " +(System.currentTimeMillis()-stime)/1000 + " s");
				stime = System.currentTimeMillis();
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
		int origin_topic = z[m][n].topic;
		int wid = z[m][n].word_id;
		
		UpdateNormalCounter(wid, m, origin_topic, -1);

		double[] p = new double[K];
		for(int topic_index = 0; topic_index < K ; topic_index++)
		{
			//_theta are represented co-occurrence influences
			double _theta = (ndt[m][topic_index] + alpha) / (ndtSum[m] + K_alpha);
			//_phi are represented the probability that a word will appear under each topic 
			double _phi = (ntw[topic_index][wid] + beta) / (ntwSum[topic_index] + V_beta);
			p[topic_index] = _theta * _phi ;
		}
		
		int newtopic = this.sampleMultinomial(p);
		//update matrix by new topic
		z[m][n].topic = newtopic;
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
				theta[doc_index][topic_index] = (ndt[doc_index][topic_index] + alpha) / (ndtSum[doc_index] + K_alpha);
	}
	
	public void updatePhi()
	{
		for(int topic_index = 0; topic_index < K; topic_index++)
			for(int word_index = 0;word_index < V; word_index++)
				phi[topic_index][word_index] = (ntw[topic_index][word_index] + beta) / (ntwSum[topic_index] + V_beta);
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
	 * input word_id and return word-topic vector by phi
	 */
	public double[] getWordTopic(int word_id)
	{
		double[] prob = new double[K];
		for(int topic_index = 0; topic_index < K ;topic_index++)
			prob[topic_index] = phi[topic_index][word_id];
		return prob;
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
		
		bw = IO.Writer(path_result + "distinct_word_list.txt");
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<doc.distinct_words.size(); i++)
		{
			sb.append(doc.distinct_words.get(i)).append("\t");
			if(i%200==0){
				bw.write(sb.toString());
				sb = new StringBuilder();
			}
		}
		bw.write(sb.toString());
		bw.close();
	}
	
	public void ReadModel(String path, Document doc) throws IOException
	{
		BufferedReader br = IO.Reader(path + "parameter.txt");
		this.alpha = Double.parseDouble(br.readLine().split(":")[1]);
		this.beta = Double.parseDouble(br.readLine().split(":")[1]);
		this.K = Integer.parseInt(br.readLine().split(":")[1]);
		this.iteration = Integer.parseInt(br.readLine().split(":")[1]);
		this.setTopicSize(K);
		this.V = doc.distinct_words.size();
		br.close();
		
		//read phi
		br = IO.Reader(path + "phi.txt");
		phi = new double[this.K][doc.distinct_words.size()];
		String lin = "";
		int topic_index = 0;
		while((lin = br.readLine()) != null)
		{
			String[] _phi = lin.split("\t");
			for(int word_index=0; word_index<_phi.length; word_index++)
				phi[topic_index][word_index] = Double.parseDouble(_phi[word_index]);
			topic_index++;
		}
		br.close();
	}
	
	public void TestingDocument(String output_path, Document doc) throws IOException
	{
		this.M = doc.wordInDocument.size();
		int[][] _theta = new int[this.M][this.K];
		int[] thetaSum = new int[this.M];
		int doc_index = 0;
		//predicting word topic
		for(String[] review : doc.wordInDocument)
		{
			for(String word : review)
			{
				int word_id = doc.distinct_words.indexOf(word);
				if(word_id < this.V)
				{
					double[] tmp = getWordTopic(word_id);
					int predWordTopic = this.sampleMultinomial( tmp);
					_theta[doc_index][predWordTopic]++;
				}else{
					//if model not contain this word, random select topic
					_theta[doc_index][(int)(Math.random()*this.K)]++;
				}
				thetaSum[doc_index]++;
				
			}
			doc_index++;
		}
		
		BufferedWriter bw = IO.Writer(output_path);
		for(doc_index = 0; doc_index < this.M; doc_index++)
		{
			StringBuilder sb = new StringBuilder();
			for(int topic_index = 0; topic_index < this.K; topic_index++)
			{
				double pred_theta = (_theta[doc_index][topic_index] + this.alpha) / (thetaSum[doc_index] + K * this.alpha);
				sb.append(MyJavaUtil.round(pred_theta, 5)).append("\t");
			}
			sb.append("\n");
			bw.write(sb.toString());
		}
		bw.close();
	}
}
