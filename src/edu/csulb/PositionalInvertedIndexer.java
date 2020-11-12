package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.*;
import cecs429.query.*;
import cecs429.text.AdvanceTokenProcessor;
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
	static DocumentCorpus corpus;
	static Index index;
	static BufferedReader reader;
	static int corpusSize;
	static Path path;
	static DiskPositionalIndex diskPositionalIndex;
	static SoundexPositionalIndex soudnexpositionalindex;
	static SoundexDiskIndexWriter soundexdiskwriter;


	public static void main(String[] args) {
		reader = new BufferedReader(new InputStreamReader(System.in));
		loadCorpusAndCreateIndex();
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
						termToFreq.put(term,termToFreq.getOrDefault(term,0)+1);
					}
				}
				i++;
				if(string.contains("-")){
					for(String splitString:string.split("-+")){
						String term=tokenProcessor.normalization(splitString).toLowerCase();
						if(!term.isEmpty()) {
							index.addToVocab(term);
						}
					}
				}else{
					String term=tokenProcessor.normalization(string).toLowerCase();
					if(!term.isEmpty()) {
						index.addToVocab(term);
					}
				}
			}
			double wdt = 0;
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
		index.setWeightOfDocuments(weightOfDocuments);
		diskIndexWriter.writeWeightOfDocuments(weightOfDocuments);
		return index;
	}


	public static  boolean loadCorpusAndCreateIndex()
	{

		try {
			System.out.println("Please enter your desired search directory...");
			path = Paths.get(reader.readLine());
			corpus = DirectoryCorpus.loadDirectory(path.toAbsolutePath());
			corpusSize = corpus.getCorpusSize();
			System.out.println("Would you like to create an Index or run the queries? Enter \"Y\" to create an index and \"N\" to run queries ");
			String programMode = reader.readLine();
			if (programMode.equalsIgnoreCase("y") || programMode.equalsIgnoreCase("yes")) {
				tokenProcessor = new AdvanceTokenProcessor();
				createIndex(path, corpus, tokenProcessor);
				return false;
			} else {
				if(new File(path.toString() + File.separator + "index").exists()) {
					if(new File(path.toString() + File.separator + "index").listFiles().length<9)
					{
						System.out.println("No Index files are available. Create an Index!");
						return false;
					}
					else {
						tokenProcessor = new AdvanceTokenProcessor();
						diskPositionalIndex = new DiskPositionalIndex(path.toString() + File.separator + "index");
						soudnexpositionalindex = new SoundexPositionalIndex(path.toString() + File.separator + "index");
						diskPositionalIndex.generateKGrams(3);
						runQueries();
					}
				}
				else
				{
					System.out.println("No Index files are available. Create an Index!");
					return false;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return true;
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
				,tokenProcessor);
		SearchResult searchResult = rankedQueryParser.getPostings(query);
		return  searchResult;
	}

	// Returns the resultant postings from the given author query
	public static List<Posting> getSoundexIndexPostings(String query, SoundexIndex index, TokenProcessor tokenProcessor){

		List<Posting> resultPostings=index.getPostingsWithOutPositions(tokenProcessor.processToken(query).get(0));
		if(resultPostings!=null){
			return resultPostings;
		}
		return null;
	}
	public static List<Posting> getSoundexDiskIndexPostings(String query,SoundexPositionalIndex index,TokenProcessor tokenprocessor){
		List<Posting> resultPostings=index.getPostingsWithOutPositions(tokenprocessor.processToken(query).get(0));
		if(resultPostings!=null)
			return resultPostings;
		return null;
	}

	public static DocumentCorpus createIndex(Path path, DocumentCorpus corpus, TokenProcessor tokenProcessor){

		diskIndexWriter = new DiskIndexWriter(path.toString()
				+File.separator+"index");
		soundexdiskwriter=new SoundexDiskIndexWriter(path.toString()+File.separator+"index");
		long startTime=System.nanoTime();
		index = indexCorpus(corpus,tokenProcessor);
		List<Long> memoryAddresses = diskIndexWriter.writeIndex(index);
		diskIndexWriter.writeVocabularyToDisk(index.getVocabulary());
		List<Long> soundexAddresses=  soundexdiskwriter.writeSoundexIndex(soundexindex);
		index.generateKGrams(3);
		Map<String,List<String>> kgramIndex=index.getKGrams();
		List<Long> kgramAddress=  diskIndexWriter.writeKgramIndex(kgramIndex);
		long endTime=System.nanoTime();
		System.out.println("Indexing duration(milli sec):"+ (float)(endTime-startTime)/1000000);
		return corpus;
	}



	static void runQueries(){
		try {
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
			start:while (query.length()>0) {
				if (query.equals(":q")) {
					break;
				} else if (query.startsWith(":index")) {
					//if loadCorpusAndCreateIndex() returns false=> Implies that index is not available and user has chosen not to create one
					if(!loadCorpusAndCreateIndex())
						break;
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
					List<Posting> resultPostings=getSoundexDiskIndexPostings(tokenTerm,soudnexpositionalindex,tokenProcessor);
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
									System.out.println();
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
					diskPositionalIndex.getVocabulary()
							.stream()
							.limit(1000)
							.forEach(System.out::println);
					System.out.println("Size of the vocabulary is" + diskPositionalIndex.getVocabulary().size());
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

						SearchResult searchResult = getRankedPostings(query,diskPositionalIndex,tokenProcessor);
						List<Accumulator> rankedQueries = searchResult.getSearchResults();
						if (rankedQueries != null && rankedQueries.size() != 0) {
							for (Accumulator accumulator : rankedQueries) {
								Document document = corpus.getDocument(accumulator.getDocumentId());
								System.out.println(document.getTitle() + " (\"" + document.getDocumentName() + "\") Calculated Accumulator value: " + accumulator.getPriority());
							}
							if(!query.equals(searchResult.getSuggestedString())) {
								System.out.println("\""+searchResult.getSuggestedString() +"\" may yield better search results for given query" +
										"\n would like to try? (y/n)");
								query = reader.readLine();
								if(query.equalsIgnoreCase("y")) {
									query = searchResult.getSuggestedString();
									continue start;
								}
							}
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
							if(!query.equals(searchResult.getSuggestedString())) {
								System.out.println("\""+searchResult.getSuggestedString() +"\" may yield better search results for given query" +
										"\n would like to try? (y/n)");
								query = reader.readLine();
								if(query.equalsIgnoreCase("y")) {
									query = searchResult.getSuggestedString();
									continue start;
								}
							}
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
}



