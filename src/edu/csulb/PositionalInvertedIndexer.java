package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.EnglishTokenStream;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class PositionalInvertedIndexer  {

	public static void main(String[] args) {
        Scanner scanner
                = new Scanner(System.in);
	    System.out.println("Please enter your desired search directory...");
	    Path path=Paths.get(scanner.next());
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(path.toAbsolutePath(), ".txt");
		Index index = indexCorpus(corpus) ;
		//TODO: A full query parser; for now, we'll only support single-term queries.
        System.out.println("Please enter your search query...");
        while(scanner.hasNext()) {
            String query = scanner.next();
            //TODO: Tokenization of input ":stem token"
            if(query.equals(":q")){
                break;
            }else if(query.startsWith(":index")){
                path=Paths.get(query.substring(query.indexOf(' ')+1));
                corpus = DirectoryCorpus.loadTextDirectory(path.toAbsolutePath(), ".txt");
                index = indexCorpus(corpus) ;
            }else if(query.equals(":vocab")){
            	index.getVocabulary()
                        .stream()
                        .limit(1000)
                        .forEach(System.out::println);
            }else {
            	List<Posting> resultList = index.getPostings(query);
                for (Posting p : resultList) {
                    System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
                }
                System.out.println("Total number of documents fetched: " + resultList.size() );
            }
        }
	}
	
	private static Index indexCorpus(DocumentCorpus corpus) {
		HashSet<String> vocabulary = new HashSet<>();
		AdvanceTokenProcessor processor = new AdvanceTokenProcessor();

        PositionalInvertedIndex index = new PositionalInvertedIndex();
        for(Document document:corpus.getDocuments()){
			EnglishTokenStream englishTokenStream=new EnglishTokenStream(document.getContent());
			Iterable<String> strings=englishTokenStream.getTokens();
			int i=1;
			for(String string: strings){
				i=1;
			    for(String term:processor.processToken(string)) {
                    index.addTerm(term, document.getId(), i);
                    i++;
				}
			}
			try {
				englishTokenStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return index;
	}

}
