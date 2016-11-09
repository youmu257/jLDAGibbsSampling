# jLDAGibbsSampling
Latent Dirichlet allocation(LDA) is a unsupersived machine learning mathod that distil topic of word in training data.

#Data
  Dataset download from [UCI Twenty Newsgroups](https://archive.ics.uci.edu/ml/datasets/Twenty+Newsgroups)
  Select five categories and four news in each category.
  Each file represents a document.
  
  Described as follows:
  * rec.motorcycles : 102616, 104312, 104630, 104582
  * rec.sport.baseball : 104343, 102591, 101666, 104541
  * comp.sys.ibm.pc.hardware : 58862, 60758, 60207, 60172
  * sci.med : 59285, 59122, 59283, 59286
  * talk.politics.guns : 54684, 54380, 54215, 53296
		
#Data Preprocessing
   1. Stopword list download from [[link](http://www.lextek.com/manuals/onix/stopwords1.html)].
   This stopword list is probably the most widely used stopword list. 
   It covers a wide number of stopwords without getting too aggressive and including too many words which a user might search upon. 
   This wordlist contains 429 words.
   
   2. Stemming using [Snowball Stemmer](http://snowball.tartarus.org/download.html) java version
   
#Reference
  1. [David M. Blei Blog for Topic Modeling](http://www.cs.columbia.edu/~blei/topicmodeling.html) .
  2. [Liu Yang's LDAGibbsSampling](https://github.com/yangliuy/LDAGibbsSampling) .
  3. Blei, David M., Ng, Andrew Y., and Jordan, Michael I. 2003. Latent dirichlet allocation. Journal of Machine Learning Research. 3 (Mar. 2003), 993-1022[[pdf](http://www.cs.princeton.edu/picasso/mats/BleiNgJordan2003_blei.pdf)] .

 