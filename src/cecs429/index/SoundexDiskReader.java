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
public class SoundexDiskReader implements Index{
    String path;
    SoundexAlgorithm a;
    ConcurrentMap<String,Long> diskIndex;
    File file;
    public SoundexDiskReader(String path) {
        this.path = path;
        DB db = DBMaker
                .fileDB(path+File.separator+"soundexPositions.db")
                .fileMmapEnable()
                .make();
         diskIndex = db
                .hashMap("address", Serializer.STRING, Serializer.LONG)
                .open();

    }

    @Override
    public List<Posting> getPostingsWithPositions(String term) {
        return null;
    }

    @Override
    public List<Posting> getPostingsWithOutPositions(String term) throws NullPointerException {



        file = new File(path,"SoundexPostings.bin");
        List<Posting> postingList = null;

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {

            String s1=SoundexAlgorithm.getSoundexCode(term);
            if(diskIndex.get(s1)==null){
                return null;
            }
            long address = diskIndex.get(s1);
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
}
