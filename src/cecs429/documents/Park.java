package cecs429.documents;

import java.nio.file.Path;

public class Park {
    // creating Park json file field variables
    String body;
    String url;
    String title;
    Path documentPath;

    public Park(String body, String url, String title, Path documentPath) {
        this.body = body;
        this.url = url;
        this.title = title;
        this.documentPath = documentPath;
    }
    // get and set methods on instance variables
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDocumentName(){return String.valueOf(documentPath.getFileName());}

    public  void setDocumentPath(Path documentPath){this.documentPath = documentPath;}
}