package cecs429.index;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        postingsFile.getParentFile().mkdir();
        if(postingsFile.exists()){
            postingsFile.delete();
            mapDBFile.delete();
        }

        List<String> sortedTerms= soundexindex.getTerms();
        //System.out.println(sortedTerms.size());
        Collections.sort(sortedTerms);
        try(DB db= DBMaker.fileDB(path + File.separator + "soundexPositions.db").fileMmapEnable().make()) {
            ConcurrentMap<String, Long> diskIndex = db.hashMap("address", Serializer.STRING, Serializer.LONG).create();

            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(postingsFile))) {
                postingsFile.createNewFile();
                for (String term : sortedTerms) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;

    }
}
