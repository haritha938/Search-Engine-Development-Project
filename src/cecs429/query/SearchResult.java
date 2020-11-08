package cecs429.query;

import java.util.List;

/**
 * stores the ranked search result and spelling correction of query
 */
public class SearchResult {
    String suggestedString;
    List<Accumulator> searchResults;

    public SearchResult(String suggestedString, List<Accumulator> searchResults) {
        this.suggestedString = suggestedString;
        this.searchResults = searchResults;
    }

    public String getSuggestedString() {
        return suggestedString;
    }

    public List<Accumulator> getSearchResults() {
        return searchResults;
    }
}
