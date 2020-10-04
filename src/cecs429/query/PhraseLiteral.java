package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.Stemmer;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements Query {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	TokenProcessor tokenProcessor;
	private boolean isNegativeLiteral;

	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms, TokenProcessor tokenProcessor,boolean isNegativeLiteral) {
        this.isNegativeLiteral=isNegativeLiteral;
		int size=terms.length();
		terms=terms.substring(terms.indexOf("\"")< size-1? terms.indexOf("\"")+1:0,size-1);
		System.out.println("Phrase Literal terms:"+terms);
		for(String term: terms.split(" ")) {
			mTerms.add(tokenProcessor.processToken(term).get(0));
		}

	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		// TODO: program this method. Retrieve the postings for the individual terms in the phrase,
		// and positional merge them together.
		List<Posting> postingListResult=index.getPostings(mTerms.get(0));
		List<Posting> postingListInput=null;
		int distanceBetweenTerms=0;
		for(int i=1;i<mTerms.size();i++)
		{
			postingListInput=index.getPostings(mTerms.get(i));
			postingListResult=PositionalMerge(postingListResult,postingListInput,++distanceBetweenTerms);
			if(postingListResult==null)
			{
				return null;
			}

		}

		return postingListResult;
	}

	@Override
	public boolean IsNegativeQuery() {
		return isNegativeLiteral;
	}

	public List<Posting> PositionalMerge(List<Posting> postingsOne, List<Posting> postingsTwo, int distance)
	{
		int i=0,j=0,postingOneDocId=0,postingTwoDocId=0;
		int postingOneSize=postingsOne.size();
		int postingTwoSize=postingsTwo.size();
		List<Posting> Result=null;

		while (i < postingOneSize && j< postingTwoSize)
		{
			postingOneDocId=postingsOne.get(i).getDocumentId();
			postingTwoDocId=postingsTwo.get(j).getDocumentId();
			if(postingOneDocId==postingTwoDocId)
			{
				Posting mergedPosting=Merge(postingsOne.get(i).getPositions(),postingsTwo.get(j).getPositions(),postingOneDocId,distance);
				if(mergedPosting!=null)
				{
					if (Result == null)
					{
						Result = new ArrayList<>();
					}
					Result.add(mergedPosting);
				}
				i++;
				j++;
			}
			else if(postingOneDocId < postingTwoDocId)
			{
				i++;
			}
			else //postingOneDocId > postingTwoDocId
			{
				j++;
			}
		}

		return Result;
	}

	public Posting Merge(List<Integer> positionListOne,List<Integer> positionListTwo,int documentId, int distance)
	{

		int i=0,j=0;
		int listOneSize=positionListOne.size(),listTwoSize=positionListTwo.size();
		Posting resultantPosting=null;


		while(i<listOneSize && j< listTwoSize)
		{
			int postionOfDoc1=positionListOne.get(i);
			int postionOfDoc2=positionListTwo.get(j);
					//postionOfDoc1+ distance;
			if(postionOfDoc2== postionOfDoc1+ distance)
			{
				if(resultantPosting==null)
				{
					resultantPosting=new Posting(documentId,postionOfDoc1);
				}
				else
				{
					resultantPosting.addPositionToExistingTerm(postionOfDoc1);
				}
				i++;
				j++;
			}
			else if(postionOfDoc2 > postionOfDoc1+ distance)
			{
				i++;
			}
			else //postionOfDoc2 < postionOfDoc1+ distance
			{
				j++;
			}
		}

		return resultantPosting;
	}
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
