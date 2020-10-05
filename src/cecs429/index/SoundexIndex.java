package cecs429.index;

import cecs429.text.SoundexAlgorithm;

import java.util.*;

public class SoundexIndex {
    private final Map<String, List<Posting>> mIndex;
   // private List<String> mVocabulary;
    private final SoundexAlgorithm soundexAlgorithm;
    public SoundexIndex(){
        mIndex = new HashMap();
       // mVocabulary = new ArrayList();
        soundexAlgorithm=new SoundexAlgorithm();
    }

    public void addTerm(String term, int documentId){
        //Updating existing Map entry if term is present
        String soundexString= soundexAlgorithm.getSoundexCode(term);
        //System.out.println(term +" "+soundexString);
        if(mIndex.containsKey(soundexString)){

            List<Posting> postingsOfTerm = mIndex.get(soundexString);
           // System.out.println(postingsOfTerm.size());
            Posting positing = postingsOfTerm.get(postingsOfTerm.size()-1);

            if(positing.getDocumentId()!=documentId){
                Posting newPosting = new Posting(documentId);
                postingsOfTerm.add(newPosting);
                mIndex.put(soundexString,postingsOfTerm);
            }

        }else{
            //System.out.println(term);
            //System.out.println(term.length());

            //System.out.println(soundexString);
            Posting newPosting = new Posting(documentId);
            List<Posting> postingsOfTerm = new ArrayList();
            postingsOfTerm.add(newPosting);
            mIndex.put(soundexString, postingsOfTerm);

     //       mVocabulary.add(term);
        }
    }

    /**
     * Fetches list of posting of single
     * @param term
     * TODO: Need to update for phrase search
     */
    public List<Posting> getPostings(String term){
        String soundexString=soundexAlgorithm.getSoundexCode(term);
        return mIndex.get(soundexString);
    }

    /*public List<String> getVocabulary(){
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);
    }*/
}
