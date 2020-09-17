package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;

import cecs429.text.Stemmer;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements Query {
	private String mTerm;
	
	public TermLiteral(String term) {

		StringBuilder builder = new StringBuilder(term.trim());
		int i=0;
		while(i<builder.length()){
			if(Character.isDigit(builder.charAt(i)) || Character.isLetter(builder.charAt(i)))
				break;
			i++;
		}
		if(i!=0)
			builder.delete(0,i-1);

		int j=builder.length()-1;
		while(j>=0){
			if(Character.isDigit(builder.charAt(j)) || Character.isLetter(builder.charAt(j)))
				break;
			j--;
		}
		if(j!=builder.length()-1)
			builder.delete(j+1,builder.length());
		 term = builder.toString().replaceAll("\"|'", "");
		//TODO: Need to add stemming code
		if(term.indexOf('-')!=-1){

			String[] arrofStr=term.split("-");
			term=arrofStr[0];
		}
		mTerm = stemProcess(term);
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

	String stemProcess(String queryWord) {
		Stemmer s=new Stemmer();
		char[] n=queryWord.toCharArray();
		s.add(n,n.length);
		s.stem();
		String u=s.toString();
		return u;
	}
}
