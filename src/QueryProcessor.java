
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class QueryProcessor{
	public static ArrayList<String> queryTerm;
	static HashMap<String, ArrayList<Double>> queryTermWeight;
	static ArrayList<Double> queryWeightArray;
	static ArrayList<String> listOfTerms;
	static ArrayList<String> termsInQuery;
	static HashMap<String, Integer> QTF;
	static Float Length[];
	static Double Score[];
	static protected HashMap<String,Integer> docListMapToInt;
	static protected HashMap<Integer, String> docIndexMapToString;
	static int docId1;
	static ArrayList<LinkedHashMap<String, Integer>> postingsTupleList=null;
	static HashMap<String, Double> DocScore;
	static HashMap<String, Float> termWeight;
	static WordIndex wordInd;
	static BiWordIndex biWord;
	static ArrayList<String> QueryBiWords;
	static HashMap<String, Integer> topTerms;
	static ArrayList<finalList> topK = new ArrayList<>();
	
	/* Constructor which takes query, # of documents to show and the path to the folder as input 
	 * It calls the buildIndex form wordIndex and BiWordIndex class.
	 */
	public QueryProcessor(String query, Integer k, String path) {

		String folderPath = path;
		File folder = new File(folderPath);
		wordInd=new WordIndex(folderPath);
		biWord = new BiWordIndex(folderPath);
		wordInd.buildIndex();
		biWord.buildIndex();
		docListMapToInt= new HashMap<String,Integer>();
		docIndexMapToString = new HashMap<Integer,String>();
		
		/* Pre-process the query */
		ProcessQuery(query);
		
		int N = wordInd.totalNoDocs;
		ArrayList<String> termsInDoc = null; 
		int docId=0;
		
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles==null){
			System.out.println("folder path is incorrect!!");
			return;
		}
		Length = new Float[N];
		Score = new Double[N];
		for (File file : listOfFiles) {
			if (file.isFile() && !file.isHidden()) 		
			{
				termsInDoc = new ArrayList<String>();
				docListMapToInt.put(file.getName(), docId);
				docIndexMapToString.put(docId, file.getName());
				docId++;
				
				BufferedReader br = null;
				try {
					br = new BufferedReader(new FileReader(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
	
				try {
					while (br.ready()) 
					{
						String line=br.readLine();
						line=line.trim().toLowerCase();		// trim change to lower chase
						line=line.replaceAll("\\s+", " ").replaceAll("[:.,;']", "");  //remove punctuation symbol	
						String arr[]=line.split(" ");		// split into array of terms
						for(String word: arr)
						{
							if(word.length()>2 && (word.compareToIgnoreCase("the")!=0))		// as per requirement do not consider these terms
							{
								if(!termsInDoc.contains(word))
									termsInDoc.add(word);		// add to the list of terms for this document
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}				
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		
				/* Store the terms and term weight for a document */
				termWeight = new HashMap<String,Float>();
				float len=0.0f;
				for (String t : termsInDoc) {
					termWeight.put(t, wordInd.weight(t, file.getName()));
				}
				
				for (Float f:termWeight.values()) {
					len += f*f;
				}
				
				float length = (float) Math.sqrt(len);
						
				/* Initialize the Length and Score array */
				Length[getDocId(file.getName())] = length;
				Score[getDocId(file.getName())] =0.0;
			}
			
		}
		
		/* Calculate the score and weight for each postings */
		for (String t: termsInQuery) 
		{
			postingsTupleList=new ArrayList<LinkedHashMap<String, Integer>>();
			postingsTupleList=wordInd.postingsList(t);
			if (postingsTupleList==null) 
			{
				System.out.println("Term not in the documents!");
				continue;
			}
			
			for(LinkedHashMap<String, Integer> tuple: postingsTupleList)
			{
				for (Map.Entry<String, Integer> entry : tuple.entrySet()) 
				{
					String fileName = entry.getKey();
					Integer freq = entry.getValue();
					Float wtTD=wordInd.weight(t, fileName);
					Float wtTQ=weightInQuery(t,query);
					docId1=getDocId(fileName);
					Score[docId1]+=wtTD*wtTQ;
				}
			}
		}
		
		Float sizeVQ = 0.0f, size = 0.0f;
		for (String t: termsInQuery) {
			size += weightInQuery(t, query)*weightInQuery(t, query); 
		}
		sizeVQ = (float) Math.sqrt(size);
		
		for (int i : docListMapToInt.values()) {
			Score[i] = Score[i]/(Length[i]*sizeVQ);
		}
		
		
		/* Update the doc score i.e the hashmap of documents and its corresponding score w.r.t. a query */
		DocScore = new HashMap<String, Double>();
		for (int i : docListMapToInt.values()) {
			DocScore.put(docIndexMapToString.get(i), Score[i]);
		}
		
   	 // biWord.printPostingsList(query);
		
		String[] top2KPages = top2KPages(2*k);
	    
	 // System.out.println("\nTop 2k pages are:");    
     // for(String s : top2KPages){
	 // 	System.out.println(s +" : "+DocScore.get(s));
     //	}
		
	    topTerms = new HashMap<String,Integer>();
	    
	    /* fill the topTerm table which is contains the document name and number of bi-words from query in the document */
		for (String s : top2KPages) {
			int value =0;
			topTerms.put(s, value);
			for (String st: QueryBiWords) {
				ArrayList<String> listOfFIle = new ArrayList<String>();
				listOfFIle = biWord.postingsList(st);
				if(listOfFIle!=null) {
					if(listOfFIle.contains(s))
						topTerms.put(s, ++value);
				} 				
			}
		}
		
		/* TopK list contains top k documents after all processing */
		topK= new ArrayList<>();
		
	    for(String s : top2KPages) {
	    	String doc;
	    	Double score;
	    	Integer freq;
	    	doc = s;
	    	score = DocScore.get(s);
	    	freq = topTerms.get(s);
	    	finalList l = new finalList(doc, score, freq);
	    	topK.add(l);
	    }
	    
	    /* Display all k top pages relevant to the query */
	    System.out.println("\n Top k pages are : ");
	    Collections.sort(topK);
	    for(int i=0;i<k;i++){
	    	System.out.println(topK.get(i).doc +" : "+ topK.get(i).score);
	    }    
	    
	}
	
	/* Method to get the document ID corresponding to the document name */
	private int getDocId(String fileName) {
		return docListMapToInt.get(fileName);
	}

	/* Method to calculate the weight of term in the query */
	private Float weightInQuery(String t, String query) {
		return ((float)(Math.log(1 + QTF.get(t))/Math.log(2)));
	}

	/* Method to sort hash table according to the score */
	@SuppressWarnings("unchecked")
	public String[] topKPages(int k) {
		ArrayList<String> pageList=new ArrayList<String>();
		int i=0;
		Integer temp=0;
		Object[] a = topTerms.entrySet().toArray();
		
	    Arrays.sort(a, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
	            return ((Map.Entry<String, Integer>) o2).getValue().compareTo(
	                    ((Map.Entry<String, Integer>) o1).getValue());
	        }
	    });
	    for (Object e : a) {
	    	
	    	if(i<k || temp==((Map.Entry<String, Integer>) e).getValue()){
	        	pageList.add(((Map.Entry<String, Integer>) e).getKey());
	        	if (temp!=((Map.Entry<String, Integer>) e).getValue()){
	        		temp = ((Map.Entry<String, Integer>) e).getValue();
	        		i++;
	        	}
	        }
	    }

	    String[] pageArr = new String[pageList.size()];
	    pageArr = pageList.toArray(pageArr);

		return pageArr;
	}
	
	/* Method to sort hash table according to the freq of bi-words in doc from the query */
	@SuppressWarnings("unchecked")
	public String[] top2KPages(int k) {
		ArrayList<String> pageList=new ArrayList<String>();
		int i=0;
		Double temp=0.0;
		Object[] a = DocScore.entrySet().toArray();
		
	    Arrays.sort(a, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
	            return ((Map.Entry<String, Double>) o2).getValue().compareTo(
	                    ((Map.Entry<String, Double>) o1).getValue());
	        }
	    });
	    for (Object e : a) {
	    	
	    	if(i<k || temp==((Map.Entry<String, Double>) e).getValue()){
	        	pageList.add(((Map.Entry<String, Double>) e).getKey());
	        	if (temp!=((Map.Entry<String, Double>) e).getValue()){
	        		temp = ((Map.Entry<String, Double>) e).getValue();
	        		i++;
	        	}
	        }
	    }

	    String[] pageArr = new String[pageList.size()];
	    pageArr = pageList.toArray(pageArr);

		return pageArr;
	}


	/* 
	 * Method to pre-process the query 
	*/
	private static void ProcessQuery(String query) 
	{
		String line = query;
		String lastWord="";
		line = line.trim().toLowerCase();
		line = line.replaceAll("\\s", " ").replaceAll("[:.,;']", "");
		String arr[]=line.split(" ");
		QueryBiWords = new ArrayList<String>();
		
		termsInQuery = new ArrayList<String>();
		QTF = new HashMap<String,Integer>();

		for(String word: arr)
		{
			if(word.length()>2 && (word.compareToIgnoreCase("the")!=0))		// as per requirement do not consider these terms
			{
				if(!termsInQuery.contains(word)) {
					termsInQuery.add(word);		// add to the list of terms for this document
				}
			
			
				// add bi words to a list 
				if(lastWord.isEmpty())
				{
					lastWord=word;
					continue;
				}
				else
				{
					String tmp=word;
					word=lastWord+ " " + word;
					lastWord=tmp;
				}
				QueryBiWords.add(word);
			}
		}
		System.out.println("All Bi words in the query: " + QueryBiWords);
		
		for (String s : arr) 
		{
			if(!QTF.containsKey(s)){
				 QTF.put(s,1);
			} else {
				int x = QTF.get(s);
				x = x+1;
				QTF.put(s,x);
			}	 
		}
	}
	
	/* Main Method */
	public static void main(String[] args) 
	{
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter the path of the folder : ");
		String path = sc.nextLine();
		String dec = "y";
		while (!dec.equals("n")) {
			System.out.println("Please enter your query: \n");
			String query = sc.nextLine();
			System.out.println("Please enter the no. of top results you want to display: ");
			Integer k = Integer.parseInt(sc.nextLine());
			QueryProcessor q = new QueryProcessor(query, k, path);
			System.out.println("Do you want to continue (y/n)?");
			dec = sc.nextLine();
		}
		sc.close();
	}
}

/* finalList class which keep track of document, its score and frequency of bi-word from query */
class finalList implements Comparable<finalList>
{
	String doc;
	Double score;
	Integer BiFrequency;

	public finalList(String doc, Double score, Integer BiFrequency) 
	{
		this.doc = doc;
		this.score = score;
		this.BiFrequency = BiFrequency;
	}
	public int compareTo(finalList other)
	{
		if(BiFrequency > other.BiFrequency)
			return -1;
		else if (BiFrequency < other.BiFrequency)
			return +1;
		else {
			if(score > other.score)
				return -1;
			else if (score < other.score)
				return 1;
			else 
				return 0;
		}
	}
}



