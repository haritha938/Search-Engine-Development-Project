package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;

import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.Stemmer;
import cecs429.text.TokenProcessor;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements Query {
	private String mTerm;
	TokenProcessor tokenProcessor;
	public TermLiteral(String term, TokenProcessor tokenProcessor) {
		this.tokenProcessor = tokenProcessor;
		mTerm = tokenProcessor.processToken(term).get(0);
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		return index.getPostings(mTerm);
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
