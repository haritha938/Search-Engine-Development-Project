package cecs429.documents;

public class Article {
    String url;
    String title;
    String subtitle;
    String body;
    String author;

    public Article(String url, String title, String subtitle, String body, String author) {
        this.body = body;
        this.url = url;
        this.title = title;
        this.subtitle=subtitle;
        this.author= author;
    }

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

    public void setAuthor(String author){
        this.author=author;
    }
    public String getAuthor(){
        return author;
    }
}

