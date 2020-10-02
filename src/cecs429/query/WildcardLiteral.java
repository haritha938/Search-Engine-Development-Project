package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.TokenProcessor;
import cecs429.tolerantRetrieval.KGram;

import java.util.*;

public class WildcardLiteral implements Query {
    String queryTerm="";
    String[] wildCardStringTerms;
    TokenProcessor tokenProcessor;
    public WildcardLiteral(String term, TokenProcessor tokenProcessor) {
        queryTerm = term;
        this.tokenProcessor = tokenProcessor;
    }

    //TODO:Filtering step need to added
    private List<String> getPossibleStrings(Index index){
        int kGramSize=3;

        /*
         * kGramSearchTerm will have parts of searched wildcard e.g., castl*
         * kGramSearchTerm consist of ca, cas, ast, stl
         */
        Set<String> kGramSearchTerm = new HashSet<>();
        for(String string:queryTerm.split("\\*")) {
            if (string.length() < kGramSize)
                kGramSearchTerm.add(string);
            else {
                for (int i = 0; i < string.length() - kGramSize; i++) {
                    String splitString = string.substring(i, i + kGramSize);
                    kGramSearchTerm.add(splitString);
                }
                /*
                String trailing = string.substring(string.length()-kGramSize+1);
                String origin = string.substring(0,kGramSize-1);
                kGramSearchTerm.add(trailing);
                kGramSearchTerm.add(origin);
                */
                //TODO:Check organization for c*ast*e
            }
        }
        KGram kGram = new KGram(3);
        Map<String,List<String>> kGramIndex = kGram.getkGramIndex(index);
        String smallLengthKGram="";
        int smallLen=Integer.MAX_VALUE;
        for(String string:kGramSearchTerm){
            if(smallLen>kGramIndex.get(string).size()){
                smallLen=kGramIndex.get(string).size();
                smallLengthKGram=string;
            }
        }

        /*
         * kGramResult will have the valid strings that are fetched
         * from k-gram
         */
        List<String> kGramResult = new ArrayList<>();
        kGramResult.addAll(kGramIndex.get(smallLengthKGram));
        for(String string:kGramSearchTerm){
            int i=0;
            int j=0;
            List<String> temp = new ArrayList<>();
            while(i<kGramResult.size() && j<kGramIndex.get(string).size()){
                int compare =kGramResult.get(i).compareTo(kGramIndex.get(string).get(j));
                if(compare<0){
                    i++;
                }else if(compare>0){
                    j++;
                }else{
                    temp.add(kGramResult.get(i));
                    i++;
                    j++;
                }
            }
            kGramResult.clear();
            kGramResult.addAll(temp);
        }
        return kGramResult;
    }

    @Override
    public List<Posting> getPostings(Index index) {

        List<Query> queries = new ArrayList<>();
        for(String term:getPossibleStrings(index)){
            queries.add(new TermLiteral(term,tokenProcessor));
        }
        return new OrQuery(queries).getPostings(index);
    }
}
