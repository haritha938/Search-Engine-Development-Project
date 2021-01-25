package cecs429.index;

import cecs429.tolerantRetrieval.KGram;

import java.util.*;

public class PositionalInvertedIndex implements Index {

    private final Map<String, List<Posting>> mIndex;
    private List<String> mVocabulary;
    private Map<String,List<String>> kGramsOfVocabulary;
    private List<Double> weightOfDocuments;

    public PositionalInvertedIndex(){
        mIndex = new HashMap<>();
        mVocabulary = new ArrayList<>();
        kGramsOfVocabulary = new HashMap<>();
        weightOfDocuments = new ArrayList<>();
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
            List<Posting> postingsOfTerm = new ArrayList<>();
            postingsOfTerm.add(newPosting);
            mIndex.put(term, postingsOfTerm);
            //mVocabulary.add(term);
        }
    }

    /**
     * Fetches list of posting of single 
     * @param term
     */
    public List<Posting> getPostingsWithPositions(String term){
        return mIndex.get(term);
    }

    @Override
    public List<Posting> getPostingsWithOutPositions(String term) {
        return mIndex.get(term);
    }

    public List<String> getVocabulary(){
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);
    }

    public void addToVocab(String token){
        mVocabulary.add(token);
    }

    /**
     * Returns k-gram for the @mVocabulary
     * @return
     */
    @Override
    public Map<String, List<String>> getKGrams() {
        return Collections.unmodifiableMap(kGramsOfVocabulary);
    }

    /**
     * This method is used to create k-grams
     * of size @param kGramSize
     */
    public void generateKGrams(int kGramSize){
        KGram kGram = new KGram(kGramSize);
        kGramsOfVocabulary = kGram.getkGramIndex(getVocabulary());
    }

    @Override
    public List<String> getTerms() {
        return new ArrayList<>(mIndex.keySet());
    }

    @Override
    public double getDocLength(int documentID) {
        return weightOfDocuments.get(documentID-1);//-1 since document ordering starts from zero
    }

    public void setWeightOfDocuments(List<Double> weightOfDocuments) {
        this.weightOfDocuments = weightOfDocuments;
    }
}