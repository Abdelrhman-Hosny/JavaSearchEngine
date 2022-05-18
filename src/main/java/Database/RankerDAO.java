package Database;
import java.sql.*;
import java.util.HashMap;

import org.jsoup.internal.StringUtil;

import Indexer.Word;

public class RankerDAO extends BaseDAO {
    
    public ResultSet SearchWordIndex(String word , String doc) {
        CallableStatement cstmt;
        try {


            cstmt = connection.prepareCall("{call getWordInfo(?,?)}");
            cstmt.setString(1, word); 
            cstmt.setString(2, doc); 
            
            
            ResultSet rs =  cstmt.executeQuery(); // return resultSet
            return rs ;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getNumberOfDocumentsForWord(String word ) {
        CallableStatement cstmt;
        try {


            cstmt = connection.prepareCall("{call numberOfDocumentsForWord(?)}");
            cstmt.setString(1, word); 
            ResultSet rs =  cstmt.executeQuery(); // return resultSet
            int result = 0;
            while(rs.next()){
                result = rs.getInt("total");
            }
            rs.close();
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getTotalNumberOfDocument() {
        CallableStatement cstmt;
        try {


            cstmt = connection.prepareCall("{call totalNumberOfDocuments}");
            ResultSet rs =  cstmt.executeQuery(); // return resultSet
            int result = 0;
            while(rs.next()){
                result = rs.getInt("numberOfDocuments");
            }
            rs.close();
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet GetDocumentsWhereAllWordsAppears(String finalWords){
        String queryGetDocumentsWhereAllWordsAppears = "SELECT  document_name" +
        " FROM IndexTable" + " where word in " + finalWords ;
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            return stmt.executeQuery(queryGetDocumentsWhereAllWordsAppears);
        } catch (SQLException e) {
            e.printStackTrace();
            return (ResultSet) e;
        }
    }
    public void addPageRank(HashMap<String,Double> pageRank){
        try {
            // since we can do incremental update
            // so we need to check that we delete url before working due to some reasons:
            // as page is updated and maybe words are outdated
            // will have same (word,url) combination which will damage primary key constraints
            CallableStatement cstmt;
            cstmt = connection.prepareCall("{call addPageRank(?,?)}");
            for (String key : pageRank.keySet()) {

                cstmt.setString(1, key);
                cstmt.setDouble(2, pageRank.get(key));

                if(cstmt.execute()){
                    cstmt.close();
                }
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RankerDAO rk = new RankerDAO();
        System.out.println(rk.getTotalNumberOfDocument());
    }
}
