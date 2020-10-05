package cecs429.text;

class SoundexAlgorithm {
    public static String getSoundexCode(String s)
    {
        char[] x = s.toUpperCase().toCharArray();
        char firstLetter = x[0];
        for (int i = 0; i < x.length; i++) {

            if(x[i]=='B' || x[i]=='F' || x[i]=='P' || x[i]=='V'){
                x[i]='1';
            }
            else if(x[i]=='C' || x[i]=='G' || x[i]=='J' || x[i]=='K' || x[i]=='Q' || x[i]=='S' || x[i]=='X' || x[i]=='Z'){
                x[i]='2';
            }
            else if(x[i]=='D' || x[i]=='T'){
                x[i]='3';
            }
            else if(x[i]=='L'){
                x[i]='4';
            }
            else if(x[i]=='M' || x[i]=='N'){
                x[i]='5';
            }
            else if(x[i]=='R'){
                x[i]='6';
            }
            else{
                x[i]='0';
            }
        }
        String output = "" + firstLetter;
        for (int i = 1; i < x.length; i++)
            if (x[i] != x[i - 1] && x[i] != '0')
                output += x[i];

        output = output + "0000";
        return output.substring(0, 4);
    }
}