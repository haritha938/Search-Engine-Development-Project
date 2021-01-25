# Search Engine Development Project
# Search engine project CECS 529

## Goal: 
Index the corpus on the disk. Present results to the user based on the below option the user select.

* boolean search
* Ranked search
* Text classification
* Inexact retrieval



## Sub-goals

1. Create a Token processor
2. Create In-memory positional inverted index
3. Boolean query processing
   * And query
   * Or query
   * Not query
   * Phrase query
   * n-grams and Wildcard queries
   * author query(special query Soundex search)
4. Disk-memory positional inverted index (with weight!)
5. Ranked Retrieval
6. Spelling correction
7. Text classification
8. In-exact retrieval - vocab elimination

```
The search engine's fundamental is mapping the key to a value where the key is a tokenized word and value is a list of documents with other information.
```

### Create Token processor
Using Porter2 stemmer and a few more rules, we shall create keys for Information retrieval.
### Create In-memory positional inverted index
Each document in the corpus shall be provided with a docId in the form of an increasing manner.
Each tokenized word in the corpus shall be paired with the docIds that consist of the tokenized words.
### Boolean query processing
In this mode, results are retrieved only if a document in the corpus satisfies all the boolean query criteria answered by the user.
A query can be in the form of:


Term literal -> a simple one-word

Phrase literal -> "term literal (term literal)+"

Wildcard literal -> term literal with leading, trailing or embedded * characters

Search Token -> term literal | phrase literal | wildcard literal

AND queries -> ([Search Token]+[Search Token])+

>park+evening
 
"+" denotes OR operation, looks for either park or evening

OR queries -> [Search Token] <space character> [Search Token]
>park evening
 
" " denotes OR operation, looks for either park or evening

NOT query -> -[Search Token]
>park - evening.  
 
"-" denotes NOT operation, looks for the park without evening

AUTHOR queries -> :author <name of author without angular brackets>
> E.g., :author Stewart
 
Author queries are provisioned with the Soundex algorithm, where the author can get results of both Stewart and Stuart for the above-searched query.

## Disk-memory positional inverted index (with weight!)
We stored the key, value pair in In-memory till now. Moving forward, we use the disk-based index to save RAM from being exploited.
Each key will be stored in a B+ tree with access to data (d wd,t tftd p1 p2 p3)+ in the disk.

## Ranked retrieval
The user gives a list of words as a search query, and results will be displayed in the order of decreasing relevance to the query.

## Spelling correction
Users will be suggested with an alternate query (only in ranked retrieval mode) if the search results of any single term of the query are zero or less than a threshold limit.

## Text classification
Using two vector-based algorithms, Knn classification and Rocchio classification, we tried to finish the well-known disputed federalist-papers problem. We are glad that our results are similar to the results of other researchers.

## In-exact retrieval
By taking a few metrics into consideration, we shall improve search retrievals speed by compromising a few search results, which should not bother since the deviation in the precision of results is very minute.

## Contributors of this project
CECS 529 (Search Engine Technology) - Fall 2020
1. Haritha Nimmagadda
2. Abhinay Kacham
3. Varun Lingabathini

## Contributing
The development of this project is closed.

## Media
Demo video: [YouTube](https://youtu.be/mEtWmqUqf1w)

## References
[Introduction to Information Retrieval](https://nlp.stanford.edu/IR-book/information-retrieval-book.html)
