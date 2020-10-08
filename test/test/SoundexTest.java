package test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.index.SoundexIndex;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.TokenProcessor;
import edu.csulb.PositionalInvertedIndexer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;

import static test.IndexTest.TestQuery;

@RunWith(Parameterized.class)
@Category(PositionalInvertedIndexer.class)

public class SoundexTest {
    // Creating a Map of soundex has and list of postings
    private static Map<String, List<Posting>> dictionary = new HashMap<>();
    /* Creating the tokenprocessor and soundexindex instance variables*/
    static TokenProcessor tokenProcessor=new AdvanceTokenProcessor();
    private SoundexIndex testSoundexIndex;
    static Index testIndex=null;
    private final String authorQuery;
    private final List<Integer> expectedDocumentList;
    public SoundexTest(String authorQuery, List<Integer> expectedDocumentList)
    {
        this.authorQuery=authorQuery;
        this.expectedDocumentList=expectedDocumentList;
    }
    @Parameterized.Parameters(name="TestQuery = \"{0}\" and expected document list = {1}")
    public static  Collection<Object[]> data()
    {
        // Creating array list of objects
        List<Object[]> expectedResult=new ArrayList<>();
        String query1, query2, query3, query4;
        //Testing single term query
        query1 = "Robert";
        List<Integer> ExpectedDoucmentListForQuery1 = new ArrayList<>();
        ExpectedDoucmentListForQuery1.add(0);
        ExpectedDoucmentListForQuery1.add(1);
        //Testing AND query
        query2 = "Rupert";
        List<Integer> ExpectedDoucmentListForQuery2 = new ArrayList<>();
        ExpectedDoucmentListForQuery2.add(0);
        ExpectedDoucmentListForQuery2.add(1);
        //Testing OR query
        query3 = "Mark";
        List<Integer> ExpectedDoucmentListForQuery3 = new ArrayList<>();
        ExpectedDoucmentListForQuery3.add(0);
        ExpectedDoucmentListForQuery3.add(2);
        //Testing query that returns null
        query4 = "call";
        List<Integer> ExpectedDoucmentListForQuery4=new ArrayList<>();
        expectedResult.add(new Object[]{query1,ExpectedDoucmentListForQuery1});
        expectedResult.add(new Object[]{query2,ExpectedDoucmentListForQuery2});
        expectedResult.add(new Object[]{query3,ExpectedDoucmentListForQuery3});
        expectedResult.add(new Object[]{query4,ExpectedDoucmentListForQuery4});
        return expectedResult;
    }

    @Test
    public void TestQuery() {
        //IdentifyCorpus and CreateIndex --using same methods from the project..but providing params so as to access the test corpus
        DirectoryCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get("./files"), ".json");
        // Creating indexCorpus arguments for getDeclaredMethod
        Class[] arg = new Class[2];
        arg[0] = DocumentCorpus.class;
        arg[1]= TokenProcessor.class;
        PositionalInvertedIndexer indexer=new PositionalInvertedIndexer();
        // Instantiating the testSoundexIndex
        testSoundexIndex=new SoundexIndex();
        try {
            Method method= indexer.getClass().getDeclaredMethod("indexCorpus", DocumentCorpus.class, TokenProcessor.class);
            method.setAccessible(true);
            // generating the positional inverted index
            testIndex=(Index)method.invoke(indexer.getClass(),corpus,new AdvanceTokenProcessor());
            // getting the soundex index
            testSoundexIndex=indexer.getSoundexIndex();
        }
        catch ( NoSuchMethodException e)
        {
            e.getStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        // Getting the actual posting lists of the given query
        List<Posting> actualSoundexList = PositionalInvertedIndexer.getSoundexIndexPostings(authorQuery,testSoundexIndex,tokenProcessor);
        List<Integer> actualDocumentList = new ArrayList<>();
        if (actualSoundexList != null) {
            for (Posting posting : actualSoundexList) {
                actualDocumentList.add(posting.getDocumentId());
            }
        }

        //Check whether expected result matches with actualresult.
        Assert.assertArrayEquals(expectedDocumentList.toArray(),actualDocumentList.toArray());

    }
}

