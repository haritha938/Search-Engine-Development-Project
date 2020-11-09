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

		mChildren = new ArrayList<>((Collection<? extends Query>) children);
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = null;

		int ChildernCount=mChildren.size();
		//if there are any orQuery objects then perform orMerge on them
		if(ChildernCount>0) {
			if (ChildernCount == 1) {
				result = mChildren.get(0).getPostings(index);
			}else{
				List<Posting> postings = null;
				List<Posting> ResultantPostings = null;
				ResultantPostings = mChildren.get(0).getPostings(index);
				for (int i = 1; i < ChildernCount; i++) {
					postings = mChildren.get(i).getPostings(index);
					if (postings != null) {
						ResultantPostings = OrMerge(ResultantPostings, postings,true);
					}
				}
				result = ResultantPostings;
			}
		}
		return result;
	}

	@Override
	public List<Posting> getPostingsWithoutPositions(Index index) {
		List<Posting> result = null;

		int ChildernCount=mChildren.size();
		//if there are any orQuery objects then perform orMerge on them
		if(ChildernCount>0) {
			if (ChildernCount == 1) {
				result = mChildren.get(0).getPostingsWithoutPositions(index);
			}else{
				List<Posting> postings = null;
				List<Posting> ResultantPostings = null;
				ResultantPostings = mChildren.get(0).getPostingsWithoutPositions(index);
				for (int i = 1; i < ChildernCount; i++) {
					postings = mChildren.get(i).getPostingsWithoutPositions(index);
					if (postings != null) {
						ResultantPostings = OrMerge(ResultantPostings, postings,false);
					}

				}
				result = ResultantPostings;

			}
		}
		return result;
	}

	@Override
	public boolean isNegativeQuery() {
		return false;
	}


	//Logic for merging two list using OrMerge
	public List<Posting> OrMerge(List<Posting> A,List<Posting> B,boolean isBooleanQuery) {
		List<Posting> OrMergeResult = new ArrayList<>();
		int aDocId = 0, bDocId = 0;
		int aSize = A.size();
		int bSize = B.size();
		int i = 0, j = 0;
		while (i < aSize && j < bSize) {

			aDocId = A.get(i).getDocumentId();
			bDocId = B.get(j).getDocumentId();

			if (aDocId == bDocId) {
				if(isBooleanQuery) {
					OrMergeResult.add(
							new Posting(
									A.get(i).getDocumentId()
									, mergePositions(A.get(i).getPositions(), B.get(j).getPositions())));
				}else{
					OrMergeResult.add(
							new Posting(
									A.get(i).getDocumentId()
									, A.get(i).getWdt() + B.get(j).getWdt()));
				}
				i++;
				j++;
			} else if (aDocId < bDocId) {
				OrMergeResult.add(A.get(i));
				i++;
			} else //aDocId > bDocId
			{
				OrMergeResult.add(B.get(j));
				j++;
			}
		}
		while (j < bSize) {
			OrMergeResult.add(B.get(j));
			j++;
		}

		while (i < aSize) {
			OrMergeResult.add(A.get(i));
			i++;
		}
		return OrMergeResult;
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
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
				String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()))
				+ " )";
	}
}
