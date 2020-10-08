package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.JsonFileDocument;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.SoundexIndex;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class PositionalInvertedIndexer  {
	static TokenProcessor tokenProcessor = null;
	// Creating a soundexindex instance variable and assigning it as null.
	static SoundexIndex soundexindex=null;
	public static void main(String[] args) {
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("Enter your preferred token processor's serial from below options");
			System.out.println("1. Basic Token processor - Stems based on spaces and removes special characters and changes terms to lowercase");
			System.out.println("2. Advance Token processor - Remove alpha-numeric beginning and end, apostropes and quotation marks, handles hyphens, lowercase letters and uses stemming");
			loop:while(true) {
				String tokenizationInput = reader.readLine();
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
			System.out.println("Please enter your desired search directory...");
			Path path = Paths.get(reader.readLine());
			DocumentCorpus corpus = DirectoryCorpus.loadDirectory(path.toAbsolutePath());
			long startTime=System.nanoTime();
			Index index = indexCorpus(corpus,tokenProcessor);
			index.generateKGrams(3);
			long endTime=System.nanoTime();
			System.out.println("Indexing duration(milli sec):"+ (float)(endTime-startTime)/1000000);
			System.out.println("Please enter your search query...");

			String query=null;
			query=reader.readLine();
			String documentName=null;
			boolean found;
			while (query.length()>0) {
				if (query.equals(":q")) {
					break;
				} else if (query.startsWith(":index")) {
					System.out.println("Enter your preferred token processor's serial from below options");
					System.out.println("1. Basic Token processor - Stems based on spaces and removes special characters and changes terms to lowercase");
					System.out.println("2. Advance Token processor - Remove alpha-numeric beginning and end, apostropes and quotation marks, handles hyphens, lowercase letters and uses stemming");
					loop:while(true) {
						int tokenizationInput = Integer.parseInt(reader.readLine());
						switch (tokenizationInput) {
							case 1:
								tokenProcessor = new BasicTokenProcessor();
								break loop;
							case 2:
								tokenProcessor = new AdvanceTokenProcessor();
								break loop;
							default:
								System.out.println("Please enter a valid input");
								break;
						}
					}
					path = Paths.get(query.substring(query.indexOf(' ') + 1));
					corpus = DirectoryCorpus.loadDirectory(path.toAbsolutePath());
					startTime=System.nanoTime();
					index = indexCorpus(corpus, tokenProcessor);
					index.generateKGrams(3);
					endTime=System.nanoTime();
					System.out.println("Indexing duration(milli sec):"+ (float)(endTime-startTime)/1000000);
				}
				else if(query.startsWith(":stem")){
					String tokenTerm=query.substring(query.indexOf(' ')+1);
					System.out.println(tokenProcessor.processToken(tokenTerm));
				}
				// Checking if the query starts with the word "author"
				else if(query.startsWith(":author")){
					// Getting the next immediate word after author
					String tokenTerm=query.substring(query.indexOf(' ')+1);
					// Getting the soundexindex positings for the given term
					List<Posting> resultPostings=getSoundexIndexPostings(tokenTerm,soundexindex,tokenProcessor);
					//System.out.println(resultPostings.size());

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
							.limit(100000)
							.forEach(System.out::println);
					System.out.println("Size of the vocabulary is" + index.getVocabulary().size());
				}
				else{
					List<Posting> resultList = ParseQueryNGetpostings(query,index,tokenProcessor);
					if (resultList != null && resultList.size()!=0) {
						for (Posting p : resultList) {
							Document document=corpus.getDocument(p.getDocumentId());
							System.out.println( document.getTitle()+" (\""+document.getDocumentName()+"\")" );
						}
						System.out.println("Total number of documents fetched: " + resultList.size());


						while(true)
						{
							System.out.println("Enter document name to view the content (or) type \"query\" to start new search:");
							documentName = reader.readLine();
							if(documentName.equalsIgnoreCase("query"))
							{
								break;
							}
							if(documentName.equalsIgnoreCase(":q"))
							{
								break;
							}
							found=false;
							for(Posting p: resultList) {
								Document document=corpus.getDocument(p.getDocumentId());
								if (documentName.equals(document.getTitle())) {
									Reader printDocument = document.getContent();
									int data = printDocument.read();
									while (data != -1) {
										System.out.print((char) data);
										data = printDocument.read();
									}
									printDocument.close();
									System.out.println();
									found=true;
								}


							}
							if(!found) {
								System.out.println("Wrong document name. Enter document names from the above list !");
							}
							else {
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
				}
				if(documentName!=null)
				{
					if(documentName.equals(":q"))
					break;
				}
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
		for(Document document:corpus.getDocuments()){
			EnglishTokenStream englishTokenStream=new EnglishTokenStream(document.getContent());
			Iterable<String> strings=englishTokenStream.getTokens();
			int i=1;
			for(String string: strings){
				for(String term:tokenProcessor.processToken(string)) {
					index.addTerm(term, document.getId(), i);
				}
				i++;
				if(string.contains("-")){
					for(String splitString:string.split("-+")){
						String term=tokenProcessor.normalization(splitString).toLowerCase();
						if(!term.isEmpty())
							index.addToVocab(term);
					}
				}else{
					String term=tokenProcessor.normalization(string).toLowerCase();
					if(!term.isEmpty())
						index.addToVocab(term);
				}
			}
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
							// adding it to the soundexindex using addTerm method
							soundexindex.addTerm(authorTerm,file.getId());
						}

					}
				}
			}


		}
		return index;
	}

	private static void printDocument(String filePath)throws IOException{
		BufferedReader reader=new BufferedReader(new FileReader(filePath));
		String line;
		while((line=reader.readLine())!=null){
			System.out.println(line);
		}

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
	// Returns the resultant postings from the given author query
	public static List<Posting> getSoundexIndexPostings(String query, SoundexIndex index, TokenProcessor tokenProcessor){

		List<Posting> resultPostings=index.getPostings(tokenProcessor.processToken(query).get(0));
		if(resultPostings!=null){
			return resultPostings;
		}
		return null;
	}
}



