package cecs429.documents;

import com.google.gson.Gson;

import javax.xml.catalog.Catalog;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonFileDocument implements FileDocument{
    private int mDocumentId;
    private Path mFilePath;
    private String title;
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
