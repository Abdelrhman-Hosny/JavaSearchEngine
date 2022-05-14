package Database;
import java.sql.*;
import java.util.HashMap;

import Indexer.Word;

public class IndexerDAO extends BaseDAO {
    
    public boolean InsertWordIndex(HashMap<String,Word> allWords , String url) {
        CallableStatement cstmt;
        try {
            cstmt = connection.prepareCall("{call Add_Index_Entry(?,?,?,?,?,?,?,?,?)}");
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

                if(cstmt.execute()){
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
                return true;
            }
            
        } catch (SQLException e) {
            //TODO :: we need to roll back ?? so will delete all rows of same url in database
            e.printStackTrace();
        }
        return false;
    }
}
