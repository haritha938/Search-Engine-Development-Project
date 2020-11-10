package cecs429.index;

import cecs429.text.SoundexAlgorithm;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class SoundexPositionalIndex implements SoundexIndexInterface{
    String path;
    public SoundexPositionalIndex(String path){
        this.path = path;
    }

    @Override
    public List<Posting> getPostingsWithOutPositions(String term) {
        DB soundexDb = DBMaker
                .fileDB(path+File.separator+"soundexPositions.db")
                .fileMmapEnable()
                .make();
        ConcurrentMap<String, Long> sdiskIndex = soundexDb
                .treeMap("address", Serializer.STRING, Serializer.LONG)
                .open();
        File soundexFile = new File(path,"SoundexPostings.bin");

        List<Posting> postingList = null;

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(soundexFile, "r")) {

            String s1 = SoundexAlgorithm.getSoundexCode(term);
            if (sdiskIndex.get(s1) == null) {
                return null;
            }
            long address = sdiskIndex.get(s1);
            randomAccessFile.seek(address);
            byte[] readIntBuffer = new byte[4];
            randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
            int numberOfPostings = ByteBuffer.wrap(readIntBuffer).getInt();
            postingList = new ArrayList<>(numberOfPostings);
            int previousDocumentId = 0;
            for (int i = 0; i < numberOfPostings; i++) {
                randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                int currentDocumentId = ByteBuffer.wrap(readIntBuffer).getInt() + previousDocumentId;
                postingList.add(new Posting(currentDocumentId));
                previousDocumentId = currentDocumentId;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        soundexDb.close();
        return postingList;
    }

    //TODO
    @Override
    public List<String> getTerms() {
        List<String> soundexHashTerms=new ArrayList<>();
        File vocabFile = new File(path, "soundexHashTerms.bin");
        String term;

        try (DataInputStream dataInputStream=new DataInputStream(new FileInputStream(vocabFile))){
            while (dataInputStream.available()>0) {
                byte[] readIntBuffer = new byte[4];
                //read term length
                dataInputStream.read(readIntBuffer, 0, readIntBuffer.length);
                int termLength = ByteBuffer.wrap(readIntBuffer).getInt();
                byte[] readCharBuffer = new byte[termLength];
                //read term in bytes
                dataInputStream.read(readCharBuffer, 0, readCharBuffer.length);
                term = new String(readCharBuffer, StandardCharsets.UTF_8);
                soundexHashTerms.add(term);
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return soundexHashTerms;
    }
}
