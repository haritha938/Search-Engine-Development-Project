package cecs429.index;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class SoundexDiskIndexWriter {
    String path;
    public SoundexDiskIndexWriter(String path){
        this.path = path;
    }

    public List<Long> writeSoundexIndex(SoundexIndex soundexindex){
        List<Long> locations= new LinkedList<>();
        File postingsFile=new File(path,"SoundexPostings.bin");
        File mapDBFile=new File(path,"soundexPositions.db");
        File termsFile = new File(path,"soundexHashTerms.bin");
        postingsFile.getParentFile().mkdir();
        if(postingsFile.exists())
            postingsFile.delete();
        if(mapDBFile.exists())
            mapDBFile.delete();
        if(termsFile.exists())
            termsFile.delete();
        try {
            postingsFile.createNewFile();
            termsFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> sortedTerms= soundexindex.getTerms();
        //System.out.println(sortedTerms.size());
        Collections.sort(sortedTerms);
        try(DB db= DBMaker.fileDB(path + File.separator + "soundexPositions.db").fileMmapEnable().make();
            DataOutputStream outputStreamForTerms = new DataOutputStream(new FileOutputStream(termsFile));
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(postingsFile))) {
            ConcurrentMap<String, Long> diskIndex = db.treeMap("address", Serializer.STRING, Serializer.LONG).create();
            for (String term : sortedTerms) {
                byte arr[]=term.getBytes(StandardCharsets.UTF_8);
                //write size of term
                outputStreamForTerms.writeInt(arr.length);
                //Writing term to terms.bin;
                outputStreamForTerms.write(arr);
                List<Posting> postingList = soundexindex.getPostinglist(term);
                if(postingList!=null){
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
}
