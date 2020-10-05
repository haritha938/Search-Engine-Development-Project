package test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
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
@RunWith(Parameterized.class)
@Category(PositionalInvertedIndexer.class)

public class SoundexTest {
    /*private static Map<String, List<Posting>> dictionary = new HashMap<>();
    static TokenProcessor tokenProcessor=new AdvanceTokenProcessor();
    static SoundexIndex testIndex=null;
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

        List<Object[]> expectedResult=new ArrayList<>();
        String query1, query2, query3, query4, query5, query6, query7;

        //Testing single term query
        query1 = "Robert";

        List<Integer> ExpectedDoucmentListForQuery1 = new ArrayList<>();
        ExpectedDoucmentListForQuery1.add(4);

        //Testing AND query
        query2 = "Rupert";
        List<Integer> ExpectedDoucmentListForQuery2 = new ArrayList<>();
        ExpectedDoucmentListForQuery2.add(1);

        //Testing OR query
        query3 = "Something";
        List<Integer> ExpectedDoucmentListForQuery3 = new ArrayList<>();
        ExpectedDoucmentListForQuery3.add(0);
        ExpectedDoucmentListForQuery3.add(3);
        ExpectedDoucmentListForQuery3.add(4);

        //Testing query that returns null
        query4 = "call";
        List<Integer> ExpectedDoucmentListForQuery4 = new ArrayList<>();

        //testing phrasequery
        query5="\"CECS529 course is amazing\"";
        List<Integer> ExpectedDoucmentListForQuery5 = new ArrayList<>();
        ExpectedDoucmentListForQuery5.add(4);

        //Testing Not query
        query6 = "Test -case -hey";
        List<Integer> ExpectedDoucmentListForQuery6 = new ArrayList<>();
        ExpectedDoucmentListForQuery6.add(3);

        //Testing wild query
        query7="t*t case + fi*";
        List<Integer> ExpectedDoucmentListForQuery7 = new ArrayList<>();
        ExpectedDoucmentListForQuery7.add(0);
        ExpectedDoucmentListForQuery7.add(3);



        expectedResult.add(new Object[]{query1,ExpectedDoucmentListForQuery1});
        expectedResult.add(new Object[]{query2,ExpectedDoucmentListForQuery2});
        expectedResult.add(new Object[]{query3,ExpectedDoucmentListForQuery3});
        expectedResult.add(new Object[]{query4,ExpectedDoucmentListForQuery4});
        expectedResult.add(new Object[]{query5,ExpectedDoucmentListForQuery5});
        expectedResult.add(new Object[]{query6,ExpectedDoucmentListForQuery6});
        expectedResult.add(new Object[]{query7,ExpectedDoucmentListForQuery7});

        return expectedResult;
    }
*/
}

