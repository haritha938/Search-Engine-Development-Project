package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements Query {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		//hn
		int size=terms.length();
		terms=terms.substring(terms.indexOf("\"")< size-1? terms.indexOf("\"")+1:0,size-1);
		System.out.println("Phrase Literal terms:"+terms);
		//end hn
		mTerms.addAll(Arrays.asList(terms.split(" ")));
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

	public List<Posting> PositionalMerge(List<Posting> Postings_one, List<Posting> Postings_two, int distance)
	{
		int i=0,j=0,Posting_one_docId=0,Posting_two_docId=0;
		int Posting_one_size=Postings_one.size();
		int Posting_two_size=Postings_two.size();
		List<Posting> Result=null;

		while (i < Posting_one_size && j< Posting_two_size)
		{
			Posting_one_docId=Postings_one.get(i).getDocumentId();
			Posting_two_docId=Postings_two.get(j).getDocumentId();
			if(Posting_one_docId==Posting_two_docId)
			{
				Posting mergedPosting=Merge(Postings_one.get(i).getPositions(),Postings_two.get(j).getPositions(),Posting_one_docId,distance);
				if(mergedPosting!=null)
					Result.add(mergedPosting);

			}
			else if(Posting_one_docId < Posting_two_docId)
			{
				i++;
			}
			else //Posting_one_docId > Posting_two_docId
			{
				j++;
			}
		}

		return Result;
	}

	public Posting Merge(List<Integer> positionList_one,List<Integer> positionList_two,int documentId, int distance)
	{

		int i=0,j=0;
		int ListOne_size=positionList_one.size(),ListTwo_size=positionList_two.size();
		Posting resultantPosting=null;


		while(i<ListOne_size && j< ListTwo_size)
		{
			int postionOf_doc1=positionList_one.get(i);
			int postionOf_doc2=positionList_two.get(j);
					//postionOf_doc1+ distance;
			if(postionOf_doc2== postionOf_doc1+ distance)
			{
				if(resultantPosting==null)
				{
					resultantPosting=new Posting(documentId,postionOf_doc1);
				}
				else
				{
					resultantPosting.addPositionToExistingTerm(postionOf_doc1);
				}
			}
			else if(postionOf_doc2 > postionOf_doc1+ distance)
			{
				i++;
			}
			else //postionOf_doc2 < postionOf_doc1+ distance
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
