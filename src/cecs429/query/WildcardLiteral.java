package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.*;

/**
 * A WildcardLiteral represents a wildcard term in a sub-query.
 */
public class WildcardLiteral implements Query {
    String queryTerm;
    String modifiedTerm;
    TokenProcessor tokenProcessor;
    boolean isNegativeLiteral;
    public WildcardLiteral(String term, TokenProcessor tokenProcessor,boolean isNegativeLiteral) {
        queryTerm = term;
        modifiedTerm = "$"+term.toLowerCase(Locale.ENGLISH)+"$";
        this.tokenProcessor = tokenProcessor;
        this.isNegativeLiteral=isNegativeLiteral;
    }

    /**
     * @return List of strings that match with given wildcard term after filtering step.
     */
    private List<String> getPossibleStrings(Index index){
        int kGramSize=3;

        /*
         * kGramSearchTerm will have parts of searched wildcard e.g., castl*
         * kGramSearchTerm consist of ca, cas, ast, stl
         */
        List<String> kGramSearchTerm = new ArrayList<>();
        for(String string:modifiedTerm.split("\\*")) {
            if(string.equals("$"))
                    continue;
            else if(string.length() < kGramSize){
                kGramSearchTerm.add(string);
            } else {
                for (int i = 0; i < string.length() - kGramSize+1; i++) {
                    String splitString = string.substring(i, i + kGramSize);
                    kGramSearchTerm.add(splitString);
                }
            }
        }

        Map<String,List<String>> kGramIndex = index.getKGrams();
        List<String> kGramResult =getValidKGrams(kGramSearchTerm,kGramIndex);
        if(kGramResult==null || kGramResult.size()==0)
            return new ArrayList<>();

        /* From here we perform filtering step
         * to check whether fetched strings match with given wildcard
         */
        List<String> postFiltering = new ArrayList<>(kGramResult);
            for (String kgram : kGramResult) {
                int lastIndex=-1;
                for(String searchWord:kGramSearchTerm){
                    if(searchWord.indexOf("$")==0){
                        if(!kgram.startsWith(searchWord.substring(1))) {
                            postFiltering.remove(searchWord);
                            break;
                        }else{
                            lastIndex = 0;
                        }
                    }
                    else if(searchWord.indexOf("$")==searchWord.length()-1){
                        if(!kgram.startsWith(searchWord.substring(0,searchWord.length()-1))) {
                            postFiltering.remove(searchWord);
                            break;
                        }else{
                            lastIndex = searchWord.length()-1;
                        }
                    }
                    else if(kgram.indexOf(searchWord)<lastIndex){
                        postFiltering.remove(kgram);
                        break;
                    }else{
                        lastIndex = kgram.indexOf(searchWord);
                    }
                }
            }
        return postFiltering;
    }

    @Override
    public List<Posting> getPostings(Index index) {

        List<Query> queries = new ArrayList<>();
        TermLiteral previousTermLiteral=null;
        for(String term:getPossibleStrings(index)){
            TermLiteral termLiteral = new TermLiteral(term,tokenProcessor,isNegativeLiteral);
            /*  Checking if termLiteral is same as previously added termLiteral as ques* generate - questions. || questions, || question
                after stemming all of them become question. So, we are pruning remaining list for faster search results
             */
            if(queries.size()==0 || !previousTermLiteral.getmTerm().equals(termLiteral.getmTerm())) {
                queries.add(termLiteral);
                previousTermLiteral = termLiteral;
            }
        }
        return new OrQuery(queries).getPostings(index);
    }

    public List<Posting> getPostingsWithoutPositions(Index index) {

        List<Query> queries = new ArrayList<>();
        TermLiteral previousTermLiteral=null;
        for(String term:getPossibleStrings(index)){
            TermLiteral termLiteral = new TermLiteral(term,tokenProcessor,isNegativeLiteral);
            /*  Checking if termLiteral is same as previously added termLiteral as ques* generate - questions. || questions, || question
                after stemming all of them become question. So, we are pruning remaining list for faster search results
             */
            if(queries.size()==0 || !previousTermLiteral.getmTerm().equals(termLiteral.getmTerm())) {
                queries.add(termLiteral);
                previousTermLiteral = termLiteral;
            }
        }
        return new OrQuery(queries).getPostingsWithoutPositions(index);
    }

    @Override
    public boolean isNegativeQuery() {
        return isNegativeLiteral;
    }

    /**
     * @return possible list of strings that match with
     * @param kGramSearchTerm
     */
    List<String> getValidKGrams(List<String> kGramSearchTerm,Map<String,List<String>> kGramIndex){
        int search;
        List<String> kGramResult=null;
        for(search=0;search<kGramSearchTerm.size();search++) {
            if(kGramIndex.containsKey(kGramSearchTerm.get(search))) {
                if(!kGramSearchTerm.contains("-")) {
                    kGramResult = new ArrayList<>(kGramIndex.get(kGramSearchTerm.get(search)));
                }else{
                    kGramResult = new ArrayList<>(getValidKGrams(tokenProcessor.processToken(kGramSearchTerm.get(search)),kGramIndex));
                }
                break;
            }
        }
        if(search==kGramSearchTerm.size()){
            return new ArrayList<>();
        }
        for(;search<kGramSearchTerm.size();search++){
            String target = kGramSearchTerm.get(search);
            int i=0;
            int j=0;
            List<String> temp = new ArrayList<>();
            while(i<kGramResult.size() && kGramIndex.containsKey(target) && j<kGramIndex.get(target).size()){
                int compare =kGramResult.get(i).compareTo(kGramIndex.get(target).get(j));
                if(compare<0){
                    i++;
                }else if(compare>0){
                    j++;
                }else{
                    if(!kGramResult.get(i).contains("-"))
                        temp.add(kGramResult.get(i));
                    else{
                        List<String> tokenizedTerms = tokenProcessor.processToken(kGramResult.get(i));
                        //Removing unnecessary terms that occurred after stemming of tokens with hyphens
                        if(target.startsWith("$"))
                            tokenizedTerms.removeIf(term->!term.startsWith(target.substring(1)));
                        else if(target.endsWith("$"))
                            tokenizedTerms.removeIf(term->!term.endsWith(target.substring(0,target.length()-1)));
                        else{
                            tokenizedTerms.removeIf(term->term.contains(target));
                        }
                        temp.addAll(tokenizedTerms);
                    }
                    i++;
                    j++;
                }
            }
            kGramResult.clear();
            kGramResult.addAll(temp);
        }
        return kGramResult;
    }
}
