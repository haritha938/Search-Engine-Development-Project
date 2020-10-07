package cecs429.tolerantRetrieval;

import java.util.*;

/**
 * This class is responsible for creating K-Grams of given kGramSize from mVocabulary
 */
public class KGram {
    private Map<String, List<String>> kGramIndex;
    private int kGramSize;
    public KGram(int kGramSize) {
        this.kGramSize=kGramSize;
        this.kGramIndex = new HashMap<>();
    }

    public Map<String, List<String>> getkGramIndex(List<String> mVocabulary) {
        processKGram(mVocabulary);
        return Collections.unmodifiableMap(kGramIndex);
    }

    /**
     * @param mVocabulary is processed into k-grams
     */
    private void processKGram(List<String> mVocabulary){
        String lastProcessedWord ="";
        for(int k = 1;k<=kGramSize;k++) {

            for (String word : mVocabulary) {
                if(lastProcessedWord.equals(word))
                    continue;
                String modifiedWord;
                if(word.length()!=1)
                    modifiedWord = "$" + word + "$";
                else
                    modifiedWord=word;
                for (int i = 0; i < modifiedWord.length() - k+1; i++) {
                    String string = modifiedWord.substring(i, i + k);
                    List<String> stringList;
                    //Create new list or fetch existing list, word at the end and add to Map
                    if (kGramIndex.containsKey(string))
                        stringList = kGramIndex.get(string);
                    else
                        stringList = new ArrayList<>();

                    stringList.add(word);
                    kGramIndex.put(string, stringList);
                }
                lastProcessedWord = word;
            }
        }
    }
}
