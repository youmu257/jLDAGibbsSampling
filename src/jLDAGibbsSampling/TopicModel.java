package jLDAGibbsSampling;

import java.io.BufferedReader;
import java.io.IOException;

import util.IO;

abstract class TopicModel {
	//Default parameter
	protected double alpha = 0.1;
	protected double beta = 0.01;
	protected int topicSize = 10;
	protected int iteration = 100;
	
	//Setting alpha parameter which affects topic selection
	public void setAlpha(double alpha){
		this.alpha = alpha;
	}
	
	//Setting beta parameter which affects word selection in topic
	public void setBeta(double beta){
		this.beta = beta;
	}
	
	//To adjust topic number
	public void setTopicSize(int topicSize){
		this.topicSize = topicSize;
	}
	
	//Change iteration in topic model
	public void setIteration(int iteration){
		this.iteration = iteration;
	}
	
	public double getAlpha(){
		return alpha;
	}
	
	public double getBeta(){
		return beta;
	}
	
	public int getTopicSize(){
		return topicSize;
	}
	
	public int getIteration(){
		return iteration;
	}
	
	/**
	 * Sampling a new topic
	 * @param p[] : is word's probability in each topic 
	 * @return newTopic : topic id
	 */
	public int sampleMultinomial(double[] p){
		//Cumulative probability
		for(int topic_index = 1; topic_index < topicSize; topic_index++)
			p[topic_index] += p[topic_index-1];
		
		double threshold = Math.random()*p[topicSize-1];
		int newTopic = 0;
		for(;newTopic < topicSize; newTopic++)
			if(threshold < p[newTopic])
				break;
		if(newTopic == topicSize)
			newTopic--;
		return newTopic;
	}
	
	
	public void readParameter(String path) throws IOException
	{
		BufferedReader br = IO.Reader(path);
		String lin ="";
		while((lin = br.readLine()) != null)
		{
			String[] parameter = lin.split(":");
			if(parameter[0].compareTo("alpha") == 0){
				this.alpha = Double.parseDouble(parameter[1]);
			}
			else if(parameter[0].compareTo("beta") == 0){
				this.beta = Double.parseDouble(parameter[1]);
			}
			else if(parameter[0].compareTo("topicSize") == 0){
				this.topicSize = Integer.parseInt(parameter[1]);
			}
			else if(parameter[0].compareTo("iteration") == 0){
				this.iteration = Integer.parseInt(parameter[1]);
			}
		}
		br.close();
	}
}
