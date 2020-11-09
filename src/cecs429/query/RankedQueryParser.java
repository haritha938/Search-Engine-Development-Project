package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.TermDocumentIndex;
import cecs429.text.TextUtilities;
import cecs429.text.TokenProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    int thresholdCheck;
    float jaccardCoefficintThreshold;
    public RankedQueryParser(Index index, int sizeOfCorpus, String path, TokenProcessor tokenProcessor) {
        this.diskIndex = index;
        this.sizeOfCorpus = sizeOfCorpus;
        this.path = path;
        this.tokenProcessor = tokenProcessor;
        readProperties();
    }

    public SearchResult getPostings(String query) {
        StringBuilder querySuggestion = new StringBuilder();
        SearchResult searchAcknowledgment;
        TextUtilities textUtilities = new TextUtilities(tokenProcessor);
        List<Accumulator> rankedSearchResult = new ArrayList<>();
        File file = new File(path, "docWeights.bin");
        Queue<Accumulator> rankedPostings = new PriorityQueue<>(Comparator.comparingDouble(a -> -1 * a.priority));
        Map<Integer, Accumulator> documentAccumulatorMap = new HashMap<>();
        String[] queryTerms = query.split(" +");
        for (String searchToken : queryTerms) {
            List<Posting> postingList = null;
            if (!searchToken.contains("*")) {
                postingList = new TermLiteral(searchToken,tokenProcessor,false).getPostingsWithoutPositions(diskIndex);
                if (postingList == null) {
                    String suggestion = textUtilities.getSuggestion(searchToken, diskIndex, 0, jaccardCoefficintThreshold);
                    if(suggestion.equals(""))
                        querySuggestion.append(" ").append(searchToken);
                    else
                        querySuggestion.append(" ").append(suggestion);
                    continue;
                } else if (postingList.size() < thresholdCheck) {
                    String suggestion = textUtilities.getSuggestion(searchToken, diskIndex, postingList.size(), jaccardCoefficintThreshold);
                    if(suggestion.equals(""))
                        querySuggestion.append(" ").append(searchToken);
                    else
                        querySuggestion.append(" ").append(suggestion);
                } else {
                    querySuggestion.append(" ").append(searchToken);
                }
            }else {
                Query q = new WildcardLiteral(searchToken.trim(), tokenProcessor, false);
                postingList = q.getPostingsWithoutPositions(diskIndex);
                querySuggestion.append(" ").append(searchToken);
                if(postingList == null)
                    continue;
            }
            double wqt = Math.log(1 + (((double) sizeOfCorpus) / postingList.size()));
            for (Posting posting : postingList) {
                Accumulator accumulator = documentAccumulatorMap.getOrDefault(
                        posting.getDocumentId()
                        , new Accumulator(posting.getDocumentId()));
                accumulator.setPriority(accumulator.getPriority() + (wqt * posting.getWdt()));
                documentAccumulatorMap.put(posting.getDocumentId(), accumulator);
            }
        }

        for (Integer documentID : documentAccumulatorMap.keySet()) {
            Accumulator accumulator = documentAccumulatorMap.get(documentID);
            accumulator.setPriority(accumulator.getPriority() / diskIndex.getDocLength(documentID));
            rankedPostings.add(accumulator);
        }

        for (int i = 0; i < Math.min(limit, documentAccumulatorMap.size()); i++) {
            rankedSearchResult.add(rankedPostings.poll());
        }
        searchAcknowledgment = new SearchResult(querySuggestion.toString().trim(), rankedSearchResult);
        return searchAcknowledgment;
    }

    public void readProperties(){

        try(InputStream in = getClass().getResourceAsStream("/Application.properties")){
            Properties prop = new Properties();
            prop.load(in);
            limit = Integer.parseInt(prop.getProperty("numberOfRetrievalsForRankedQueries"));
            thresholdCheck = Integer.parseInt(prop.getProperty("documentFrequencyThreshold"));
            jaccardCoefficintThreshold = Float.parseFloat(prop.getProperty("jaccardCoefficientThreshold"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
