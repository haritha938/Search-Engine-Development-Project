package cecs429.index;

import java.io.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class DiskIndexWriter {
    /* sql operations */
    private  Connection connect=null;
    private  Statement statement=null;
    private  ResultSet resultSet=null;
    private  String url="jdbc:mysql://localhost:3306/bplustree";
    private  String user="root", pass="";
    public List<Integer> writeIndex(Index index, Path path){
        List<Integer> locations = new LinkedList<>();
        File file = path.toFile();
        file.getParentFile().mkdirs();

        // Testing Sql operations.
        sqlOperations();

        try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file))){
            file.createNewFile();
            Map<String, List<Posting>> positionalInvertedIndex = index.getIndex();
            List<String> sortedTerms = new ArrayList<>(index.getIndex().keySet());
            Collections.sort(sortedTerms);
            for(String term: sortedTerms){
                List<Posting> postingList = positionalInvertedIndex.get(term);
                //Adding Term start location to output list
                locations.add(outputStream.size());
                //Writing Number of postings for given term
                outputStream.writeInt(postingList.size());
                int previousDocID=0;
                for(Posting posting:postingList){
                    //Writing document ID of a posting
                    outputStream.writeInt(posting.getDocumentId()-previousDocID);
                    //Writing Number of positions for given posting
                    previousDocID=posting.getDocumentId();
                    outputStream.writeInt(posting.getPositions().size());
                    int previousPosition=0;
                    for(Integer position:posting.getPositions()){
                        //Writing positions of posting
                        outputStream.writeInt(position-previousPosition);
                        previousPosition=position;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;
    }
    public  void sqlOperations() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connect = DriverManager
                    .getConnection(url, user, pass);
            statement = connect.createStatement();
            resultSet = statement
                    .executeQuery("select * from bptree;");

            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String term = resultSet.getString(2);
                long address = resultSet.getLong(3);
                System.out.println("id: " + id + " term: " + term + " address: " + address);

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // Setup the connection with the DB
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}