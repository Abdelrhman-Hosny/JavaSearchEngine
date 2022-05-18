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

    // public static void main(String[] args) throws SQLException, InterruptedException, IOException {
        // Object[] entryArray =rankerObj.process("algorithm");
        // String finalWords = "(";
        // for (int i = 0; i < entryArray.length; i++) {
        //     finalWords += "'"+ ((Entry) entryArray[i]).getKey() +"'"+ ",";
        // }
        // int index = finalWords.lastIndexOf(',');
        // finalWords = finalWords.substring(0,index);
        // finalWords += ")";
        // ResultSet rs =db.GetallDocumentswithUrls("('https://www.geeksforgeeks.org','https://www.geeksforgeeks.org/minimize-cost-to-reach-bottom-right-corner-of-a-matrix-using-given-operations/?ref=rp')");
        // while(rs.next()){
        //     System.out.println(rs.getString("document_name"));
        // }
    // }
}
