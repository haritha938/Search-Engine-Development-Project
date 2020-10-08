package test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.TokenProcessor;
import edu.csulb.PositionalInvertedIndexer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexTest {
    private static Map<String, List<Posting>> expectedIndex = new HashMap<>();
    static TokenProcessor tokenProcessor=new AdvanceTokenProcessor();
    static Index testIndex =null;
    static String TestQuery;


    @BeforeClass
    public static void PrepareTest(){

        //IdentifyCorpus and CreateIndex --using same methods from the project..but providing params so as to access the test corpus
        DirectoryCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("./files"), ".txt");
        Class[] arg = new Class[2];
        arg[0] = DocumentCorpus.class;
        arg[1]=TokenProcessor.class;
        try {
            Method method = PositionalInvertedIndexer.class.getDeclaredMethod("indexCorpus", arg);
            method.setAccessible(true);
            testIndex = (Index) method.invoke(PositionalInvertedIndexer.class,corpus,  new AdvanceTokenProcessor());
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
        expectedIndex.put( tokenProcessor.processToken("hey").get(0), postingsOfHey);

        Posting term2 = new Posting(0, 2);
        List<Posting> postingsOfTest = new ArrayList<>();
        postingsOfTest.add(term2);

        Posting term3 = new Posting(0, 3);
        List<Posting> postingsOfCase = new ArrayList<>();
        postingsOfCase.add(term3);
        expectedIndex.put( tokenProcessor.processToken("case").get(0), postingsOfCase);

        Posting term4 = new Posting(1, 1);
        List<Posting> postingsOfSearch = new ArrayList<>();
        postingsOfSearch.add(term4);
        expectedIndex.put(tokenProcessor.processToken("search").get(0), postingsOfSearch);


        Posting term5 = new Posting(1, 2);
        List<Posting> postingsOfEngine = new ArrayList<>();
        postingsOfEngine.add(term5);
        expectedIndex.put(tokenProcessor.processToken("engine").get(0), postingsOfEngine);

        Posting term6 = new Posting(2, 1);
        List<Posting> postingsOfIn = new ArrayList<>();
        postingsOfIn.add(term6);
        expectedIndex.put(tokenProcessor.processToken("in").get(0), postingsOfIn);

        Posting term7 = new Posting(2, 2);
        List<Posting> postingsOfThese = new ArrayList<>();
        postingsOfThese.add(term7);
        expectedIndex.put(tokenProcessor.processToken("these").get(0), postingsOfThese);

        Posting term8 = new Posting(3, 1);
        postingsOfTest.add(term8);
        expectedIndex.put(tokenProcessor.processToken("test").get(0), postingsOfTest);

        Posting term9 = new Posting(3, 1);
        List<Posting> postingsOfFiles = new ArrayList<>();
        postingsOfFiles.add(term9);
        expectedIndex.put(tokenProcessor.processToken("files").get(0), postingsOfFiles);

        Posting term10 = new Posting(3, 1);
        List<Posting> postingsOfTestFiles = new ArrayList<>();
        postingsOfTestFiles.add(term10);
        expectedIndex.put(tokenProcessor.processToken("testfiles").get(0), postingsOfTestFiles);

        Posting term11 = new Posting(4, 1);
        List<Posting> postingsOf529 = new ArrayList<>();
        postingsOf529.add(term11);
        expectedIndex.put(tokenProcessor.processToken("cecs529").get(0), postingsOf529);

        Posting term12 = new Posting(4, 2);
        List<Posting> postingsOfCourse = new ArrayList<>();
        postingsOfCourse.add(term12);
        expectedIndex.put(tokenProcessor.processToken("course").get(0), postingsOfCourse);

        Posting term13 = new Posting(4, 3);
        List<Posting> postingsOfIs = new ArrayList<>();
        postingsOfIs.add(term13);
        expectedIndex.put(tokenProcessor.processToken("is").get(0), postingsOfIs);

        Posting term14 = new Posting(4, 4);
        List<Posting> postingsOfAmazing = new ArrayList<>();
        postingsOfAmazing.add(term14);
        expectedIndex.put(tokenProcessor.processToken("amazing").get(0), postingsOfAmazing);

    }



    @Test
    public void TestIndex()
    {
        //check if the size of the expectedIndex matches with the actualIndex(testIndex)
        Assert.assertTrue("Index size do not match", expectedIndex.size()== testIndex.getIndex().size());
        int expectedIndexSize= expectedIndex.size();
        int actualIndexSize= testIndex.getIndex().size();

        if(expectedIndexSize==actualIndexSize) {
            //check if the keys of the expectedIndex matches with the actualIndex(testIndex)
            Assert.assertTrue("Keys not equal", expectedIndex.keySet().equals(testIndex.getIndex().keySet()));
            for(String key: expectedIndex.keySet())
            {
                //Displayed the key for the mismatched values in  expectedIndex and testIndex
                Assert.assertEquals("Mismatch at Index key,"+key, expectedIndex.get(key), testIndex.getIndex().get(key));

            }
        }
    }
}