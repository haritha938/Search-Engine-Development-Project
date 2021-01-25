package cecs429.classification;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.Posting;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class RocchioClassification {
    String path;
    String hamiltonPath;
    String jayPath;
    String madisonPath;
    String disputedPath;
    Index hamiltonIndex;
    Index jayIndex;
    Index madisonIndex;
    Index disputedIndex;
    Set<String> vocabOfAllClasses;
    Map<String,CentroidOfClass> classToCentroid;

    public RocchioClassification(String path){
        this.path = path;
        this.hamiltonPath = path + File.separator + "HAMILTON";
        this.jayPath = path + File.separator + "JAY";
        this.madisonPath = path + File.separator + "MADISON";
        this.disputedPath = path + File.separator + "DISPUTED";
        hamiltonIndex = new DiskPositionalIndex(hamiltonPath+File.separator+"index");
        jayIndex = new DiskPositionalIndex(jayPath+File.separator+"index");
        madisonIndex = new DiskPositionalIndex(madisonPath+File.separator+"index");
        disputedIndex = new DiskPositionalIndex(disputedPath+File.separator+"index");
        vocabOfAllClasses = new HashSet<>();
        classToCentroid = new HashMap<>();
    }

    public Map<String, String> classify(){

        mergeVocabs();
        classToCentroid.put("Hamilton",calculateHamiltonCentroid());
        classToCentroid.put("Jay",calculateJayCentroid());
        classToCentroid.put("Madison",calculateMadisonCentroid());

        Map<Integer,Map<String,Double>> disputedDocumentVectors = new HashMap<>();
        Map<String,String> result = new HashMap<>();
        for(String vocab:vocabOfAllClasses){
            List<Posting> postings = disputedIndex.getPostingsWithOutPositions(vocab);
            if(postings==null)
                continue;
            for(Posting posting:postings){
                Map<String,Double> documentVector = disputedDocumentVectors.getOrDefault(posting.getDocumentId(), getVector());
                documentVector.put(vocab, posting.getWdt()/disputedIndex.getDocLength(posting.getDocumentId()));
                disputedDocumentVectors.put(posting.getDocumentId(),documentVector);
            }
        }
        DocumentCorpus disputedCorpus = DirectoryCorpus.loadDirectory(Paths.get(disputedPath).toAbsolutePath());
        disputedCorpus.getDocuments();
        for(Integer documentId: disputedDocumentVectors.keySet()){
            double similarity = Double.MAX_VALUE;
            for(String classification:classToCentroid.keySet()){
                double temp=0.0;
                for(String vocab:vocabOfAllClasses){
                    temp+=Math.pow((disputedDocumentVectors.get(documentId).get(vocab))
                                -classToCentroid.get(classification).getCentroid().get(vocab),2);
                }
                //temp/=classToCentroid.get(classification).getLengthOfCentroidVector();
                System.out.println("Dist to /"+classification+" for doc "+disputedCorpus.getDocument(documentId).getDocumentName()+" is "+Math.sqrt(temp));
                if(temp<similarity){
                    similarity = temp;
                    result.put(disputedCorpus.getDocument(documentId).getDocumentName(),classification);
                }
            }
            System.out.println("Low distance for "+disputedCorpus.getDocument(documentId).getDocumentName()+" is /"+result.get(disputedCorpus.getDocument(documentId).getDocumentName())+"\n");
        }
        return result;
    }

    CentroidOfClass calculateHamiltonCentroid(){
        DocumentCorpus hamiltonCorpus = DirectoryCorpus.loadDirectory(Paths.get(hamiltonPath).toAbsolutePath());
        Map<String,Double> centroid = new HashMap<>();
        for(String vocab:vocabOfAllClasses){
            centroid.put(vocab,0.0);
        }
        double length=0.0;
        for(String vocab:vocabOfAllClasses){
            List<Posting> postings = hamiltonIndex.getPostingsWithOutPositions(vocab);
            if(postings==null)
                continue;
            double normalizedVectorComponent=0;
            for(Posting posting:postings){
                normalizedVectorComponent+=posting.getWdt()/ hamiltonIndex.getDocLength(posting.getDocumentId());
            }
            centroid.put(vocab,normalizedVectorComponent/hamiltonCorpus.getCorpusSize());
            length+=Math.pow(normalizedVectorComponent/hamiltonCorpus.getCorpusSize(),2);

        }
        return new CentroidOfClass(centroid,Math.sqrt(length));
    }


    CentroidOfClass calculateJayCentroid(){
        DocumentCorpus jayCorpus = DirectoryCorpus.loadDirectory(Paths.get(jayPath).toAbsolutePath());
        Map<String,Double> centroid = new HashMap<>();
        for(String vocab:vocabOfAllClasses){
            centroid.put(vocab,0.0);
        }
        double length=0.0;
        for(String vocab:vocabOfAllClasses){
            List<Posting> postings = jayIndex.getPostingsWithOutPositions(vocab);
            if(postings==null)
                continue;
            double normalizedVectorComponent=0;
            for(Posting posting:postings){
                normalizedVectorComponent+=posting.getWdt()/jayIndex.getDocLength(posting.getDocumentId());
            }
            centroid.put(vocab,normalizedVectorComponent/jayCorpus.getCorpusSize());
            length+=Math.pow(normalizedVectorComponent/jayCorpus.getCorpusSize(),2);

        }
        return new CentroidOfClass(centroid,Math.sqrt(length));
    }

    CentroidOfClass calculateMadisonCentroid(){
        DocumentCorpus madisonCorpus = DirectoryCorpus.loadDirectory(Paths.get(madisonPath).toAbsolutePath());
        Map<String,Double> centroid = new HashMap<>();
        for(String vocab:vocabOfAllClasses){
            centroid.put(vocab,0.0);
        }
        double length=0.0;
        for(String vocab:vocabOfAllClasses){
            List<Posting> postings = madisonIndex.getPostingsWithOutPositions(vocab);
            if(postings==null)
                continue;
            double normalizedVectorComponent=0;
            for(Posting posting:postings){
                normalizedVectorComponent+=posting.getWdt()/madisonIndex.getDocLength(posting.getDocumentId());
            }
            centroid.put(vocab,normalizedVectorComponent/madisonCorpus.getCorpusSize());
            length+=Math.pow(normalizedVectorComponent/madisonCorpus.getCorpusSize(),2);
        }
        return new CentroidOfClass(centroid,Math.sqrt(length));
    }

    Map<String, Double> getVector(){
        Map<String,Double> vector = new HashMap<>();
        for(String vocab:vocabOfAllClasses){
            vector.put(vocab,0.0);
        }
        return vector;
    }

    void mergeVocabs(){
        vocabOfAllClasses.addAll(disputedIndex.getTerms());
        vocabOfAllClasses.addAll(hamiltonIndex.getTerms());
        vocabOfAllClasses.addAll(madisonIndex.getTerms());
        vocabOfAllClasses.addAll(jayIndex.getTerms());
    }

    private static class CentroidOfClass{
        Map<String, Double> centroid;
        Double lengthOfCentroidVector;

        public CentroidOfClass(Map<String, Double> centroid, Double lengthOfCentroidVector) {
            this.centroid = centroid;
            this.lengthOfCentroidVector = lengthOfCentroidVector;
        }

        public Map<String, Double> getCentroid() {
            return centroid;
        }

    }
}
