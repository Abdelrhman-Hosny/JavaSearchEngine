import java.sql.*;
import java.lang.Math;
import java.util.Comparator;
import java.util.PriorityQueue;

//public class Priority{
//    public static class Entry implements Comparable<test.Entry> {
//        private String key;
//        private double value;
//
//        public double getValue()
//        {
//            return this.value;
//        }
//
//
//
//        public Entry(String key, double value) {
//            this.key = key;
//            this.value = value;
//        }
//
//    }
//}

public class Ranker {
    //here we will define some general variables that we will change locally


    static String SQLurl = "";
    static String username = "";
    static String pass = "";


    //word_document table info
    static String wordTableName = "WORD_DOCUMENT";
    static String documentWordTable = "DOCUMENT_WORD";
    //document table info
    String documentTableName = "document_data";
    String documentTableIdColumn = "id";


    //this will be used in the priority queue
    public static class Entry {
        private String key;
        private double value;

        public double getValue() {
            return this.value;
        }


        public Entry(String key, double value) {
            this.key = key;
            this.value = value;
        }
    }


//    public static void numberOfDocuments(ResultSet[] rs) throws SQLException {
//        Connection con = DriverManager.getConnection("jdbc:default:connection");
//        Statement stmt = null;
//        String query =
//                "select count(distinct document_name) from"+ wordTableName;
//        stmt = con.createStatement();
//        rs[0] = stmt.executeQuery(query);
//    }
//
//    public void createProcedures(Connection con) throws SQLException
//    {
//        Statement stmtShowNumberOfDocuments = null;
//        String queryTotalNumberOfDocuments=
//                "CREATE PROCEDURE NUMBER_OF_DOCUMENTS()" +
//                        "PARAMETER STYLE JAVA " +
//                        "LANGUAGE JAVA " +
//                        "DYNAMIC RESULT SETS 1 " +
//                        "";
//
//        System.out.println("calling show documents procedure");
//        stmtShowNumberOfDocuments = con.createStatement();
//        stmtShowNumberOfDocuments.execute(queryTotalNumberOfDocuments);
//
//    }


    public static void main(String args[]) throws SQLException {
        //assuming that the connection to the sql server is made
        //and we have a string of words after they are query processed
        //we are going to calculate the tf-idf of the word we are getting through the following queries
        String search = "football mexico";
        String[] searchQuery = search.split(" ");
        //for each word we will compute term frequency

        //add all these words in one statement
        PriorityQueue<Entry> rankerResult = new PriorityQueue<Entry>(new Comparator<Ranker.Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                if (o1.getValue() < o2.getValue())
                    return 1;
                else if (o1.getValue() > o2.getValue())
                    return -1;
                else
                    return 0;
            }
        });


        Connection conn = DriverManager.getConnection(SQLurl, username, pass);
        //first we get all documents where all these words appears
        String finalWords = "(";
        for (int i = 0; i < searchQuery.length; i++) {
            finalWords += searchQuery[i] + ", ";
        }
        finalWords += ")";


        //we are going to assume for two words at least to appear

        String queryGetDocumentsWhereAllWordsAppears = "SELECT  document_name" +
                "FROM " + documentWordTable +
                "where word in" + finalWords +
                "GROUP BY document_name" +
                "HAVING COUNT(*) > 1;";

        Statement stmtDocumentsWhereAllWordsAppears = conn.createStatement();
        ResultSet rsDocumentsWhereAllWordsAppears = stmtDocumentsWhereAllWordsAppears.executeQuery(queryGetDocumentsWhereAllWordsAppears);
        String[] mainDocuments = new String[100];
        int counter = 0;
        while (rsDocumentsWhereAllWordsAppears.next()) {
            mainDocuments[counter] = rsDocumentsWhereAllWordsAppears.getString("document_name");
            counter += 1;
        }


        int currentIndex = 0; //this will be used to loop through our documents


        //first step is to calculate total number of documents
        String getTotalNumberOfDocuments = "select count( distinct document_name) as numberOfDocuments from DOCUMENT_WORD ";
        Statement stmtTotalNumberOfDocuments = conn.createStatement();
        ResultSet rsTotalNumberOfDocuments = stmtTotalNumberOfDocuments.executeQuery(getTotalNumberOfDocuments);
        int countTotalNumberOfDocuments = 0;
        while (rsTotalNumberOfDocuments.next()) {
            countTotalNumberOfDocuments = rsTotalNumberOfDocuments.getInt("numnberOfDocuments");
        }

        //-----------------------------------------------------------------------------------------------------------
        while (mainDocuments[currentIndex] != null) {
            //here we will loop through our query words, get tf-idf of that word relative to that documment and save the
            //result in a priority queue based in the score
            String currentDocument = mainDocuments[currentIndex];

            //we will loop through our search query items
            float tf_idf = 0;
            for (int i = 0; i < searchQuery.length; i++) {

                //getting tf of word
                String queryTf = "SELECT  tf_idf as tf" +
                        "FROM" + documentWordTable +
                        "where word = '" + searchQuery[i] + "'" +
                        " and document_name = '" + currentDocument + "'";

                Statement stmtWordTF = conn.createStatement();
                ResultSet rsWordTF = stmtWordTF.executeQuery(queryTf);
                float current_tf = 0;
                while (rsWordTF.next()) {
                    current_tf = rsWordTF.getFloat("tf");
                }
                //------------------------------------------------------------------------
                //getting idf of word
                String getNumberDocumentsForWord = "select count(distinct document_name) as total from " + documentWordTable
                        + " where word = '" + searchQuery[i] + "'";
                Statement stmtWordIDF = conn.createStatement();
                ResultSet rsWordIDF = stmtWordIDF.executeQuery(getNumberDocumentsForWord);
                int countWordInWeb = 0;
                while (rsWordIDF.next()) {
                    countWordInWeb = rsWordIDF.getInt("total");
                }
                //--------------------------------------------------------------------------
                double idfOfWord = Math.log(countTotalNumberOfDocuments / countWordInWeb);
                tf_idf += (current_tf * idfOfWord);

            }
            Entry finalValues = new Entry(currentDocument, (double) tf_idf);
            rankerResult.add(finalValues);

            currentIndex += 1;
        }

        /*we are going to create number of threads equivalent to the number of words in our query where each query
          will get number of documents of this word and compute the TF of the word
         */


    }
}
