package cecs429.index;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Reads data from disk
 */
public class DiskPositionalIndex implements Index{
    String path;
    DB db;
    ConcurrentMap<String, Long> diskIndex;
    File file;
    Map<String,List<String>> kgramIndex;

    public DiskPositionalIndex(String path){
        this.path = path;
        file = new File(path, "Postings.bin");
        file.mkdirs();
        kgramIndex=new HashMap<>();
        db = DBMaker
                .fileDB(path + File.separator + "positionalIndex.db")
                .fileMmapEnable()
                .make();
        diskIndex = db.treeMap("vocabToAddress", Serializer.STRING, Serializer.LONG)
                .open();
    }

    /**
     * @param term where each posting consist of document Id and positions of occurrence
     * @return list postings of given
     */
    @Override
    public List<Posting> getPostingsWithPositions(String term) {

        if (db.isClosed()) {

            db = DBMaker
                    .fileDB(path + File.separator + "positionalIndex.db")
                    .fileMmapEnable()
                    .make();
            diskIndex = db.treeMap("vocabToAddress", Serializer.STRING, Serializer.LONG)
                    .open();

            file = new File(path, "Postings.bin");
        }

       // file = new File(path, "Postings.bin");

        List<Posting> postingList = null;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {

            if (diskIndex.get(term) == null)
                return null;

            long address = diskIndex.get(term);
            randomAccessFile.seek(address);
            byte[] readIntBuffer = new byte[4];
            byte[] readDoubleBuffer = new byte[8];
            randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
            int numberOfPostings = ByteBuffer.wrap(readIntBuffer).getInt();
            postingList = new ArrayList<>(numberOfPostings);
            int previousDocumentId = 0;
            for (int i = 0; i < numberOfPostings; i++) {
                randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                int currentDocumentId = ByteBuffer.wrap(readIntBuffer).getInt() + previousDocumentId;
                randomAccessFile.read(readDoubleBuffer, 0, readDoubleBuffer.length);
                //Skipping since boolean query does not need wdt
                randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                double weightOfDocTerm = ByteBuffer.wrap(readDoubleBuffer).getDouble();
                int numberOfTerms = ByteBuffer.wrap(readIntBuffer).getInt();
                List<Integer> positions = new ArrayList<>(numberOfTerms);
                int previousPosition = 0;
                for (int j = 0; j < numberOfTerms; j++) {
                    randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                    int currentPosition = ByteBuffer.wrap(readIntBuffer).getInt() + previousPosition;
                    positions.add(currentPosition);
                    previousPosition = currentPosition;
                }
                postingList.add(new Posting(currentDocumentId, positions));
                previousDocumentId = currentDocumentId;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
        return postingList;
    }

    /**
     * @param term where each posting consist of document Id, weight of
     * @param term in document, frequency of
     * @param term in document
     *             without positions
     * @return list postings of given
     */
    @Override
    public List<Posting> getPostingsWithOutPositions(String term) {

        if (db.isClosed()) {

            db = DBMaker
                    .fileDB(path + File.separator + "positionalIndex.db")
                    .fileMmapEnable()
                    .make();
            diskIndex = db.treeMap("vocabToAddress", Serializer.STRING, Serializer.LONG)
                    .open();

            file = new File(path, "Postings.bin");
        }

        List<Posting> postingList = null;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            if (diskIndex.get(term) != null) {
                long address = diskIndex.get(term);
                randomAccessFile.seek(address);
                byte[] readIntBuffer = new byte[4];
                byte[] readDoubleBuffer = new byte[8];
                randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                int numberOfPostings = ByteBuffer.wrap(readIntBuffer).getInt();
                postingList = new ArrayList<>(numberOfPostings);
                int previousDocumentId = 0;
                for (int i = 0; i < numberOfPostings; i++) {

                    randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                    int currentDocumentId = ByteBuffer.wrap(readIntBuffer).getInt() + previousDocumentId;
                    randomAccessFile.read(readDoubleBuffer, 0, readDoubleBuffer.length);
                    double weightOfDocTerm = ByteBuffer.wrap(readDoubleBuffer).getDouble();
                    randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                    int numberOfTerms = ByteBuffer.wrap(readIntBuffer).getInt();
                    randomAccessFile.skipBytes(4 * numberOfTerms);
                    postingList.add(new Posting(currentDocumentId, weightOfDocTerm));
                    previousDocumentId = currentDocumentId;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
        return postingList;
    }

    @Override
    public List<String> getVocabulary() {
        List<String> tokensList=new ArrayList<>();
        File vocabFile;
        vocabFile = new File(path, "vocabulary.bin");
        String token=null;
        try(DataInputStream dataInputStream=new DataInputStream(new FileInputStream(vocabFile))){
            while (dataInputStream.available()>0) {
                byte[] readIntBuffer = new byte[4];
                //read token length
                dataInputStream.read(readIntBuffer, 0, readIntBuffer.length);
                int termLength = ByteBuffer.wrap(readIntBuffer).getInt();
                byte[] readCharBuffer = new byte[termLength];
                //read token in bytes
                dataInputStream.read(readCharBuffer, 0, readCharBuffer.length);
                token = new String(readCharBuffer, StandardCharsets.UTF_8);
                tokensList.add(token);
            }
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokensList;
    }

    @Override
    public Map<String, List<String>> getKGrams() {
        return kgramIndex;
    }

    @Override
    public void generateKGrams(int kGramSize) {
            File kgramFile;
            DB kgramDb = DBMaker
                    .fileDB(path + File.separator + "kgrams.db")
                    .fileMmapEnable()
                    .make();
            ConcurrentMap<String, Long> kgramDiskIndex = kgramDb
                    .treeMap("vocabToAddress", Serializer.STRING, Serializer.LONG)
                    .open();

            kgramFile = new File(path, "kgrams.bin");

        String kgram;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(kgramFile, "r")) {

            Iterator<String> iterator=kgramDiskIndex.keySet().iterator();
            while (iterator.hasNext()) {
                kgram=iterator.next();

                long address = kgramDiskIndex.get(kgram);
                randomAccessFile.seek(address);
                byte[] readIntBuffer = new byte[4];
                randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                int numberOfTermsPerKgram = ByteBuffer.wrap(readIntBuffer).getInt();
                ArrayList<String> listOfTerms = new ArrayList<>(numberOfTermsPerKgram);
                for (int i = 0; i < numberOfTermsPerKgram; i++) {
                    randomAccessFile.read(readIntBuffer, 0, readIntBuffer.length);
                    int lengthOfKgram = ByteBuffer.wrap(readIntBuffer).getInt();
                    byte[] readCharBuffer = new byte[lengthOfKgram];
                    randomAccessFile.read(readCharBuffer, 0, readCharBuffer.length);
                    String kgramString = new String(readCharBuffer, StandardCharsets.UTF_8);
                    listOfTerms.add(kgramString);
                }
                kgramIndex.put(kgram,listOfTerms);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        kgramDb.close();
    }

    @Override
    public List<String> getTerms() {
        List<String> termsList=new ArrayList<>();
        File vocabFile = new File(path, "terms.bin");
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
                termsList.add(term);
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return termsList;
    }

    @Override
    public double getDocLength(int documentID) {
        double docWeight=0;
        File file = new File(path+File.separator+"docWeights.bin");
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            byte[] doubleBuffer = new byte[8];
            randomAccessFile.seek((documentID - 1) * 8);
            randomAccessFile.read(doubleBuffer, 0, doubleBuffer.length);
            docWeight = ByteBuffer.wrap(doubleBuffer).getDouble();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return docWeight;
    }
}
