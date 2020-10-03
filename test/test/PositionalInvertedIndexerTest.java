package test;





import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import edu.csulb.PositionalInvertedIndexer;

import org.junit.Assert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.catalog.Catalog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;


@RunWith(Parameterized.class)
class PositionalInvertedIndexerTest {
    private static Map<String, List<Posting>> dictionary = new HashMap<>();


    Index TestIndex=null;
   // List<Posting> postingList=null;
  //  @Parameterized.Parameter(0)
    //String TestQuery;






    @BeforeEach
    public void PrepareTest(){

        //IdentifyCorpus and CreateIndex --using same methods from the project..but proviy=ting params so as to access the test corpus
        DirectoryCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("./files"), ".txt");
        Class[] arg = new Class[1];
        arg[0] = DocumentCorpus.class;
        try {
            Method method = PositionalInvertedIndexer.class.getDeclaredMethod("indexCorpus", arg);
            method.setAccessible(true);
            TestIndex= (Index) method.invoke(PositionalInvertedIndexer.class,corpus);
        }
        catch ( NoSuchMethodException e)
        {
            e.getStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

//Creating TestData
        Posting term1 = new Posting(0, 1);
        term1.addPositionToExistingTerm(4);
        List<Posting> postingsOfHey = new ArrayList<>();
        postingsOfHey.add(term1);
        dictionary.put("hey", postingsOfHey);

        Posting term2 = new Posting(0, 2);
        List<Posting> postingsOfTest = new ArrayList<>();
        postingsOfTest.add(term2);

        Posting term3 = new Posting(0, 3);
        List<Posting> postingsOfCase = new ArrayList<>();
        postingsOfCase.add(term3);
        dictionary.put("case", postingsOfCase);

        Posting term4 = new Posting(1, 1);
        List<Posting> postingsOfSearch = new ArrayList<>();
        postingsOfSearch.add(term4);
        dictionary.put("search", postingsOfSearch);


        Posting term5 = new Posting(1, 2);
        List<Posting> postingsOfEngine = new ArrayList<>();
        postingsOfEngine.add(term5);
        dictionary.put("engine", postingsOfEngine);

        Posting term6 = new Posting(2, 1);
        List<Posting> postingsOfIn = new ArrayList<>();
        postingsOfIn.add(term6);
        dictionary.put("in", postingsOfIn);

        Posting term7 = new Posting(2, 2);
        List<Posting> postingsOfThese = new ArrayList<>();
        postingsOfThese.add(term7);
        dictionary.put("these", postingsOfThese);

        Posting term8 = new Posting(3, 1);
        postingsOfTest.add(term8);
        dictionary.put("test", postingsOfTest);

        Posting term9 = new Posting(3, 1);
        List<Posting> postingsOfFiles = new ArrayList<>();
        postingsOfFiles.add(term9);
        dictionary.put("files", postingsOfFiles);

        Posting term10 = new Posting(3, 1);
        List<Posting> postingsOfTestFiles = new ArrayList<>();
        postingsOfTestFiles.add(term10);
        dictionary.put("testfiles", postingsOfTestFiles);

        Posting term11 = new Posting(4, 1);
        List<Posting> postingsOf529 = new ArrayList<>();
        postingsOf529.add(term11);
        dictionary.put("cecs529", postingsOf529);

        Posting term12 = new Posting(4, 2);
        List<Posting> postingsOfCourse = new ArrayList<>();
        postingsOfCourse.add(term12);
        dictionary.put("course", postingsOfCourse);

        Posting term13 = new Posting(4, 3);
        List<Posting> postingsOfIs = new ArrayList<>();
        postingsOfIs.add(term13);
        dictionary.put("is", postingsOfIs);

        Posting term14 = new Posting(4, 4);
        List<Posting> postingsOfAmazing = new ArrayList<>();
        postingsOfAmazing.add(term12);
        dictionary.put("amazing", postingsOfAmazing);
        String s = "amazing";
    }


    /*@Parameterized.Parameters
    public static  Collection<Object[]> data()
    {
        String[][] data= new String[][]{{"amazing"}};
        return Arrays.asList(data);
    }*/

    @Test
    public void testAndQuery()  {
        //Assert
        //Postings ..manually created-> 'dictionary.get(TestQuery)' with below
        //Posting..calculated using the projects's PositionalInvertedIndexer code--> 'PositionalInvertedIndexer.ParseQueryNGetpostings(TestQuery,TestIndex);'


        String query="cecs529";

        Assert.assertEquals("Failed!",dictionary.get(query),PositionalInvertedIndexer.ParseQueryNGetpostings(query,TestIndex));

    }
}