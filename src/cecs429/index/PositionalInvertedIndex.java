package cecs429.index;

import java.util.*;

public class PositionalInvertedIndex implements Index {
    
    private final Map<String, List<Posting>> mIndex;
    private List<String> mVocabulary;

    public PositionalInvertedIndex(){
        mIndex = new HashMap();
        mVocabulary = new ArrayList();
    }

    public void addTerm(String term, int documentId,int position){
        //Updating existing Map entry if term is present
        if(mIndex.containsKey(term)){

            List<Posting> postingsOfTerm = mIndex.get(term);
            Posting positing = postingsOfTerm.get(postingsOfTerm.size()-1);
            
            if(positing.getDocumentId()==documentId){
                positing.addPositionToExistingTerm(position);
            }else{
                Posting newPosting = new Posting(documentId, position);
                postingsOfTerm.add(newPosting);
            }

        }else{
            Posting newPosting = new Posting(documentId, position);
            List<Posting> postingsOfTerm = new ArrayList();
            postingsOfTerm.add(newPosting);
            mIndex.put(term, postingsOfTerm);
            mVocabulary.add(term);
        }
    }

    /**
     * Fetches list of posting of single 
     * @param term
     * TODO: Need to update for phrase search
     */
    public List<Posting> getPostings(String term){
        return mIndex.get(term);
    }

    public List<String> getVocabulary(){
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);
    }
}