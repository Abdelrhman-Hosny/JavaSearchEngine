package Database;
import java.sql.*;
import java.util.HashMap;

import Indexer.Word;

public class IndexerDAO extends BaseDAO {
    
    public boolean InsertWordIndex(HashMap<String,Word> allWords , String url, int documentSize) {
        try {
            // since we can do incremental update
            // so we need to check that we delete url before working due to some reasons:
            // as page is updated and maybe words are outdated
            // will have same (word,url) combination which will damage primary key constraints
            delete_rollBack_Url(url);
       
            CallableStatement cstmt;
            cstmt = connection.prepareCall("{call AddIndex_Entry(?,?,?,?,?,?,?,?,?,?)}");
            for (String key : allWords.keySet()) {

                // values (@inWord,@inDocument_name,@inTitle,
                // @inH1,@inH2,@inH3,@inH4,@inBold,@inText);
                
                cstmt.setString(1, key);
                cstmt.setString(2, url);
                cstmt.setInt(3, allWords.get(key).title);
                cstmt.setInt(4, allWords.get(key).h1);
                cstmt.setInt(5, allWords.get(key).h2);
                cstmt.setInt(6, allWords.get(key).h3);
                cstmt.setInt(7, allWords.get(key).h4_6);
                cstmt.setInt(8, allWords.get(key).bold);
                cstmt.setInt(9, allWords.get(key).text);
                cstmt.setFloat(10, allWords.get(key).count /(float) documentSize );

                if(cstmt.execute()){
                    cstmt.close();
                    return true;
                }
            }
        } catch (SQLException e) {
            //TODO :: we need to roll back ?? so will delete all rows of same url in database
            delete_rollBack_Url(url);
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete_rollBack_Url(String url) {
        CallableStatement cstmt;
        try {
            cstmt = connection.prepareCall("{call DeleteURLEntries(?)}");
            cstmt.setString(1, url);    
            if(cstmt.execute()){
                cstmt.close();
                return true;
            }
            
        } catch (SQLException e) {
            //TODO :: we need to roll back ?? so will delete all rows of same url in database
            e.printStackTrace();
        }
        return false;
    }

    public boolean InsertDocument(String url, String title, String snippet) {
        try {

            CallableStatement cstmt;
            cstmt = connection.prepareCall("{call Insert_Document(?,?,?)}");
            cstmt.setString(1, url);
            cstmt.setString(2, title);
            cstmt.setString(3, snippet);

            
            if(cstmt.execute()){
                cstmt.close();
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ResponseObject Retrieve_Document(String url) {
        CallableStatement cstmt;
        try {
            

            cstmt = connection.prepareCall("{call Retrieve_Document(?)}");
            cstmt.setString(1, url); 
            
            ResultSet rs =  cstmt.executeQuery(); // return resultSet
            String title = "" , snippet ="";
            // list of queries to be returned
            while(rs.next()){
                // added quotations to make json
                title = rs.getString("title");
                snippet = rs.getString("snippet");
            }
            // cleaning up
            rs.close();
            cstmt.close();
            return new ResponseObject(url,title,snippet);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args) {
        IndexerDAO in = new IndexerDAO();
        ResponseObject rs = in.Retrieve_Document("url");

    }
}
