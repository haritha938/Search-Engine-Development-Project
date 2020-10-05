package cecs429.documents;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

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
            title = gson.fromJson(reader, Park.class).getTitle();
        }catch(Exception e){
            e.printStackTrace();
        }



    }

    @Override
    public Boolean hasAuthor() {
        return null;
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Reader getContent() {
        Reader stringReader=null;
        try(Reader reader = Files.newBufferedReader(mFilePath)) {
            Gson gson = new Gson();
            stringReader = new StringReader(gson.fromJson(reader, Park.class).getBody());
        }catch(Exception e){
            e.printStackTrace();
        }
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
