package cecs429.query;

/**
 * Accumulator is used to track priority of each document created in ranked search mode
 */
public class Accumulator {
    int documentId;
    double priority;

    public Accumulator(int documentId) {
        this.documentId = documentId;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public int getDocumentId() {
        return documentId;
    }
}
