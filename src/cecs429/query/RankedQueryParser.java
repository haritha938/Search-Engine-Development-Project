package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * It is used to retrieve postings from given search query by parsing and calculating priority
 */
public class RankedQueryParser {
    Index diskIndex;
    int sizeOfCorpus;
    String path;
    int limit;
    TokenProcessor tokenProcessor;
    public RankedQueryParser(Index index, int sizeOfCorpus, String path, int limit, TokenProcessor tokenProcessor){
        this.diskIndex = index;
        this.sizeOfCorpus = sizeOfCorpus;
        this.path = path;
        this.limit = limit;
        this.tokenProcessor = tokenProcessor;
    }
    public List<Accumulator> getPostings(String query){
        List<Accumulator> searchResult = new ArrayList<>();
        File file = new File(path,"docWeights.bin");
        Queue<Accumulator> rankedPostings = new PriorityQueue<>(Comparator.comparingDouble(a -> -1*a.priority));
        Map<Integer,Accumulator> documentAccumulatorMap = new HashMap<>();
        String[] queryTerms=query.split(" +");
        for(String searchToken:queryTerms){
            List<Posting> postingList=null;
            if(!searchToken.contains("*")) {
                String searchTerm = (searchToken.contains("-"))
                        ? tokenProcessor.processToken(searchToken.replaceAll("-", "")).get(0)
                        : tokenProcessor.processToken(searchToken).get(0);

                postingList = diskIndex.getPostingsWithOutPositions(searchTerm);
            }
            else
            {
                String searchTerm = (searchToken.contains("-"))
                        ? searchToken.replaceAll("-", "").trim()
                        : searchToken.trim();
                Query q= new WildcardLiteral(searchTerm.trim(),tokenProcessor,false);
                postingList=q.getPostings(diskIndex);
            }

            if(postingList!=null) {
                double wqt = Math.log(1 + (((double) sizeOfCorpus) / postingList.size()));
                for (Posting posting : postingList) {
                    Accumulator accumulator = documentAccumulatorMap.getOrDefault(
                            posting.getDocumentId()
                            , new Accumulator(posting.getDocumentId()));
                    accumulator.setPriority(accumulator.getPriority() + (wqt * posting.getWdt()));
                    documentAccumulatorMap.put(posting.getDocumentId(), accumulator);
                }


            }
        }
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r")) {
            byte[] doubleBuffer = new byte[8];
            for (Integer documentID : documentAccumulatorMap.keySet()) {
                randomAccessFile.seek((documentID-1)*8);
                randomAccessFile.read(doubleBuffer,0,doubleBuffer.length);
                double docWeight = ByteBuffer.wrap(doubleBuffer).getDouble();
                Accumulator accumulator = documentAccumulatorMap.get(documentID);
                accumulator.setPriority(accumulator.getPriority()/docWeight);
                rankedPostings.add(accumulator);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        for(int i=0;i<Math.min(limit, documentAccumulatorMap.size());i++){
            searchResult.add(rankedPostings.poll());
        }
        return searchResult;
    }
}
