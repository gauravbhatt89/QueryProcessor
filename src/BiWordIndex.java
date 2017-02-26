

/*
 * BiWordIndex class will take the folder name and make an inverted index for the biWords in the documents
 * @author: Dipanjan Karmakar
 * @author: Gaurav Bhatt
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BiWordIndex 
{
	LinkedHashMap<String, ArrayList<Integer>> dictionary;
	LinkedHashMap<Integer, LinkedHashMap<String, Integer>> postings;
	static protected int termId;
	static protected int totalDocs;
	private static String folderPath;

	/*
	 * Constructor which will take the filePath as input and return and object for this class
	 */
	public BiWordIndex(String filePath)
	{
		try{
			dictionary = new LinkedHashMap<String, ArrayList<Integer>>();
			postings = new LinkedHashMap<Integer, LinkedHashMap<String, Integer>>();
			totalDocs=0;
			termId=0;
			folderPath=filePath;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * This function build the index of the terms in the document
	 */
	public void buildIndex()
	{
		try{
			/*File op = new File("BiTermsList.txt");
		op.createNewFile();
		FileWriter writer = new FileWriter(op); */


			File folder = new File(folderPath);		
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) 
			{
				if (file.isFile() && !file.isHidden()) 		// do not consider the hidden files
				{
					totalDocs++;
					String lastWord="";
					BufferedReader reader = new BufferedReader(new FileReader(file));
					while(reader.ready())
					{
						ArrayList<Integer> dictionaryTuple = new ArrayList<Integer>();	// First element is the frequency, second is the fileId
						String line= reader.readLine();
						line=line.trim().toLowerCase();		// trim change to lower chase
						line=line.replaceAll("\\s+", " ").replaceAll("[:.,;']", "");  //remove punctuation symbol	
						String arr[]=line.split(" ");		// split into array of terms
						for(String word: arr)
						{
							if(word.length()>2 && (word.compareToIgnoreCase("the")!=0))		// as per requirement do not consider these terms
							{
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

									} else {
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
					//scanner.close();			// close the scanner to avoid the memory leak
					reader.close();				// close the reader to avoid the memory leak
				}
			}
		}catch(Exception pt)
		{
			pt.printStackTrace();
		}
		//writer.flush();writer.close();
	}
	
	/*
	 * This function returns an array list. Returns an array list consisting of document (names) that contain the bi-word.
	 * @param t  the term
	 * @return 	the arrayList that contains the names documents
	 */

	protected ArrayList<String> postingsList(String t) 
	{
		t=t.toLowerCase();
		ArrayList<String> listOfFiles = new ArrayList<String>();

		int termId = 0;

		if (dictionary.get(t) != null) {
			termId = dictionary.get(t).get(1);

			LinkedHashMap<String, Integer> postingTuple = postings.get(termId);

			listOfFiles.addAll(postingTuple.keySet());
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
		ArrayList<String> postingsTupleList=new ArrayList<String>();
		postingsTupleList=postingsList(t);
		if(postingsTupleList!=null && !postingsTupleList.isEmpty())
		{
			System.out.println("Biword \'" + t + "\' appeared in these documents :");
			for(String file: postingsTupleList)
			{
				System.out.println(file);
			}
		}
		else 
			System.out.println("No file found");
	}

	/*public static void main(String[] args) {

		try {
			BiWordIndex biWordInd=new BiWordIndex("/Users/dipanjankarmakar/Documents/Isu Google Drive/Isu Studies Google Drive/BigDataAlgo/Assignments/pa4/pa4");
			biWordInd.buildIndex();
			biWordInd.printPostingsList("absolutely refusing");
			//System.out.println("Total biWords :: " + termId);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
