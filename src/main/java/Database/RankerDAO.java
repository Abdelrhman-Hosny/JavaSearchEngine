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

}
