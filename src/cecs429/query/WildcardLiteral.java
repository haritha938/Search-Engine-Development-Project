package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.*;

public class WildcardLiteral implements Query {
    String queryTerm;
    String modifiedTerm;
    TokenProcessor tokenProcessor;
    public WildcardLiteral(String term, TokenProcessor tokenProcessor) {
        queryTerm = term;
        modifiedTerm = "$"+term+"$";
        this.tokenProcessor = tokenProcessor;
    }

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
/*        String smallLengthKGram="";
        int smallLen=Integer.MAX_VALUE;
        for(String string:kGramSearchTerm){
            if(smallLen>kGramIndex.get(string).size()){
                smallLen=kGramIndex.get(string).size();
                smallLengthKGram=string;
            }
        }
*/
        /*
         * kGramResult will have the valid strings that are fetched
         * from k-gram
         */
        List<String> kGramResult = new ArrayList<>(kGramIndex.get(kGramSearchTerm.get(0)));
//        kGramIndex.remove(kGramSearchTerm.get(0));
//        kGramSearchTerm.remove(0);
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
        List<String> postFitering = new ArrayList<>(kGramResult);
        if(kGramSearchTerm.size()==1){
            if(queryTerm.indexOf("*")==0) {
                postFitering.removeIf(kgram -> !kgram.endsWith(kGramSearchTerm.get(0).substring(0, kGramSearchTerm.get(0).length()-1)));
            }else{
                postFitering.removeIf(kgram -> !kgram.startsWith(kGramSearchTerm.get(0).substring(1)));
            }
        }else {
            for (String kgram : kGramResult) {
                int lastIndex=-1;
                for(String searchWord:kGramSearchTerm){
                    if(searchWord.indexOf("$")==0){
                        if(!kgram.startsWith(searchWord.substring(1))) {
                            postFitering.remove(searchWord);
                            break;
                        }else{
                            lastIndex = 0;
                        }
                    }
                    else if(searchWord.indexOf("$")==searchWord.length()-1){
                        if(!kgram.startsWith(searchWord.substring(0,searchWord.length()-1))) {
                            postFitering.remove(searchWord);
                            break;
                        }else{
                            lastIndex = searchWord.length()-1;
                        }
                    }
                    else if(kgram.indexOf(searchWord)<lastIndex){
                        postFitering.remove(kgram);
                        break;
                    }else{
                        lastIndex = kgram.indexOf(searchWord);
                    }
                }
            }
        }
        return postFitering;
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
