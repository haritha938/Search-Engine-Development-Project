/* References:
1) https://howtodoinjava.com/algorithm/implement-phonetic-search-using-soundex-algorithm/
2) https://nlp.stanford.edu/IR-book/pdf/03dict.pdf
*/
package cecs429.text;
public class SoundexAlgorithm {
    public static String getSoundexCode(String s)
    {
        // Converting the string to upper case and storing in the characterArray
        char[] x = s.toUpperCase().toCharArray();
        // Restoring the first Letter of the given string.
        char firstLetter = x[0];
        // Iterating from index 1 to length-1 of the given string
        for (int i = 0; i < x.length; i++) {
            // Replacing the characters like 'B','F','P','V' to code as '1'.
            if(x[i]=='B' || x[i]=='F' || x[i]=='P' || x[i]=='V'){
                x[i]='1';
            }
            // Replacing the characters like 'C','G','J','K','Q','S','X' and 'Z' to code as '2'.
            else if(x[i]=='C' || x[i]=='G' || x[i]=='J' || x[i]=='K' || x[i]=='Q' || x[i]=='S' || x[i]=='X' || x[i]=='Z'){
                x[i]='2';
            }
            // Replacing the characters like 'D','T' to code as '3'.
            else if(x[i]=='D' || x[i]=='T'){
                x[i]='3';
            }
            // Replacing the characters like 'L' to code as '4'.
            else if(x[i]=='L'){
                x[i]='4';
            }
            // Replacing the characters like 'M','N' to code as '5'.
            else if(x[i]=='M' || x[i]=='N'){
                x[i]='5';
            }
            // Replacing the characters like 'R' to code as '6'.
            else if(x[i]=='R'){
                x[i]='6';
            }
            // Replacing the characters apart from the above specified character to code as '0'.
            else{
                x[i]='0';
            }
        }
        // The resultant output hash will start with a Character of first character in the original string.
        String output = "" + firstLetter;
        // Repeatedly remove one out of each pair of consecutive identical digits
        for (int i = 1; i < x.length; i++)
            if (x[i] != x[i - 1] && x[i] != '0')
                output += x[i];
        // Padding the resulting string with trailing zeros and return the first four positions, which will consist of a letter followed by 3 digits
        output = output + "0000";
        return output.substring(0, 4);
    }
}