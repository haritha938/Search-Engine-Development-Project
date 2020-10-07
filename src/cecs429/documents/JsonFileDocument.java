package cecs429.documents;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a document that is saved as a simple JSON file in the local file system.
 */
public class JsonFileDocument implements FileDocument{
    private int mDocumentId;
    private Path mFilePath;
    private String title;
    private String url;
    private String body;
    private String author;


    /**
     * Constructs a JsonFileDocument with the given document ID representing the file at the given
     * absolute file path.
     */
    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
        try(Reader reader = Files.newBufferedReader(mFilePath)) {
            Gson gson = new Gson();
            // Checking if the json files has author field using hasAuthor method
            if(!hasAuthor()){
                // if the json file does not has author field, then we make park object and initialize its respective fields in the object
                Park park=gson.fromJson(reader,Park.class);
                title = park.getTitle();
                url= park.getUrl();
                body=park.getBody();
                park.setDocumentPath(mFilePath);
            }
            else{
                // if the json file has author field, then we make Article object and assign its respective fields in the object.
                Article article;
                article=gson.fromJson(reader,Article.class);
                title=article.getTitle();
                url=article.getUrl();
                author=article.getAuthor();
                body=article.getBody();
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }
    // hasAuthor method to check if author field is present in the json file. If yes, return true else it returns false
    @Override
    public Boolean hasAuthor() {
        try (Reader reader = Files.newBufferedReader(mFilePath)) {
            Gson gson = new Gson();
            author = gson.fromJson(reader, Article.class).getAuthor();
            if (author != null) {
                return true;
            }
            else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
        }        return false;
    }
    // return the document name of the file
    @Override
    public String getDocumentName() {
        return String.valueOf(mFilePath.getFileName());
    }
    // returns the Reader object to the author first name and last name
    public Reader getAuthor(){
        Reader stringReader=null;
        stringReader=new StringReader(author);
        return stringReader;
    }
    // returns the file path
    @Override
    public Path getFilePath() {
        return mFilePath;
    }
    // returns the document id of the file
    @Override
    public int getId() {
        return mDocumentId;
    }
    // returns the reader object of json file body field.
    @Override
    public Reader getContent() {
        Reader stringReader=null;
        stringReader=new StringReader(body);
        return stringReader;
    }

    @Override
    public String getTitle() {
        return title;
    }
    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JsonFileDocument(documentId, absolutePath);
    }
}
