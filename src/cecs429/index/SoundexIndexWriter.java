package cecs429.index;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class SoundexIndexWriter {
    String path;
    public SoundexIndexWriter(String path){
        this.path = path;
    }

    public List<Long> writeSouondexIndex(SoundexIndex soundexindex){
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

            try (DataOutputStream outputstream = new DataOutputStream(new FileOutputStream(postingsFile))) {
                postingsFile.createNewFile();
                for (String term : sortedTerms) {
                    List<Posting> postingList = soundexPostingsIndex.get(term);
                    locations.add((long) outputstream.size());
                    diskIndex.put(term,(long)outputstream.size());
                    outputstream.writeInt(postingList.size());
                    int previousDocId=0;
                    for(Posting posting: postingList){
                        outputstream.writeInt(posting.getDocumentId()-previousDocId);
                        previousDocId=posting.getDocumentId();

                    }
                }

            }

            db.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;
    }

}
