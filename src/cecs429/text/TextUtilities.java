package cecs429.text;

import cecs429.index.Index;
import cecs429.query.Query;
import cecs429.query.TermLiteral;

import java.util.*;

/**
 * This class consists of functionality related to spelling correction
 */
public class TextUtilities {
    int k=3;
    TokenProcessor tokenProcessor;
    public TextUtilities(TokenProcessor tokenProcessor){
        this.tokenProcessor = tokenProcessor;
    }
    /**
     * @return the string/list of strings that have minimum edits to transform from @param source where
     * @param source is either miss spelled term or a term with postings below threshold,
     * @param possibleStrings are the list of strings that consist the k-grams of @param source
     * and the Jaccard coefficient value greater than threshold
     */
    public List<String> editDistance(String source, List<String> possibleStrings){
        List<String> suggestions = new ArrayList<>();
        int minDistance = Integer.MAX_VALUE;
        for(String token:possibleStrings){
            int calculatedDistance = editDistance(source,token);
            if(calculatedDistance < minDistance && calculatedDistance > 0){
                suggestions.clear();
                suggestions.add(token);
                minDistance = calculatedDistance;
            }else if( minDistance == calculatedDistance && calculatedDistance > 0){
                suggestions.add(token);
            }
        }
        return suggestions;
    }

    /**
     * @return the edit distance between the
     * @param source and the
     * @param target
     */
    int editDistance(String source,String target) {
        int sourceLength = source.length(), targetLength = target.length();
        int[][] dp = new int[sourceLength + 1][targetLength + 1];

        for (int i = 0; i <= sourceLength; i++) {
            for (int j = 0; j <= targetLength; j++) {
                if (i == 0)
                    dp[i][j] = j;
                else if (j == 0)
                    dp[i][j] = i;
                else if (source.charAt(i - 1) == target.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1];
                else
                    dp[i][j] = 1 + min(dp[i][j - 1],
                            dp[i - 1][j],
                            dp[i - 1][j - 1]);
            }
        }
        return dp[sourceLength][targetLength];
    }

    int min(int x, int y, int z){
        if (x <= y && x <= z)
            return x;
        if (y <= x && y <= z)
            return y;
        else
            return z;
    }

    /**
     * @return the spelling correction for
     * @param word by considering the data set
     * @param index
     */
    public String getSuggestion(String word, Index index,int occurredPostingsSize,float jaccardCoefficintThreshold){
        String suggestion=word;
        List<String> commonStringsList = new ArrayList<>();
        List<String> stringList = getKGrams(word);
        for(String s:stringList){
            if(index.getKGrams().containsKey(s)) {
                if(commonStringsList.size() == 0) {
                    commonStringsList.addAll(index.getKGrams().get(s));
                }else {
                    //Merging since strings are stored in alphabetical order
                    commonStringsList = mergeStrings(commonStringsList,index.getKGrams().get(s));
                }
            }
        }
        commonStringsList.removeIf(s->calJaccardCoeff(stringList,s) <= jaccardCoefficintThreshold);
        /* Tie break: If there are more than one term with lowest edit distance
         * choose the term with higher postings
         * even if postings size is same we will only consider the term which comes first alphabetically
         */
        int maxPostings = Integer.MIN_VALUE;
        for(String leastEditedString:editDistance(word,commonStringsList)){
            Query query = new TermLiteral(leastEditedString,tokenProcessor,false);
            int postingListSize = query.getPostings(index).size();
            if(maxPostings<postingListSize){
                maxPostings = postingListSize;
                suggestion = leastEditedString;
            }
        }
        if(maxPostings <= occurredPostingsSize){
            suggestion="";
        }
        return suggestion.trim();
    }

    /**
     * @return the Jaccard coefficient for
     * @param a - list of k-grams of misspelled term/term with low postings
     * @param b one of the string consisting k-grams of @param a
     */
    double calJaccardCoeff(List<String> a, String b){
        double calculatedValue;
        double sizeOfA=a.size();
        List<String> stringList = new ArrayList<>(getKGrams(b));
        double sizeOfB=stringList.size();
        stringList.removeAll(a);
        double commonElements=sizeOfB-stringList.size();
        calculatedValue = commonElements/(sizeOfA+sizeOfB-commonElements);
        return calculatedValue;
    }

    /**
     * @return the k-grams of
     * @param word
     */
    List<String> getKGrams(String word){
        List<String> stringList = new ArrayList<>();
        String modifiedWord;
        if(word.length()!=1)
            modifiedWord = "$" + word + "$";
        else
            modifiedWord=word;
        for (int i = 0; i < modifiedWord.length() - k+1; i++) {
            stringList.add(modifiedWord.substring(i, i + k));
        }
        return stringList;
    }

    List<String> mergeStrings(List<String> listA,List<String> listB){
        List<String> mergedList = new ArrayList<>();
        int i=0;
        int j=0;
        while(i<listA.size() && j<listB.size()){
            String stringA = listA.get(i);
            String stringB = listB.get(j);
            int compare = stringA.compareTo(stringB);
            if(compare < 0){
                mergedList.add(stringA);
                i++;
            }else if(compare > 0){
                mergedList.add(stringB);
                j++;
            }else{
                mergedList.add(stringA);
                i++;
                j++;
            }
        }
        while(i<listA.size()){
            mergedList.add(listA.get(i));
            i++;
        }
        while(j<listB.size()){
            mergedList.add(listB.get(j));
            j++;
        }
        return mergedList;
    }
}
