package jLDAGibbsSampling;

abstract class TopicModel {
	//Default parameter
	protected double alpha = 0.1;
	protected double beta = 0.01;
	protected int topicSize = 10;
	protected int iteration = 100;
	protected int estimate_iteration = 25;
	
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
		//Cumulative probability, easy to sampling 
		for(int topic_index = 1; topic_index < topicSize; topic_index++)
			p[topic_index] += p[topic_index-1];
		
		double threshold = Math.random()*p[topicSize-1];//sampling
		int newTopic = 0;
		for(;newTopic < topicSize; newTopic++)
			if(threshold < p[newTopic])
				break;
		if(newTopic==p.length)
			newTopic--;
		return newTopic;
	}
	
	public void setParameter(double alpha, double beta, int topicSize, int iteration, int estimate_iteration)
	{
		this.alpha = alpha;
		this.beta = beta;
		this.topicSize = topicSize;
		this.iteration = iteration;
		this.estimate_iteration = estimate_iteration;
	}
}
