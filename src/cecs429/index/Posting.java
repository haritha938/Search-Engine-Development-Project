package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapsulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPositionsList;
	double wdt;

	public Posting(int documentId) {
		mDocumentId = documentId;
	}

	public Posting(int documentId,List<Integer> positionList){
		this.mDocumentId = documentId;
		this.mPositionsList = positionList;
	}

	public Posting(int mDocumentId, double wdt) {
		this.mDocumentId = mDocumentId;
		this.wdt = wdt;
	}

	public Posting(int documentId, int position) {
		mDocumentId = documentId;
		mPositionsList = new ArrayList<>();
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

	public double getWdt() {
		return wdt;
	}

	public void setWdt(double wdt) {
		this.wdt = wdt;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		Posting pos=(Posting)obj;
		if(pos.getDocumentId()==this.mDocumentId)
		{
			List<Integer> ObjPositionList=pos.getPositions();
			List<Integer> PositionList=this.mPositionsList;
			if(ObjPositionList.size()==PositionList.size())
			{
				int n=PositionList.size();
				int counter=0;

				for(int i=0;i<n;i++)
				{
					if(ObjPositionList.get(i)==PositionList.get(i))
					{ counter++;}
					else {
						break;
					}
				}
				if(n==counter)
					result=true;
			}
		}
		return result;
		//return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.mDocumentId ;
		//return super.hashCode();
	}
}
