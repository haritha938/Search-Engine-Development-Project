package cecs429.index;

import cecs429.text.SoundexAlgorithm;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Reads data from disk
 */
public class DiskPositionalIndex implements Index{
    String path;
    DB soundexDb;
    ConcurrentMap<String,Long> sdiskIndex;
    File file;
    public DiskPositionalIndex(String path){
        this.path = path;
         soundexDb = DBMaker
                .fileDB(path+File.separator+"soundexPositions.db")
                .fileMmapEnable()
                .make();
        sdiskIndex = soundexDb
                .hashMap("address", Serializer.STRING, Serializer.LONG)
                .open();
        file = new File(path,"SoundexPostings.bin");
    }

    /**
     * @return list postings of given
     * @param term where each posting consist of document Id and positions of occurrence
     */
    @Override
    public List<Posting> getPostingsWithPositions(String term) {
        DB db = DBMaker
                .fileDB(path+File.separator+"positionalIndex.db")
                .fileMmapEnable()
                .make();
        ConcurrentMap<String,Long> diskIndex;
        diskIndex = db
                .hashMap("vocabToAddress", Serializer.STRING, Serializer.LONG)
                .open();

        File file = new File(path,"Postings.bin");
        List<Posting> postingList = null;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {

            if(diskIndex.get(term)==null)
                    return null;

            long address = diskIndex.get(term);
            randomAccessFile.seek(address);
            byte[] readIntBuffer = new byte[4];
            byte[] readDoubleBuffer = new byte[8];
            randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
            int numberOfPostings = ByteBuffer.wrap(readIntBuffer).getInt();
            postingList = new ArrayList<>(numberOfPostings);
            int previousDocumentId=0;
            for(int i=0;i<numberOfPostings;i++){
                randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
                int currentDocumentId = ByteBuffer.wrap(readIntBuffer).getInt()+previousDocumentId;
                randomAccessFile.read(readDoubleBuffer,0,readDoubleBuffer.length);
                //Skipping since boolean query does not need wdt
                randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
                int numberOfTerms = ByteBuffer.wrap(readIntBuffer).getInt();
                List<Integer> positions = new ArrayList<>(numberOfTerms);
                int previousPosition = 0;
                for(int j=0;j<numberOfTerms;j++){
                    randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
                    int currentPosition = ByteBuffer.wrap(readIntBuffer).getInt() + previousPosition;
                    positions.add(currentPosition);
                    previousPosition = currentPosition;
                }
                postingList.add(new Posting(currentDocumentId,positions));
                previousDocumentId = currentDocumentId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
        return postingList;
    }

    /**
     * @return list postings of given
     * @param term where each posting consist of document Id, weight of
     * @param term in document, frequency of
     * @param term in document
     * without positions
     */
    @Override
    public List<Posting> getPostingsWithOutPositions(String term) {
        DB db = DBMaker
                .fileDB(path+File.separator+"positionalIndex.db")
                .fileMmapEnable()
                .make();
        ConcurrentMap<String,Long> diskIndex = db
                .hashMap("vocabToAddress", Serializer.STRING, Serializer.LONG)
                .open();

        File file = new File(path,"Postings.bin");
        List<Posting> postingList = null;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {
            long address = diskIndex.get(term);
            randomAccessFile.seek(address);
            byte[] readIntBuffer = new byte[4];
            byte[] readDoubleBuffer = new byte[8];
            randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
            int numberOfPostings = ByteBuffer.wrap(readIntBuffer).getInt();
            postingList = new ArrayList<>(numberOfPostings);
            int previousDocumentId=0;
            for(int i=0;i<numberOfPostings;i++){

                randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
                int currentDocumentId = ByteBuffer.wrap(readIntBuffer).getInt()+previousDocumentId;
                randomAccessFile.read(readDoubleBuffer,0,readDoubleBuffer.length);
                double weightOfDocTerm = ByteBuffer.wrap(readDoubleBuffer).getDouble();
                randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
                int numberOfTerms = ByteBuffer.wrap(readIntBuffer).getInt();
                randomAccessFile.skipBytes(4*numberOfTerms);
                postingList.add(new Posting(currentDocumentId,weightOfDocTerm,numberOfTerms));
                previousDocumentId = currentDocumentId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
        return postingList;
    }

    @Override
    public List<String> getVocabulary() {
        return null;
    }

    @Override
    public Map<String, List<String>> getKGrams() {
        return null;
    }

    @Override
    public void generateKGrams(int kGramSize) {

    }
    @Override
    public Map<String, List<Posting>> getIndex() {
        return null;
    }


    public List<Posting> getSoundexPostings(String term){

        List<Posting> postingList = null;

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {

            String s1= SoundexAlgorithm.getSoundexCode(term);
            if(sdiskIndex.get(s1)==null){
                return null;
            }
            long address = sdiskIndex.get(s1);
            randomAccessFile.seek(address);
            byte[] readIntBuffer = new byte[4];
            byte[] readDoubleBuffer = new byte[8];
            randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
            int numberOfPostings = ByteBuffer.wrap(readIntBuffer).getInt();
            postingList = new ArrayList<>(numberOfPostings);
            int previousDocumentId=0;
            for(int i=0;i<numberOfPostings;i++){

                randomAccessFile.read(readIntBuffer,0,readIntBuffer.length);
                int currentDocumentId = ByteBuffer.wrap(readIntBuffer).getInt()+previousDocumentId;

                postingList.add(new Posting(currentDocumentId));
                previousDocumentId = currentDocumentId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // db.close();
        return postingList;
    }
}
