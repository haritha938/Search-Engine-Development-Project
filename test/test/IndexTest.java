package test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvanceTokenProcessor;
import cecs429.text.TokenProcessor;
import edu.csulb.PositionalInvertedIndexer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class IndexTest {
    private static Map<String, List<Posting>> expectedIndex = new HashMap<>();
    static TokenProcessor tokenProcessor=new AdvanceTokenProcessor();
    static Index index=null;
    static String TestQuery;
    static DiskPositionalIndex diskPositionalTestIndex;
    static Map<String,List<String>> kgramIndex=new HashMap<>();


    @BeforeClass
    public static void PrepareTest(){

        //IdentifyCorpus and CreateIndex --using same methods from the project..but providing params so as to access the test corpus
       DirectoryCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get("./files"));

        Class[] arg = new Class[3];
        arg[0] =Path.class;
        arg[1]=DocumentCorpus.class;
        arg[2]=TokenProcessor.class;
        try {
           Method method = PositionalInvertedIndexer.class.getDeclaredMethod("createIndex", arg);
            method.setAccessible(true);
            method.invoke(PositionalInvertedIndexer.class,Paths.get("./files"),corpus,tokenProcessor);
            diskPositionalTestIndex = new DiskPositionalIndex(Paths.get("./files").toString() + File.separator + "index");
            diskPositionalTestIndex.generateKGrams(3);
            kgramIndex= diskPositionalTestIndex.getKGrams();

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
        Posting term1 = new Posting(4, 1);
        term1.addPositionToExistingTerm(4);
        List<Posting> postingsOfHey = new ArrayList<>();
        postingsOfHey.add(term1);
        expectedIndex.put( tokenProcessor.processToken("hey").get(0), postingsOfHey);

        Posting term2 = new Posting(4, 2);
        List<Posting> postingsOfTest = new ArrayList<>();
        postingsOfTest.add(term2);

        Posting term3 = new Posting(4, 3);
        List<Posting> postingsOfCase = new ArrayList<>();
        postingsOfCase.add(term3);
        expectedIndex.put( tokenProcessor.processToken("case").get(0), postingsOfCase);

        Posting term4 = new Posting(5, 1);
        List<Posting> postingsOfSearch = new ArrayList<>();
        postingsOfSearch.add(term4);
        expectedIndex.put(tokenProcessor.processToken("search").get(0), postingsOfSearch);


        Posting term5 = new Posting(5, 2);
        List<Posting> postingsOfEngine = new ArrayList<>();
        postingsOfEngine.add(term5);
        expectedIndex.put(tokenProcessor.processToken("engine").get(0), postingsOfEngine);

        Posting term6 = new Posting(6, 1);
        List<Posting> postingsOfIn = new ArrayList<>();
        postingsOfIn.add(term6);
        expectedIndex.put(tokenProcessor.processToken("in").get(0), postingsOfIn);

        Posting term7 = new Posting(6, 2);
        List<Posting> postingsOfThese = new ArrayList<>();
        postingsOfThese.add(term7);
        expectedIndex.put(tokenProcessor.processToken("these").get(0), postingsOfThese);

       Posting term8 = new Posting(7, 1);
        postingsOfTest.add(term8);
        expectedIndex.put(tokenProcessor.processToken("test").get(0), postingsOfTest);

        Posting term9 = new Posting(7, 1);
        List<Posting> postingsOfFiles = new ArrayList<>();
        postingsOfFiles.add(term9);
        expectedIndex.put(tokenProcessor.processToken("files").get(0), postingsOfFiles);

        Posting term10 = new Posting(7, 1);
        List<Posting> postingsOfTestFiles = new ArrayList<>();
        postingsOfTestFiles.add(term10);
        expectedIndex.put(tokenProcessor.processToken("testfiles").get(0), postingsOfTestFiles);

        Posting term11 = new Posting(8, 1);
        List<Posting> postingsOf529 = new ArrayList<>();
        postingsOf529.add(term11);
        expectedIndex.put(tokenProcessor.processToken("cecs529").get(0), postingsOf529);

        Posting term12 = new Posting(8, 2);
        List<Posting> postingsOfCourse = new ArrayList<>();
        postingsOfCourse.add(term12);
        expectedIndex.put(tokenProcessor.processToken("course").get(0), postingsOfCourse);

        Posting term13 = new Posting(8, 3);
        List<Posting> postingsOfIs = new ArrayList<>();
        postingsOfIs.add(term13);
        expectedIndex.put(tokenProcessor.processToken("is").get(0), postingsOfIs);

        Posting term14 = new Posting(8, 4);
        List<Posting> postingsOfAmazing = new ArrayList<>();
        postingsOfAmazing.add(term14);
        expectedIndex.put(tokenProcessor.processToken("amazing").get(0), postingsOfAmazing);

        Posting term15 = new Posting(1, 1);
        List<Posting> postingsOfdesign = new ArrayList<>();
        postingsOfdesign.add(term15);
        Posting term16 = new Posting(2, 1);
        postingsOfdesign.add(term16);
        expectedIndex.put(tokenProcessor.processToken("design").get(0), postingsOfdesign);

        Posting term17 = new Posting(3, 1);
        List<Posting> postingsOfrubric = new ArrayList<>();
        postingsOfrubric.add(term17);
        expectedIndex.put(tokenProcessor.processToken("rubric").get(0), postingsOfrubric);




    }



    @Test
    public void TestIndex()
    {
        if(diskPositionalTestIndex.equals(null))
        {
            System.out.println("diskPositionalTestIndex null");
        }
        else
            System.out.println("terms size:"+diskPositionalTestIndex.getTerms().size());
        //check if the size of the expectedIndex matches with the actualIndex(diskPositionalTestIndex)
        Assert.assertTrue("Index size do not match", expectedIndex.size()== diskPositionalTestIndex.getTerms().size());
        int expectedIndexSize= expectedIndex.size();
        int actualIndexSize= diskPositionalTestIndex.getTerms().size();

        if(expectedIndexSize==actualIndexSize) {
            //check if the keys of the expectedIndex matches with the actualIndex(diskPositionalTestIndex)

            List<String> expectedTermList=new ArrayList<>(expectedIndex.keySet());
           Collections.sort(expectedTermList);

            Assert.assertTrue("Keys not equal", expectedTermList.equals(diskPositionalTestIndex.getTerms()));
            for(String key: expectedIndex.keySet())
            {
                //Displayed the key for the mismatched values in  expectedIndex and diskPositionalTestIndex
                Assert.assertEquals("Mismatch at Index key,"+key, expectedIndex.get(key), diskPositionalTestIndex.getPostingsWithPositions(key));

            }
        }
    }
}