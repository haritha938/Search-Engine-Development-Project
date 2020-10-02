package cecs429.tolerantRetrieval;

import cecs429.index.Index;

import java.util.*;

public class KGram {
    private Map<String, List<String>> kGramIndex;
    private int kGramSize;
    public KGram(int kGramSize) {
        this.kGramSize=kGramSize;
        this.kGramIndex = new HashMap<>();
    }

    public Map<String, List<String>> getkGramIndex(Index index) {
        processKGram(index);
        return kGramIndex;
    }

    private void processKGram(Index index){
        List<String> mVocabulary = index.getVocabulary();
        for(String word:mVocabulary){
            if(word.length()<kGramSize) {
                List<String> stringList;
                //Create new list or fetch existing list, word at the end and add to Map
                if (kGramIndex.containsKey(word))
                    stringList = kGramIndex.get(word);
                else
                    stringList = new ArrayList<>();

                stringList.add(word);
                kGramIndex.put(word,stringList);
            }else{
                for(int i=0;i<word.length()-kGramSize;i++){
                    String string = word.substring(i,i+kGramSize);
                    List<String> stringList;
                    //Create new list or fetch existing list, word at the end and add to Map
                    if (kGramIndex.containsKey(string))
                        stringList = kGramIndex.get(string);
                    else
                        stringList = new ArrayList<>();

                    stringList.add(word);
                    kGramIndex.put(string,stringList);
                }

                String origin = word.substring(0,kGramSize-1);
                List<String> stringList;
                //Create new list or fetch existing list, word at the end and add to Map
                if (kGramIndex.containsKey(origin))
                    stringList = kGramIndex.get(origin);
                else
                    stringList = new ArrayList<>();

                stringList.add(origin);
                kGramIndex.put(origin,stringList);


                String trailing = word.substring(word.length()-kGramSize+1);
                //Create new list or fetch existing list, word at the end and add to Map
                if (kGramIndex.containsKey(trailing))
                    stringList = kGramIndex.get(trailing);
                else
                    stringList = new ArrayList<>();

                stringList.add(trailing);
                kGramIndex.put(trailing,stringList);
            }
        }
    }
}
