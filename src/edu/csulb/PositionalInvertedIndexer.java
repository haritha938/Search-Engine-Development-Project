package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.*;
import cecs429.query.*;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PositionalInvertedIndexer  {
	static TokenProcessor tokenProcessor = null;
	// Creating a soundexIndex instance variable and assigning it as null.
	static SoundexIndex soundexindex=null;
	static DiskIndexWriter diskIndexWriter=null;
	static Map<String,List<String>> kgramIndex=new HashMap<>();
	static DocumentCorpus corpus;
	static Index index;
	static BufferedReader reader;
	static int corpusSize;
	static int limit = 10;
	static Path path;
	static DiskPositionalIndex diskPositionalIndex;


	public static void main(String[] args) {
		//PositionalInvertedIndex positionalInvertedIndex = new PositionalInvertedIndex();
		reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("Please enter your desired search directory...");
			path = Paths.get(reader.readLine());
			corpus = DirectoryCorpus.loadDirectory(path.toAbsolutePath());
			corpusSize = corpus.getCorpusSize();
			System.out.println("Would you like to create an Index or run the queries? Enter \"Y\" to create an index and \"N\" to run queries ");
			String programMode = reader.readLine();
			if(programMode.equalsIgnoreCase("y") || programMode.equalsIgnoreCase("yes")) {
				chooseTokenProcessor();
				createIndex(path);
			}
			else{
				tokenProcessor=new AdvanceTokenProcessor();
			}
			diskPositionalIndex = new DiskPositionalIndex(path.toString() + File.separator + "index");
			diskPositionalIndex.generateKGrams(3);
			kgramIndex=diskPositionalIndex.getKGrams();
			//soundexdiskreader=new SoundexDiskReader(path.toString()+File.separator+"index");

			System.out.println("Entering query mode");
			System.out.println("Which mode would you like to search:");
			System.out.println("1.Boolean retrieval");
			System.out.println("2.Ranked retrieval");
			String queryMode = reader.readLine();
			System.out.println("Please enter your search query...");

			String query=null;
			query=reader.readLine();
			String documentName=null;
			boolean found;
			while (query.length()>0) {
				if (query.equals(":q")) {
					break;
				} else if (query.startsWith(":index")) {
					chooseTokenProcessor();
					path = Paths.get(query.substring(query.indexOf(' ') + 1));
					corpus = DirectoryCorpus.loadDirectory(path.toAbsolutePath());
					corpusSize = corpus.getCorpusSize();
					createIndex(path);
				}
				else if(query.startsWith(":stem")){
					String tokenTerm=query.substring(query.indexOf(' ')+1);
					System.out.println(tokenProcessor.processToken(tokenTerm));
				}
				// Checking if the query starts with the word "author"
				else if(query.startsWith(":author")){
					// Getting the next immediate word after author
					String tokenTerm=query.substring(query.indexOf(' ')+1);
					// Getting the soundexIndex postings for the given term
					//List<Posting> resultPostings=getSoundexIndexPostings(tokenTerm,soundexindex,tokenProcessor);
					List<Posting> resultPostings=getSoundexDiskIndexPostings(tokenTerm,diskPositionalIndex,tokenProcessor);
					// If the resultant postings are not null, print the postings
					if(resultPostings!=null){
						for(Posting p: resultPostings){
							Document document=corpus.getDocument(p.getDocumentId());
							System.out.println( document.getTitle()+" (\""+document.getDocumentName()+"\")" );

						}
						System.out.println("Total number of documents fetched: " + resultPostings.size());

						while(true) {
							System.out.println("Enter document name to view the content (or) type \"query\" to start new search:");
							documentName = reader.readLine();
							if (documentName.equalsIgnoreCase("query")) {
								break;
							}
							if (documentName.equalsIgnoreCase(":q")) {
								break;
							}
							//String filePath = path + "/" + documentName;
							found = false;
							for (Posting p : resultPostings) {
								Document document = corpus.getDocument(p.getDocumentId());
								if (documentName.equals(document.getTitle())) {
									Reader printDocument = document.getContent();
									int documentData = printDocument.read();
									while (documentData != -1) {
										System.out.print((char) documentData);
										documentData = printDocument.read();
									}
									printDocument.close();
									found = true;
								}
							}
							if (!found) {
								System.out.println("Wrong document name. Enter document names from the above list !");
							}
						}
					}
					// Else no postings found
					else{
						System.out.println("No postings found !");
					}
				}
				else if (query.equals(":vocab")) {
					index.getVocabulary()
							.stream()
							.limit(1000)
							.forEach(System.out::println);
					System.out.println("Size of the vocabulary is" + index.getVocabulary().size());
				}
				else {
					if (queryMode.equals("1")){
						List<Posting> resultList = ParseQueryNGetpostings(query, diskPositionalIndex, tokenProcessor);
						if (resultList != null && resultList.size() != 0) {
							for (Posting p : resultList) {
								Document document = corpus.getDocument(p.getDocumentId());
								System.out.println(document.getTitle() + " (\"" + document.getDocumentName() + "\")");
							}
							System.out.println("Total number of documents fetched: " + resultList.size());
							while (true) {
								System.out.println("Enter document name to view the content (or) type \"query\" to start new search:");
								documentName = reader.readLine();
								if (documentName.equalsIgnoreCase("query")) {
									break;
								}
								if (documentName.equalsIgnoreCase(":q")) {
									break;
								}
								found = false;
								for (Posting p : resultList) {
									Document document = corpus.getDocument(p.getDocumentId());
									if (documentName.equals(document.getTitle())) {
										Reader printDocument = document.getContent();
										int data = printDocument.read();
										while (data != -1) {
											System.out.print((char) data);
											data = printDocument.read();
										}
										printDocument.close();
										System.out.println();
										found = true;
									}
								}
								if (!found) {
									System.out.println("Wrong document name. Enter document names from the above list !");
								} else {
									System.out.println("Do you want to print other selected documents ? (yes/no)");
									String answer = reader.readLine().toLowerCase();
									if (answer.equals("yes"))
										continue;
									break;
								}
							}
						} else {
							System.out.println("No such text can be found in the Corpus!");
						}
					}else {
						/*TODO: After disk storage of index an option similar to property file
							has to be created to store token processor chosen.
						 */
						tokenProcessor = new AdvanceTokenProcessor();
						SearchResult searchResult = getRankedPostings(query,diskPositionalIndex,tokenProcessor);
						List<Accumulator> rankedQueries = searchResult.getSearchResults();
						if (rankedQueries != null && rankedQueries.size() != 0) {
							for (Accumulator accumulator : rankedQueries) {
								Document document = corpus.getDocument(accumulator.getDocumentId());
								System.out.println(document.getTitle() + " (\"" + document.getDocumentName() + "\") Calculated Accumulator value: " + accumulator.getPriority());
							}
							System.out.println("Total number of documents fetched: " + rankedQueries.size());
							//TODO: Add prompt as y/n to search directly with corrected spelling
							System.out.println("Would you like to search with following query for better result:" + searchResult.getSuggestedString());
							while (true) {
								System.out.println("Enter document name to view the content (or) type \"query\" to start new search:");
								documentName = reader.readLine();
								if (documentName.equalsIgnoreCase("query")) {
									break;
								}
								if (documentName.equalsIgnoreCase(":q")) {
									break;
								}
								found = false;
								for (Accumulator p : rankedQueries) {
									Document document = corpus.getDocument(p.getDocumentId());
									if (documentName.equals(document.getTitle())) {
										Reader printDocument = document.getContent();
										int data = printDocument.read();
										while (data != -1) {
											System.out.print((char) data);
											data = printDocument.read();
										}
										printDocument.close();
										System.out.println();
										found = true;
									}
								}
								if (!found) {
									System.out.println("Wrong document name. Enter document names from the above list !");
								} else {
									System.out.println("Do you want to print other selected documents ? (yes/no)");
									String answer = reader.readLine().toLowerCase();
									if (answer.equals("yes"))
										continue;
									break;
								}
							}
						} else {
							System.out.println("No such text can be found in the Corpus!");
							System.out.println("Would you like to search with following query for better result:" + searchResult.getSuggestedString());
						}
					}
				}
				if (documentName != null && documentName.equals(":q"))
					break;
				System.out.println("Please enter your search query...");
				query=reader.readLine();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static Index indexCorpus(DocumentCorpus corpus,TokenProcessor tokenProcessor) {
		// Initializing the the soundexIndex index
		soundexindex=new SoundexIndex();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		JsonFileDocument file;
		Map<String,Integer> termToFreq = new HashMap<>();
		List<Double> weightOfDocuments = new ArrayList<>();
		for(Document document:corpus.getDocuments()){
			EnglishTokenStream englishTokenStream=new EnglishTokenStream(document.getContent());
			Iterable<String> strings=englishTokenStream.getTokens();
			int i=1;
			for(String string: strings){
				for(String term:tokenProcessor.processToken(string)) {
					if(!term.isEmpty()) {
						index.addTerm(term, document.getId(), i);
					}
				}
				i++;
				if(string.contains("-")){
					for(String splitString:string.split("-+")){
						String term=tokenProcessor.normalization(splitString).toLowerCase();
						if(!term.isEmpty()) {
							index.addToVocab(term);
							termToFreq.put(term,termToFreq.getOrDefault(termToFreq.get(term),0)+1);
						}
					}
				}else{
					String term=tokenProcessor.normalization(string).toLowerCase();
					if(!term.isEmpty()) {
						index.addToVocab(term);
						termToFreq.put(term,termToFreq.getOrDefault(termToFreq.get(term),0)+1);
					}
				}
			}
			int wdt = 0;
			for(String termKey:termToFreq.keySet()){
				wdt+=Math.pow(1+Math.log(termToFreq.get(termKey)),2);
			}
			weightOfDocuments.add(Math.sqrt(wdt));
			termToFreq.clear();
			try {
				englishTokenStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			EnglishTokenStream authorTokenStream;
			Iterable<String> authorStrings;
			if(document.hasAuthor()) {
				file = (JsonFileDocument) document;
				// tokenizing the author field using EnglishTokenStream
				authorTokenStream=new EnglishTokenStream(file.getAuthor());
				authorStrings=authorTokenStream.getTokens();
				for(String str: authorStrings){
					// Processing each token generated from author field
					for(String authorTerm: tokenProcessor.processToken(str)){
						if(!authorTerm.equals("")){
							// adding it to the soundexIndex using addTerm method
							soundexindex.addTerm(authorTerm,file.getId());
						}

					}
				}
			}
		}
		diskIndexWriter.writeWeightOfDocuments(weightOfDocuments);
		return index;
	}

	// Returns the soundexIndex instance variable.
	public static SoundexIndex getSoundexIndex(){
		return soundexindex;
	}
	public static List<Posting> ParseQueryNGetpostings(String query,Index index,TokenProcessor tokenProcessor)
	{
		BooleanQueryParser booleanQueryParser = new BooleanQueryParser(tokenProcessor);
		Query queryobject = booleanQueryParser.parseQuery(query.trim());
		List<Posting> resultList = null;
		if (queryobject != null) {
			resultList = queryobject.getPostings(index);
		}
		return resultList;
	}

	public static SearchResult getRankedPostings(String query,Index index,TokenProcessor tokenProcessor){
		RankedQueryParser rankedQueryParser = new RankedQueryParser(index
				,corpusSize
				,path.toString()+File.separator+"index"
				,limit
				,tokenProcessor);
		SearchResult searchResult = rankedQueryParser.getPostings(query);
		return  searchResult;
	}

	// Returns the resultant postings from the given author query
	public static List<Posting> getSoundexIndexPostings(String query, SoundexIndex index, TokenProcessor tokenProcessor){

		List<Posting> resultPostings=index.getPostings(tokenProcessor.processToken(query).get(0));
		if(resultPostings!=null){
			return resultPostings;
		}
		return null;
	}
	public static List<Posting> getSoundexDiskIndexPostings(String query,DiskPositionalIndex index,TokenProcessor tokenprocessor){
		List<Posting> resultPostings=index.getSoundexPostings(tokenprocessor.processToken(query).get(0));
		if(resultPostings!=null)
			return resultPostings;
		return null;
	}
	public static DocumentCorpus createIndex(Path path){
		diskIndexWriter = new DiskIndexWriter(path.toString()
				+File.separator+"index");
		//sIndexWriter=new SoundexIndexWriter(path.toString()+File.separator+"index");
		long startTime=System.nanoTime();
		index = indexCorpus(corpus,tokenProcessor);
		SoundexIndex soundexindex=getSoundexIndex();
		List<Long> memoryAddresses = diskIndexWriter.writeIndex(index);
		List<Long> soundexAddresses=  diskIndexWriter.writeSoundexIndex(soundexindex);

		index.generateKGrams(3);
		Map<String,List<String>> kgramIndex=index.getKGrams();
		List<Long> kgramAddress=  diskIndexWriter.writeKgramIndex(kgramIndex);
		long endTime=System.nanoTime();
		System.out.println("Indexing duration(milli sec):"+ (float)(endTime-startTime)/1000000);
		return corpus;
	}

	public static void chooseTokenProcessor(){
		System.out.println("Enter your preferred token processor's serial from below options");
		System.out.println("1. Basic Token processor - Stems based on spaces and removes special characters and changes terms to lowercase");
		System.out.println("2. Advance Token processor - Remove alpha-numeric beginning and end, apostrophes and quotation marks, handles hyphens, lowercase letters and uses stemming");
		loop:while(true) {
			String tokenizationInput = null;
			try {
				tokenizationInput = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			switch (tokenizationInput) {
				case "1":
					tokenProcessor = new BasicTokenProcessor();
					break loop;
				case "2":
					tokenProcessor = new AdvanceTokenProcessor();
					break loop;
				default:
					System.out.println("Please enter a valid input");
					break;
			}
		}
	}
}



