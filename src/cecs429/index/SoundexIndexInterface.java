package cecs429.index;

import java.util.List;
import java.util.Map;

public interface SoundexIndexInterface {


    /**
     * Retrieves a list of Postings of documents that contain the given term without positions
     */
    List<Posting> getPostingsWithOutPositions(String term);



    //Todo: Instead of getIndex  use getTerms In entire code
    List<String> getTerms();

}
