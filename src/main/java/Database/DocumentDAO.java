package Database;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DocumentDAO extends BaseDAO{
    
    public ResultSet GetallDocumentswithUrls(String finalWords){
        String queryGetDocumentsWhereAllWordsAppears = "SELECT  *" +
        " FROM DocumentsTable" + " where document_name in " + finalWords ;
        
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

        

    // public static void main(String[] args) throws SQLException {
    //     DocumentDAO db = new DocumentDAO() ;
    //     ResultSet rs =db.GetPhraseLevelDocumentswithUrls("counter course");
    //     while(rs.next()){
    //         System.out.println(rs.getString("document_name"));
    //     }

    // }
}
