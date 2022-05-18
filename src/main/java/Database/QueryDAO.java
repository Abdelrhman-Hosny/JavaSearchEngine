package Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.internal.StringUtil;

public class QueryDAO extends BaseDAO {
    
    public boolean Insert_query(String query) {
        CallableStatement cstmt;
        try {
            // will make it lower case to prevent multiple queries of same content
            // and will normailise white spacees
            query= StringUtil.normaliseWhitespace(query.toLowerCase());

            cstmt = connection.prepareCall("{call AddQuery(?)}");
            cstmt.setString(1, query);    
            if(cstmt.execute()){
                cstmt.close();
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("query already existed");
        }
        return false;
    }

    public List<String> Retrieve_query(String prefix) {
        CallableStatement cstmt;
        try {
            // will make it lower case to prevent multiple queries of same content
            // and will normailise white spacees
            prefix = StringUtil.normaliseWhitespace(prefix.toLowerCase());
            prefix += '%'; // for sake of querying where i need to match first part 
            
            // for ex when user type fo -> match football, foot, ....

            cstmt = connection.prepareCall("{call RetrieveQuery(?)}");
            cstmt.setString(1, prefix); 
            
            ResultSet rs =  cstmt.executeQuery(); // return resultSet
            
            List<String> words = new ArrayList<String>();
            // list of queries to be returned
            while(rs.next()){
                // added quotations to make json
                words.add('\"'+rs.getString("query")+'\"');
            }
            // cleaning up
            rs.close();
            cstmt.close();

            return words;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // public static void main(String[] args) {
    //     QueryDAO querying = new QueryDAO();
    //     // querying.Insert_query("Real Madrid");
    //     // querying.Insert_query("Barclona");
    //     // querying.Insert_query("Barcelona");

        
    //     List<String> result=  querying.Retrieve_query("Bar");
    //     if(result != null){
    //         // check if its not null
    //         System.out.println(result);
    //     }
        
    //     // TODO :: NEED TO HANDLE CLOSING CONNECTION !!!!!!
    //     querying.CloseConnection();

    // }
}
