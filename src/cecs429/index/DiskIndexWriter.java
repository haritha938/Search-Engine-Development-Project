package cecs429.index;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Writes index to disk
 */
public class DiskIndexWriter {

    String path;
    public DiskIndexWriter(String path){
        this.path = path;
    }

    /**
     * Writes the
     * @param index to Postings.bin file and
     * creates a mapping between term and its address in database positionalIndex.db's vocabToAddress collection
     * @return list of addresses where sorted terms are stored
     */
    public List<Long> writeIndex(Index index){
        List<Long> locations = new LinkedList<>();
        File termsFile = new File(path,"terms.bin");
        File postingsFile = new File(path,"Postings.bin");
        File mapDBFile = new File(path,"positionalIndex.db");
        postingsFile.getParentFile().mkdirs();
        if(postingsFile.exists())
            postingsFile.delete();
        if(mapDBFile.exists())
            mapDBFile.delete();
        if(termsFile.exists())
            termsFile.delete();

        List<String> sortedTerms = new ArrayList<>(index.getTerms());
        Collections.sort(sortedTerms);
        try (DB db = DBMaker
                .fileDB(path + File.separator + "positionalIndex.db")
                .fileMmapEnable()
                .make()) {
            ConcurrentMap<String, Long> diskIndex = db
                    .treeMap("vocabToAddress", Serializer.STRING, Serializer.LONG)
                    .create();

            DataOutputStream outputStream;
            DataOutputStream outputStreamForTerms;
            try{
                outputStream = new DataOutputStream(new FileOutputStream(postingsFile));
                postingsFile.createNewFile();
                outputStreamForTerms = new DataOutputStream(new FileOutputStream(termsFile));
                termsFile.createNewFile();
                for (String term : sortedTerms) {
                    List<Posting> postingList = index.getPostingsWithPositions(term);
                    //Writing current stream location to output list and dictionary of term to address
                    locations.add((long) outputStream.size());
                    diskIndex.put(term, (long) outputStream.size());
                    //Convert term to bytes
                    byte arr[]=term.getBytes(StandardCharsets.UTF_8);
                    //write size of term
                    outputStreamForTerms.writeInt(arr.length);
                    //Writing term to terms.bin;
                    outputStreamForTerms.write(arr);
                    //Writing Number of postings for given term; dft
                    outputStream.writeInt(postingList.size());
                    int previousDocID = 0;
                    for (Posting posting : postingList) {
                        //Writing gap of document ID of a posting; d
                        outputStream.writeInt(posting.getDocumentId() - previousDocID);
                        //Writing weight of @term, @posting's document Id; wdt
                        outputStream.writeDouble(1 + Math.log(posting.getPositions().size()));
                        previousDocID = posting.getDocumentId();
                        //Writing Number of positions for given posting; tftd
                        outputStream.writeInt(posting.getPositions().size());
                        int previousPosition = 0;
                        for (Integer position : posting.getPositions()) {
                            //Writing gap of positions of posting; p
                            outputStream.writeInt(position - previousPosition);
                            previousPosition = position;
                        }
                    }
                }
                outputStream.close();
                outputStreamForTerms.close();
                db.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return locations;
    }


    public List<Long> writeKgramIndex(Map<String,List<String>> kgramIndex)
    {
        List<Long> locations = new LinkedList<>();
        File kgramsFile = new File(path,"kgrams.bin");
        File mapDBFile = new File(path,"kgrams.db");
        kgramsFile.getParentFile().mkdirs();
        if(kgramsFile.exists()) {
            kgramsFile.delete();
            mapDBFile.delete();
        }
        List<String> sortedKgrams = new ArrayList<>(kgramIndex.keySet());
        Collections.sort(sortedKgrams);
        try (DB db = DBMaker
                .fileDB(path + File.separator + "kgrams.db")
                .fileMmapEnable()
                .make()) {
            ConcurrentMap<String, Long> diskIndex = db
                    .treeMap("vocabToAddress", Serializer.STRING, Serializer.LONG)
                    .create();
            DataOutputStream outputStream;
            try {
                outputStream = new DataOutputStream(new FileOutputStream(kgramsFile));
                kgramsFile.createNewFile();
                for (String kgram : sortedKgrams) {
                    if(kgram.equals("$"))
                    {
                        continue;
                    }
                    List<String> termsPeKgram = kgramIndex.get(kgram);
                    //Writing current stream location to output list and dictionary of kgram to address
                    locations.add((long) outputStream.size());
                    diskIndex.put(kgram, (long) outputStream.size());
                    // <#_of_termsPerkgram  <length_of_term  term>>
                    //Writing Number of terms for given kgram
                    outputStream.writeInt(termsPeKgram.size());
                    for (String term : termsPeKgram) {
                        byte arr[]=term.getBytes(StandardCharsets.UTF_8);
                        //Writing size of term;
                        outputStream.writeInt(arr.length);
                        outputStream.write(arr);
                    }
                }
                outputStream.close();
                db.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return locations;
    }
/*
    public List<Long> writeSoundexIndex(SoundexIndex soundexindex){
        List<Long> locations= new LinkedList<>();
        File postingsFile=new File(path,"SoundexPostings.bin");
        File mapDBFile=new File(path,"soundexPositions.db");
        postingsFile.getParentFile().mkdir();
        if(postingsFile.exists()){
            postingsFile.delete();
            mapDBFile.delete();
        }
        Map<String,List<Posting>> soundexPostingsIndex=soundexindex.getIndex();
        List<String> sortedTerms=new ArrayList<>(soundexindex.getIndex().keySet());
        Collections.sort(sortedTerms);
        try(DB db= DBMaker.fileDB(path + File.separator + "soundexPositions.db").fileMmapEnable().make()) {
            ConcurrentMap<String, Long> diskIndex = db.hashMap("address", Serializer.STRING, Serializer.LONG).create();

            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(postingsFile))) {
                postingsFile.createNewFile();
                for (String term : sortedTerms) {
                    List<Posting> postingList = soundexPostingsIndex.get(term);
                    locations.add((long) outputStream.size());
                    diskIndex.put(term,(long)outputStream.size());
                    outputStream.writeInt(postingList.size());
                    int previousDocId=0;
                    for(Posting posting: postingList){
                        outputStream.writeInt(posting.getDocumentId()-previousDocId);
                        previousDocId=posting.getDocumentId();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;

    }
   */
    /*

    /**
     * Writes
     * @param lengths of documents to docWeights.bin file
     */
    public void writeWeightOfDocuments(List<Double> lengths){
        File file = new File(path,"docWeights.bin");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file))) {
            for(Double length:lengths) {
                outputStream.writeDouble(length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeVocabularyToDisk(List<String> vocabList)
    {
        try {
            File vocabFile=new File(path,"vocabulary.bin");
            if(vocabFile.exists())
                vocabFile.delete();
            else
            vocabFile.getParentFile().mkdirs();
            DataOutputStream vocabOutputStream=new DataOutputStream(new FileOutputStream(vocabFile));
            for(String token:vocabList)
            {
                //Convert token to bytes
                byte arr[] = token.trim().getBytes(StandardCharsets.UTF_8);
                //write size of token
                vocabOutputStream.writeInt(arr.length);
                //Writing token to vocabulary.bin;
                vocabOutputStream.write(arr);
            }
            vocabOutputStream.close();
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}