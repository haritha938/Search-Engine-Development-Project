package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.List;

public class NotQuery implements Query {
    private Query mChild;


    public NotQuery(Query child) {
        mChild = child;
    }
    @Override
    public List<Posting> getPostings(Index index) {

        return mChild.getPostings(index);
    }

    @Override

    public boolean isNegativeQuery() {
        return true;
    }
}
