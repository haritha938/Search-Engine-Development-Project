package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other Query objects and merges their postings with a union-type operation.
 */
public class OrQuery implements Query {
	// The components of the Or query.
	private List<Query> mChildren;
	
	public OrQuery(Iterable<Query> children) {

		//mChildren = new ArrayList<>(children);
		mChildren = new ArrayList<>((Collection<? extends Query>) children);
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = null;
		
		// TODO: program the merge for an OrQuery, by gathering the postings of the composed Query children and
		// unioning the resulting postings.
		int ChildernCount=mChildren.size();
		if(ChildernCount==1)
		{
			result= mChildren.get(0).getPostings(index);
		}
		else
		{

			List<Posting> postings=null;
			List<Posting> ResultantPostings=null;
			ResultantPostings=mChildren.get(0).getPostings(index);
			for(int i=1;i<ChildernCount;i++)
			{
				postings=mChildren.get(i).getPostings(index);
				if(postings!=null)
				{
					ResultantPostings=OrMerge(ResultantPostings,postings);
				}

			}
			result=ResultantPostings;

		}

		return result;
	}

	@Override
	public boolean IsNegativeQuery() {
		return false;
	}


	public List<Posting> OrMerge(List<Posting> A,List<Posting> B)
	{
		List<Posting> OrMergeResult = new ArrayList<>();
		int A_docId = 0, B_docId = 0;
		int A_size = A.size();
		int B_size = B.size();
		int i = 0, j = 0;
		while (i < A_size && j < B_size) {

			A_docId = A.get(i).getDocumentId();
			B_docId = B.get(j).getDocumentId();

			if (A_docId == B_docId) {
				OrMergeResult.add(A.get(i));
				i++;
				j++;
			} else if (A_docId < B_docId) {
				OrMergeResult.add(A.get(i));
				i++;
			} else //A_docId > B_docId
			{
				OrMergeResult.add(B.get(j));
				j++;
			}


		}


			while (j < B_size) {
				OrMergeResult.add(B.get(j));
				j++;
			}

			while (i < A_size) {
				OrMergeResult.add(A.get(i));
				i++;

		}
		return OrMergeResult;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
