package cecs429.index;

import java.util.List;
import java.util.Map;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {
	/**
	 * Retrieves a list of Postings of documents that contain the given term.
	 */
	List<Posting> getPostings(String term);
	
	/**
	 * A (sorted) list of all terms in the index vocabulary.
	 */
	List<String> getVocabulary();

	/**
	 * Retrieves the k-grams of size
	 */
	Map<String,List<String>> getKGrams();

	/**
	 * function is used after index is build
	 * to generate k-grams
	 */
	void generateKGrams(int kGramSize);
}
