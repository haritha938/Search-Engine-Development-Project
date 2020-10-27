package cecs429.index;

import DAO.TermToAddressDao;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class DiskIndexWriter {

    String path;
    PriorityQueue<Integer> pq;
    public DiskIndexWriter(String path){
        this.path = path;
        pq=new PriorityQueue<Integer>(10, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if(o1<o2)
                    return 1;
                else if(o1.equals(o2))
                        return 0;
                return -1;
            }
        });

    }

    public List<Integer> writeIndex(Index index){
        List<Integer> locations = new LinkedList<>();

        File file = new File(path+File.separator+"Postings.bin");
        file.getParentFile().mkdirs();
        TermToAddressDao termToAddressDao = new TermToAddressDao();
        Map<String, List<Posting>> positionalInvertedIndex = index.getIndex();
        List<String> sortedTerms = new ArrayList<>(index.getIndex().keySet());
        Collections.sort(sortedTerms);
        DB db = DBMaker
                .fileDB(path+File.separator+"index.db")
                .fileMmapEnable()
                .make();
        ConcurrentMap<String,Long> diskIndex = db
                .hashMap("diskIndex", Serializer.STRING, Serializer.LONG)
                .create();
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file))){
            file.createNewFile();

            for(String term: sortedTerms){
                List<Posting> postingList = positionalInvertedIndex.get(term);
                //Adding Term start location to output list
                locations.add(outputStream.size());
                diskIndex.put(term, (long)outputStream.size());
                //Writing Number of postings for given term
                outputStream.writeInt(postingList.size());
                int previousDocID=0;
                for(Posting posting:postingList){
                    //Writing document ID of a posting
                    outputStream.writeInt(posting.getDocumentId()-previousDocID);
                    //Writing Number of positions for given posting
                    previousDocID=posting.getDocumentId();
                    outputStream.writeInt(posting.getPositions().size());
                    int previousPosition=0;
                    for(Integer position:posting.getPositions()){
                        //Writing positions of posting
                        outputStream.writeInt(position-previousPosition);
                        previousPosition=position;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.close();
        return locations;
    }

    public void writeLengthOfDocument(List<Double> lengths){
        File file = new File(path+File.separator+"docWeights.bin");
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
}