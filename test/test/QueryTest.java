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

import javax.xml.catalog.Catalog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;



@RunWith(Parameterized.class)
@Category(PositionalInvertedIndexer.class)
public class QueryTest {
    private static Map<String, List<Posting>> dictionary = new HashMap<>();
    static TokenProcessor tokenProcessor=new AdvanceTokenProcessor();
    static Index TestIndex=null;
    private final String TestQuery;
    private final List<Integer> expectedDocumentList;

     public QueryTest(String TestQuery, List<Integer> expectedDocumentList)
     {
         this.TestQuery=TestQuery;
         this.expectedDocumentList=expectedDocumentList;
     }

   @Parameterized.Parameters(name="TestQuery = \"{0}\" and expected document list = {1}")
    public static  Collection<Object[]> data()
    {

        List<Object[]> expectedResult=new ArrayList<>();
        String query1, query2, query3, query4, query5, query6, query7;

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





    @Test
    public void TestQuery() {
        //IdentifyCorpus and CreateIndex --using same methods from the project..but proviy=ting params so as to access the test corpus
        DirectoryCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get("./files"), ".txt");
        Class[] arg = new Class[2];
        arg[0] = DocumentCorpus.class;
        arg[1]=TokenProcessor.class;
        try {
            Method method = PositionalInvertedIndexer.class.getDeclaredMethod("indexCorpus", arg);
            method.setAccessible(true);
            TestIndex= (Index) method.invoke(PositionalInvertedIndexer.class,corpus,  new AdvanceTokenProcessor());
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
        dictionary.put( tokenProcessor.processToken("hey").get(0), postingsOfHey);

        Posting term2 = new Posting(0, 2);
        List<Posting> postingsOfTest = new ArrayList<>();
        postingsOfTest.add(term2);

        Posting term3 = new Posting(0, 3);
        List<Posting> postingsOfCase = new ArrayList<>();
        postingsOfCase.add(term3);
        dictionary.put( tokenProcessor.processToken("case").get(0), postingsOfCase);

        Posting term4 = new Posting(1, 1);
        List<Posting> postingsOfSearch = new ArrayList<>();
        postingsOfSearch.add(term4);
        dictionary.put(tokenProcessor.processToken("search").get(0), postingsOfSearch);


        Posting term5 = new Posting(1, 2);
        List<Posting> postingsOfEngine = new ArrayList<>();
        postingsOfEngine.add(term5);
        dictionary.put(tokenProcessor.processToken("engine").get(0), postingsOfEngine);

        Posting term6 = new Posting(2, 1);
        List<Posting> postingsOfIn = new ArrayList<>();
        postingsOfIn.add(term6);
        dictionary.put(tokenProcessor.processToken("in").get(0), postingsOfIn);

        Posting term7 = new Posting(2, 2);
        List<Posting> postingsOfThese = new ArrayList<>();
        postingsOfThese.add(term7);
        dictionary.put(tokenProcessor.processToken("these").get(0), postingsOfThese);

        Posting term8 = new Posting(3, 1);
        postingsOfTest.add(term8);
        dictionary.put(tokenProcessor.processToken("test").get(0), postingsOfTest);

        Posting term9 = new Posting(3, 1);
        List<Posting> postingsOfFiles = new ArrayList<>();
        postingsOfFiles.add(term9);
        dictionary.put(tokenProcessor.processToken("files").get(0), postingsOfFiles);

        Posting term10 = new Posting(3, 1);
        List<Posting> postingsOfTestFiles = new ArrayList<>();
        postingsOfTestFiles.add(term10);
        dictionary.put(tokenProcessor.processToken("testfiles").get(0), postingsOfTestFiles);

        Posting term11 = new Posting(4, 1);
        List<Posting> postingsOf529 = new ArrayList<>();
        postingsOf529.add(term11);
        dictionary.put(tokenProcessor.processToken("cecs529").get(0), postingsOf529);

        Posting term12 = new Posting(4, 2);
        List<Posting> postingsOfCourse = new ArrayList<>();
        postingsOfCourse.add(term12);
        dictionary.put(tokenProcessor.processToken("course").get(0), postingsOfCourse);

        Posting term13 = new Posting(4, 3);
        List<Posting> postingsOfIs = new ArrayList<>();
        postingsOfIs.add(term13);
        dictionary.put(tokenProcessor.processToken("is").get(0), postingsOfIs);

        Posting term14 = new Posting(4, 4);
        List<Posting> postingsOfAmazing = new ArrayList<>();
        postingsOfAmazing.add(term14);
        dictionary.put(tokenProcessor.processToken("amazing").get(0), postingsOfAmazing);

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