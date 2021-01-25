package cecs429.index;

import cecs429.text.SoundexAlgorithm;

import java.util.*;

public class SoundexIndex implements SoundexIndexInterface{
    // Creating Map with soundex hash as key and List of postings as values
    private final Map<String, List<Posting>> mIndex;
    // Creating soundexAlgorithm instance variable
    private final SoundexAlgorithm soundexAlgorithm;
    public SoundexIndex(){
        // initializing the instance variables.
        mIndex = new HashMap();
        soundexAlgorithm=new SoundexAlgorithm();
    }

    public void addTerm(String term, int documentId){
        // generating the soudex hashcode using soundex algorithm for the given term
        String soundexString= soundexAlgorithm.getSoundexCode(term);
        if(mIndex.containsKey(soundexString)){
            List<Posting> postingsOfTerm = mIndex.get(soundexString);
            // getting the last document id that has been added in the posting list for the particular term
            Posting positing = postingsOfTerm.get(postingsOfTerm.size()-1);
            // System.out.println(postingsOfTerm.size());
            if(positing.getDocumentId()!=documentId){
                Posting newPosting = new Posting(documentId);
                postingsOfTerm.add(newPosting);
                mIndex.put(soundexString,postingsOfTerm);
            }
        }
        // If the postinglist for the term is not available , creating a new posting list.
        else{
            Posting newPosting = new Posting(documentId);
            List<Posting> postingsOfTerm = new ArrayList();
            postingsOfTerm.add(newPosting);
            mIndex.put(soundexString, postingsOfTerm);

        }
    }
    // getting the soundex postings for the given term

    public Integer getSize(){
        return mIndex.size();
    }

  /* public Map<String,List<Posting>> getIndex(){
        return mIndex;
    }*/

    @Override
    public List<Posting> getPostingsWithOutPositions(String term) {
        String soundexString=soundexAlgorithm.getSoundexCode(term);
        return mIndex.get(soundexString);
    }


    @Override
    public List<String> getTerms() {
        return new ArrayList<>(mIndex.keySet());
    }
    public List<Posting> getPostinglist(String term){
        return mIndex.get(term);
    }
}
