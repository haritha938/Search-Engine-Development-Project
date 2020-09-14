package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapsulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPositionsList;
	
	public Posting(int documentId) {
		mDocumentId = documentId;
	}
	
	public Posting(int documentId,int position) {
		mDocumentId = documentId;
		mPositionsList = new ArrayList();
		mPositionsList.add(position);
	}

	public void addPositionToExistingTerm(int position){
		mPositionsList.add(position);
	}

	public int getDocumentId() {
		return mDocumentId;
	}

	public List<Integer> getPositions(){
		return mPositionsList;
	}
}
