package cecs429.text;

import org.tartarus.snowball.SnowballStemmer;

import java.lang.StringBuilder;
import java.lang.String;
import java.util.*;


/**
 * A advance creates terms from tokens by removing non-alphanumeric
 * characters from the beginning and end of the token, but not the middle.
 * 
 * Remove all apostropes or quotation marks (single or double quotes) from anywhere in the string.
 * 
 */
public class AdvanceTokenProcessor implements TokenProcessor {
	@Override
	public List<String> processToken(String token) {
        List<String> terms = new ArrayList();
        String term=normalization(token.toLowerCase(Locale.ENGLISH).replaceAll("[\"']", ""));

        //TODO: Need to add stemming code
        if(term.indexOf('-')==-1){
            try {
                terms.add(stemProcess(normalization(term)));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }else{
           String[] arrofStr=term.split("-+");
           for(String a: arrofStr){
               try {
                   terms.add(stemProcess(normalization(a)));
               } catch (Throwable throwable) {
                   throwable.printStackTrace();
               }
           }
            try {
                terms.add(stemProcess(normalization(term.replaceAll("-+",""))));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
		return terms;
    }


    public String stemProcess(String token) throws Throwable{
        Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
        stemmer.setCurrent(token);
        stemmer.stem();
        return stemmer.getCurrent();
	}

    private String normalization(String builder){
        String result =builder;
	    int i=0;
        while(i<result.length()){
            if(Character.isDigit(result.charAt(i)) || Character.isLetter(result.charAt(i)))
                break;
            i++;
        }
        if(i!=0)
            result=result.substring(0,i);

        int j=result.length()-1;
        while(j>=0){
            if(Character.isDigit(result.charAt(j)) || Character.isLetter(result.charAt(j)))
                break;
            j--;
        }
        if(j!=result.length()-1)
            result=result.substring(0,j+1);
        return result;
    }
}
