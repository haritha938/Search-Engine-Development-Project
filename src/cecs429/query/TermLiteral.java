package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import cecs429.text.TokenProcessor;

import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements Query {
	private String mTerm;
	TokenProcessor tokenProcessor;
	private boolean isNegativeLiteral;
	public TermLiteral(String term, TokenProcessor tokenProcessor,boolean isNegativeLiteral) {
		if(tokenProcessor!=null) {
			this.tokenProcessor = tokenProcessor;
			this.isNegativeLiteral = isNegativeLiteral;
			mTerm = (term.contains("-"))
					? tokenProcessor.processToken(term.replaceAll("-", "")).get(0)
					: tokenProcessor.processToken(term).get(0);
		}else{
			mTerm = term;
		}
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		return index.getPostingsWithPositions(mTerm);
	}

	@Override
	public List<Posting> getPostingsWithoutPositions(Index index) {
		return index.getPostingsWithOutPositions(mTerm);
	}

	@Override
	public boolean isNegativeQuery() {
		return isNegativeLiteral;
	}

	@Override
	public String toString() {
		return mTerm;
	}

	public String getmTerm() {
		return mTerm;
	}
}
