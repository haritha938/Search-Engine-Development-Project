package test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
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

import static org.junit.runners.Parameterized.*;


@RunWith(Parameterized.class)
@Category(PositionalInvertedIndexer.class)
public class QueryTest {

    static TokenProcessor tokenProcessor=new AdvanceTokenProcessor();
    static Index TestIndex=null;
    private final String TestQuery;
    private final List<Integer> expectedDocumentList;

     public QueryTest(String TestQuery, List<Integer> expectedDocumentList)
     {
         this.TestQuery=TestQuery;
         this.expectedDocumentList=expectedDocumentList;
     }

   @Parameters(name="TestQuery = \"{0}\" and expected document list = {1}")
    public static  Collection<Object[]> data()
    {

        List<Object[]> expectedResult=new ArrayList<>();
        String query1, query2, query3, query4, query5, query6, query7,query8, query9,query10;

        //Testing single term query
        query1 = "amazing";

        List<Integer> ExpectedDoucmentListForQuery1 = new ArrayList<>();
        ExpectedDoucmentListForQuery1.add(4);

        //Testing AND query
        query2 = "Search engine";
        List<Integer> ExpectedDoucmentListForQuery2 = new ArrayList<>();
        ExpectedDoucmentListForQuery2.add(1);

        //Testing OR query
        query3 = "CECS529 + TEST";
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
        //query7="t*t case";
        query7="t*s*";
        List<Integer> ExpectedDoucmentListForQuery7 = new ArrayList<>();
        ExpectedDoucmentListForQuery7.add(0);
        ExpectedDoucmentListForQuery7.add(2);
        ExpectedDoucmentListForQuery7.add(3);

        //Wildphrase query
        query8="\"CECS5*9 co*rse is amaz*\"";
        List<Integer> ExpectedDoucmentListForQuery8 = new ArrayList<>();
        ExpectedDoucmentListForQuery8.add(4);

        //Wild query with AND-NOT
        query9="t*t -case -is";
        List<Integer> ExpectedDoucmentListForQuery9 = new ArrayList<>();
        ExpectedDoucmentListForQuery9.add(3);

        //Wildcard terms with AND
        query10="t*t ca*e";
        List<Integer> ExpectedDoucmentListForQuery10 = new ArrayList<>();
        ExpectedDoucmentListForQuery10.add(0);




        expectedResult.add(new Object[]{query1,ExpectedDoucmentListForQuery1});
        expectedResult.add(new Object[]{query2,ExpectedDoucmentListForQuery2});
        expectedResult.add(new Object[]{query3,ExpectedDoucmentListForQuery3});
        expectedResult.add(new Object[]{query4,ExpectedDoucmentListForQuery4});
        expectedResult.add(new Object[]{query5,ExpectedDoucmentListForQuery5});
        expectedResult.add(new Object[]{query6,ExpectedDoucmentListForQuery6});
        expectedResult.add(new Object[]{query7,ExpectedDoucmentListForQuery7});
        expectedResult.add(new Object[]{query8,ExpectedDoucmentListForQuery8});
        expectedResult.add(new Object[]{query9,ExpectedDoucmentListForQuery9});
        expectedResult.add(new Object[]{query10,ExpectedDoucmentListForQuery10});

        return expectedResult;
    }





    @Test
    public void TestQuery() {
        //IdentifyCorpus and CreateIndex --using same methods from the project..but providing params so as to access the test corpus
        DirectoryCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("./files"), ".txt");
        Class[] arg = new Class[2];
        arg[0] = DocumentCorpus.class;
        arg[1]=TokenProcessor.class;
        try {
            Method method = PositionalInvertedIndexer.class.getDeclaredMethod("indexCorpus", arg);
            method.setAccessible(true);
            TestIndex= (Index) method.invoke(PositionalInvertedIndexer.class,corpus,  new AdvanceTokenProcessor());
            TestIndex.generateKGrams(3);
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


        //Calculating actual result
        List<Posting> ActualPostingList = PositionalInvertedIndexer.ParseQueryNGetpostings(TestQuery, TestIndex, tokenProcessor);
        List<Integer> ActualDocumentList = new ArrayList<>();
        if (ActualPostingList != null) {
            for (Posting posting : ActualPostingList) {
                ActualDocumentList.add(posting.getDocumentId());
            }
        }

        //Check whether expected result matches with actualresult.
        Assert.assertArrayEquals(expectedDocumentList.toArray(),ActualDocumentList.toArray());

    }


}