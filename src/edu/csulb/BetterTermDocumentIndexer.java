package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

public class BetterTermDocumentIndexer {

	public static void main(String[] args) {
		DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get("").toAbsolutePath());
		Index index = indexCorpus(corpus) ;
		// We aren't ready to use a full query parser; for now, we'll only support single-term queries.
		String query = "park"; // hard-coded search for "whale"
		for (Posting p : index.getPostingsWithPositions(query)) {
			System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
		}
	}
	
	private static Index indexCorpus(DocumentCorpus corpus) {
		HashSet<String> vocabulary = new HashSet<>();
		BasicTokenProcessor processor = new BasicTokenProcessor();
		
		// First, build the vocabulary hash set.
		
		// TODO:
		// Get all the documents in the corpus by calling GetDocuments().
		// Iterate through the documents, and:
		// Tokenize the document's content by constructing an EnglishTokenStream around the document's content.
		// Iterate through the tokens in the document, processing them using a BasicTokenProcessor,
		//		and adding them to the HashSet vocabulary.
		for(Document document:corpus.getDocuments()){
			EnglishTokenStream englishTokenStream=new EnglishTokenStream(document.getContent());
			Iterable<String> strings=englishTokenStream.getTokens();
			for(String string: strings){
                List<String> stringList=processor.processToken(string);
			    for(String s:stringList){
                    vocabulary.add(s);
                }
			}
			try {
				englishTokenStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// TODO:
		// Constuct a TermDocumentMatrix once you know the size of the vocabulary.
		// THEN, do the loop again! But instead of inserting into the HashSet, add terms to the index with addPosting.
		TermDocumentIndex index=new TermDocumentIndex(vocabulary, corpus.getCorpusSize());
		for(Document document:corpus.getDocuments()){
			EnglishTokenStream englishTokenStream=new EnglishTokenStream(document.getContent());
			Iterable<String> strings=englishTokenStream.getTokens();
            for(String string: strings){
                for(String term:processor.processToken(string)) {
                    index.addTerm(term, document.getId());
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
