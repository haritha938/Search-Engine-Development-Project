package cecs429.index;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class DiskIndexWriter {
    public List<Integer> writeIndex(Index index, Path path){
        List<Integer> locations = new LinkedList<>();
        File file = path.toFile();
        file.getParentFile().mkdirs();
        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file))){
            file.createNewFile();
            Map<String, List<Posting>> positionalInvertedIndex = index.getIndex();
            List<String> sortedTerms = new ArrayList<>(index.getIndex().keySet());
            Collections.sort(sortedTerms);
            for(String term: sortedTerms){
                List<Posting> postingList = positionalInvertedIndex.get(term);
                //Adding Term start location to output list
                locations.add(outputStream.size());
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
        return locations;
    }
}