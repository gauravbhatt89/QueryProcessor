
/*
 * WordIndex class will take the folder name and make an inverted index of the terms in the documents
 * @author: Dipanjan Karmakar
 * @author: Gaurav Bhatt
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class WordIndex 
{

	LinkedHashMap<String, ArrayList<Integer>> dictionary;
	LinkedHashMap<Integer, LinkedHashMap<String, Integer>> postings;
	private static String folderPath;
	static protected int termId;
	static protected int totalNoDocs;

	/*
	 * Constructor which will take the filePath as input and return and object for this class
	 */
	public WordIndex(String filePath) 
	{
		try{
			termId=0;
			totalNoDocs=0;
			dictionary = new LinkedHashMap<String, ArrayList<Integer>>();
			postings = new LinkedHashMap<Integer, LinkedHashMap<String, Integer>>();
			folderPath=filePath;
		}
		catch(Exception e)
		{
			e.printStackTrace();;
		}
	}

	/*
	 * This function build the index of the terms in the document
	 */
	public void buildIndex()
	{
		try{
			/*File op = new File("termsList.txt");
		op.createNewFile();
		FileWriter writer = new FileWriter(op); */


			File folder = new File(folderPath);		
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) 
			{
				if (file.isFile() && !file.isHidden()) 		// do not consider the hidden files
				{
					totalNoDocs++;
					BufferedReader reader = new BufferedReader(new FileReader(file));
					while(reader.ready())
					{
						ArrayList<Integer> dictionaryTuple = new ArrayList<Integer>();	// First element is the frequency, second is the fileId
						String line=reader.readLine();
						line=line.trim().toLowerCase();		// trim & change to lower chase
						line=line.replaceAll("\\s+", " ").replaceAll("[:.,;']", "");  //remove punctuation symbol	
						String arr[]=line.split(" ");		// split into array of terms
						for(String word: arr)
						{
							if(word.length()>2 && (word.compareToIgnoreCase("the")!=0))		// as per requirement do not consider these terms
							{
								LinkedHashMap<String, Integer> postingTuple = new LinkedHashMap<String, Integer>();
								if(dictionary.containsKey(word))
								{
									dictionaryTuple = dictionary.get(word);
									postingTuple = postings.get(dictionaryTuple.get(1));
									if (postingTuple.containsKey(file.getName())) 
									{
										int termFreqInDoc = postingTuple.get(file.getName());
										termFreqInDoc++;
										postingTuple.put(file.getName(),termFreqInDoc);

									} 
									else 
									{
										dictionaryTuple.set(0, dictionaryTuple.get(0) +1);
										dictionary.put(word, dictionaryTuple);
										postingTuple.put(file.getName(), 1);
									}
									postings.put(dictionaryTuple.get(1), postingTuple);
								}
								else
								{
									ArrayList<Integer> newTermDet= new ArrayList<Integer>();
									newTermDet.add(1);
									newTermDet.add(termId);

									//writer.write(word +" : " + termId +"\n");

									dictionary.put(word, newTermDet);
									postingTuple.put(file.getName(),1);
									postings.put(termId, postingTuple);
									termId++;
								}
							}
						}
					}
					reader.close();			// close the reader to avoid the memory leak
				}
			}
		}
		catch(Exception pe)
		{
			pe.printStackTrace();
		}
		//writer.flush();	writer.close();
	}
	
	/*
	 * This function calculates the weight of a term in a document
	 * @param t	the term for which to find the weight
	 * @param d	the document in which to find the weight
	 * @return 	the calculate weight
	 */
	protected Float weight(String t, String d) 
	{
		float weight = 0f;
		Integer TFij = 0, dfti = 0;

		LinkedHashMap<String, Integer> postTupl;
		ArrayList<Integer> dicTupl= new ArrayList<Integer>();
		dicTupl = dictionary.get(t.toLowerCase());
		dfti = dicTupl.get(0);
		if(dfti==null)
		{
			dfti=0;
			System.out.println("There is no document in which the string appears");
			return null;
		}

		// Get posting list for the term t
		postTupl = postings.get(dicTupl.get(1));

		if (postTupl.get(d) != null)
			TFij = postTupl.get(d);
		else
			TFij = 0;

		// Calculate the weight of the term in document d
		Double firstTerm=Math.log(1 + TFij)/Math.log(2);
		Double secondterm=Math.log(totalNoDocs / dfti)/Math.log(10);
		weight = (float) (firstTerm*secondterm);

		return weight;
	}
	/*
	 * This function returns an array list. Each item of the array list is a tuple consisting
	 * of document name d and the frequency of term t in document d.
	 * @param t  the term
	 * @return 	the arrayList that contains the required tuple as a HashMap
	 */
	protected ArrayList<LinkedHashMap<String, Integer>> postingsList(String t) 
	{
		t=t.toLowerCase();
		LinkedHashMap<String, Integer> singleTuple;
		ArrayList<LinkedHashMap<String, Integer>> listOfFiles = new ArrayList<LinkedHashMap<String, Integer>>();
		int termId = 0;

		if (dictionary.get(t) != null) {
			termId = dictionary.get(t).get(1);

			LinkedHashMap<String, Integer> postingTuple = postings.get(termId);
			//System.out.println("Length of filesLists :: " + postingTuple.size());
			int i=0;
			for (Map.Entry<String, Integer> entry : postingTuple.entrySet()) 
			{
				singleTuple = new LinkedHashMap<String, Integer>();
				String fileName = entry.getKey();
				//System.out.println(++i + fileName);
				Integer freq = entry.getValue();
				singleTuple.put(fileName,freq);
				listOfFiles.add(singleTuple);
			}
			return listOfFiles;
		} else
			return null;
	}
	/*
	 * This function prints the contents of the list returned by postingsList(t)
	 * in a human-readable form.
	 * @param t  the term for which we need to print the postings lists
	 */
	protected void printPostingsList(String t)
	{
		ArrayList<LinkedHashMap<String, Integer>> postingsTupleList=new ArrayList<LinkedHashMap<String, Integer>>();
		postingsTupleList=postingsList(t);
		if(postingsTupleList!=null && !postingsTupleList.isEmpty())
		{
			System.out.println("Term \'" + t + "\' appeared in these documents with the following frequencies >> ");
			for(LinkedHashMap<String, Integer> tuple: postingsTupleList)
			{
				for (Map.Entry<String, Integer> entry : tuple.entrySet()) {
					String fileName = entry.getKey();
					Integer freq = entry.getValue();
					System.out.println(fileName+" : " + freq);
				}
			}
		}
		else 
			System.out.println("No file found");
	}

	/*public static void main(String[] args) 
	{

		try {
			WordIndex wordInd=new WordIndex("/Users/dipanjankarmakar/Documents/Isu Google Drive/Isu Studies Google Drive/BigDataAlgo/Assignments/pa4/pa4");
			wordInd.buildIndex();
			wordInd.printPostingsList("m}ste");
			//System.out.println("Weight " + wordInd.weight("July", "space-603.txt-clean"));
			//System.out.println("Total words :: " + termId);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}*/

}
