package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other Query objects and merges their postings in an intersection-like operation.
 */
public class AndQuery implements Query {
	//private List<Query> mChildren;
	private List<Query> mChildren;
	
	public AndQuery(Iterable<Query> children) {
		mChildren = new ArrayList<>((Collection<? extends Query>) children);



	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = null;
		
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		int ChildernCount=mChildren.size();
		if(ChildernCount==1)
		{
			result= mChildren.get(0).getPostings(index);
		}
		else
		{

			ArrayList<List<Posting>> AndMergeInputs=new ArrayList<>();
			List<Posting> postings=null;
			for(int i=0;i<ChildernCount;i++)
			{
				postings=mChildren.get(i).getPostings(index);
				if(postings!=null)
				{AndMergeInputs.add(postings);}
				else
					return null;

			}

			while (AndMergeInputs.size()>1)
			{
				AndMergeInputs=Merge(AndMergeInputs);

			}
			if(AndMergeInputs.size()>0) {
				result = AndMergeInputs.get(0);
			}
		}

		return result;
	}

	public ArrayList<List<Posting>> Merge(List<List<Posting>> Inputs)
	{
		int k=0;
		ArrayList<List<Posting>> RemainingAndMergeInputs=new ArrayList<>();
		int ChildernCount=Inputs.size();
		while (k < ChildernCount - 1) {
			List<Posting> A = Inputs.get(k);
			List<Posting> B = Inputs.get(k+1);

			RemainingAndMergeInputs.add(AndMerge(A,B));
			k = k + 2;
			if(k==ChildernCount-1)
			{
				RemainingAndMergeInputs.add(Inputs.get(k));
			}

		}
		return RemainingAndMergeInputs;
	}

	public List<Posting> AndMerge(List<Posting> A,List<Posting> B)
	{
		List<Posting> AndMergeResult = new ArrayList<>();
		int A_docId = 0, B_docId = 0;
		int A_size = A.size();
		int B_size = B.size();
		int i = 0, j = 0;
		while (i < A_size && j < B_size) {

			A_docId = A.get(i).getDocumentId();
			B_docId = B.get(j).getDocumentId();

			if (A_docId == B_docId) {
				AndMergeResult.add(A.get(i));
				i++;
				j++;
			} else if (A_docId < B_docId) {
				i++;
			} else //A_docId > B_docId
			{
				j++;
			}

		}

		return AndMergeResult;
	}
	
	@Override
	public String toString() {

		return
		 String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
