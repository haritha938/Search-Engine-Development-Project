package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other Query objects and merges their postings in an intersection-like operation.
 */
public class AndQuery implements Query {

	private List<Query> mChildren;
	private boolean isNegativeQuery=false;

	public AndQuery(Iterable<Query> children) {
		mChildren = new ArrayList<>((Collection<? extends Query>) children);
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<>();

		//  program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		int ChildernCount=mChildren.size();
		if(ChildernCount==1)
		{
			result= mChildren.get(0).getPostings(index);

		}
		else
		{
			List<Posting> postings=new ArrayList<>();
			List<Posting> resultantPostings=new ArrayList<>();
			List<Query> notQueries=new ArrayList<>();
			boolean isFirstPositiveQuery=true;
			for(int i=0;i<ChildernCount;i++)
			{
				//if the query is negative then add it to norQueries list
				if(mChildren.get(i).isNegativeQuery())
				{
					Query q=new NotQuery(mChildren.get(i));
					notQueries.add(q);
					continue;
				}
				//Assigning first positive query posting to 'resultantPostings'
				if(isFirstPositiveQuery)
				{
					resultantPostings=mChildren.get(i).getPostings(index);
					isFirstPositiveQuery=false;
					continue;
				}

				postings=mChildren.get(i).getPostings(index);
				if(postings!=null && resultantPostings!=null)
				{
					resultantPostings=AndMerge(resultantPostings,postings);
				}
				else
					return null;

			}

			//If there are any notQueries perform AndNotmerge
			if( notQueries.size()  >0)
			{
				if(resultantPostings!=null)
				{
					resultantPostings= andNotMerge(resultantPostings,notQueries,notQueries.size()-1, index);
				}
				else
				{
					System.out.println("No And query..Only not queries are available. Hence not a valid query");
				}
			}

			result=resultantPostings;
		}

		return result;
	}

	/**
	 * This class is used for boolean queries so this method never get called
	 */
	@Override
	public List<Posting> getPostingsWithoutPositions(Index index) {
		return null;
	}

	@Override
	public boolean isNegativeQuery() {
		return false;
	}


	//This method performs an AndNot Merge on one postive term and a negative term , then returns the resulting posting list after the merge
	public  List<Posting> andNotMerge(List<Posting> posTermPosting, List<Query> notQuery, int index, Index corpusIndex)
	{
		if(index<0)
		{
			return posTermPosting;
		}
		else {
			int i = 0, j = 0;
			int notTermPostingSize=0,resultPostingSize=0;
			List<Posting> NegativeTermpostings=notQuery.get(index).getPostings(corpusIndex);
			if(NegativeTermpostings!=null)
			   notTermPostingSize=NegativeTermpostings.size();
			if(posTermPosting!=null)
			  resultPostingSize=posTermPosting.size();
			List<Posting> result=new ArrayList<>();
			while (i<resultPostingSize && j<notTermPostingSize)
			{
				if(NegativeTermpostings.get(j).getDocumentId()==posTermPosting.get(i).getDocumentId())
				{

					i++;
					j++;
				}
				else if(posTermPosting.get(i).getDocumentId()<NegativeTermpostings.get(j).getDocumentId())
				{
					result.add(posTermPosting.get(i));
					i++;
				}
				else //posTermPosting.get(i).getDocumentId()>NegativeTermpostings.get(j).getDocumentId()
				{
					j++;
				}
			}
			while(i<resultPostingSize)
			{
				result.add(posTermPosting.get(i));
				i++;
			}



			return andNotMerge(result,notQuery,index-1,corpusIndex);
		}
	}

	//Merges two lists using AndMerge logic
	public List<Posting> AndMerge(List<Posting> listA,List<Posting> listB)
	{
		List<Posting> andMergeResult = new ArrayList<>();
		int aDocId = 0, bDocId = 0;
		int aSize = listA.size();
		int bSize = listB.size();
		int i = 0, j = 0;
		while (i < aSize && j < bSize) {

			aDocId = listA.get(i).getDocumentId();
			bDocId = listB.get(j).getDocumentId();

			if (aDocId == bDocId) {
				andMergeResult.add(new Posting(
						listA.get(i).getDocumentId()
						,mergePositions(listA.get(i).getPositions(),listB.get(j).getPositions()
				)));
				i++;
				j++;
			} else if (aDocId < bDocId) {
				i++;
			} else //aDocId > bDocId
			{
				j++;
			}

		}

		return andMergeResult;
	}

	//Merging postions of the postings with same docId, so as to get accurate results while performing wildcard queries.
	List<Integer> mergePositions(List<Integer> positionsOfDocA,List<Integer> positionsOfDocB){
		List<Integer> result = new ArrayList<>();
		int i=0;
		int j=0;
		while(i<positionsOfDocA.size() && j<positionsOfDocB.size()) {
			if (positionsOfDocA.get(i).equals(positionsOfDocB.get(j))) {
				result.add(positionsOfDocA.get(i));
				i++;
				j++;
			} else if (positionsOfDocA.get(i) < positionsOfDocB.get(j)) {
				result.add(positionsOfDocA.get(i));
				i++;
			} else {
				result.add(positionsOfDocB.get(j));
				j++;
			}
		}
		while(i<positionsOfDocA.size()){
			result.add(positionsOfDocA.get(i));
			i++;
		}
		while(j<positionsOfDocB.size()){
			result.add(positionsOfDocB.get(j));
			j++;
		}
		return result;
	}

	@Override
	public String toString() {

		return
				String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
