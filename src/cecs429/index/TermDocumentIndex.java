package cecs429.index;

import java.util.*;

/**
 * Implements an Index using a term-document matrix. Requires knowing the full corpus vocabulary and number of documents
 * prior to construction.
 */
public class TermDocumentIndex implements Index {
	private final boolean[][] mMatrix;
	private final List<String> mVocabulary;
	private int mCorpusSize;
	
	/**
	 * Constructs an empty index with with given vocabulary set and corpus size.
	 * @param vocabulary a collection of all terms in the corpus vocabulary.
	 * @param corpuseSize the number of documents in the corpus.
	 */
	public TermDocumentIndex(Collection<String> vocabulary, int corpuseSize) {
		mMatrix = new boolean[vocabulary.size()][corpuseSize];
		mVocabulary = new ArrayList<>();
		mVocabulary.addAll(vocabulary);
		mCorpusSize = corpuseSize;
		
		Collections.sort(mVocabulary);
	}
	
	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(String term, int documentId) {
		int vIndex = Collections.binarySearch(mVocabulary, term);
		if (vIndex >= 0) {
			mMatrix[vIndex][documentId] = true;
		}
	}
	
	@Override
	public List<Posting> getPostingsWithPositions(String term) {
		List<Posting> results = new ArrayList<>();

		// Binary search the mVocabulary array for the given term.
		// Walk down the mMatrix row for the term and collect the document IDs (column indices)
		// of the "true" entries.
		int index=Collections.binarySearch(mVocabulary, term);
		for(int i=0;i<mMatrix[index].length;i++) {
			if(mMatrix[index][i])
				results.add(new Posting(i));
		}
		return results;
	}

	@Override
	public List<Posting> getPostingsWithOutPositions(String term) {
		return null;
	}

	public List<String> getVocabulary() {
		return Collections.unmodifiableList(mVocabulary);
	}

	@Override
	public Map<String, List<String>> getKGrams() {
		return null;
	}

	@Override
	public void generateKGrams(int kGramSize) {

	}



	@Override
	public List<String> getTerms() {
		return null;
	}

	@Override
	public double getDocLength(int documentID) {
		return 0;
	}
}
