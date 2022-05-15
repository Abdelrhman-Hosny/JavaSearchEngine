import java.sql.*;

import java.lang.Math;

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


    public static void numberOfDocuments(ResultSet[] rs) throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:default:connection");
        Statement stmt = null;
        String query =
                "select count(distinct document_name) from"+ wordTableName;
        stmt = con.createStatement();
        rs[0] = stmt.executeQuery(query);
    }

    public void createProcedures(Connection con) throws SQLException
    {
        Statement stmtShowNumberOfDocuments = null;
        String queryTotalNumberOfDocuments=
                "CREATE PROCEDURE NUMBER_OF_DOCUMENTS()" +
                        "PARAMETER STYLE JAVA " +
                        "LANGUAGE JAVA " +
                        "DYNAMIC RESULT SETS 1 " +
                        "";

        System.out.println("calling show documents procedure");
        stmtShowNumberOfDocuments = con.createStatement();
        stmtShowNumberOfDocuments.execute(queryTotalNumberOfDocuments);

    }


    public static void main(String args[]) throws SQLException {
        //assuming that the connection to the sql server is made
        //and we have a string of words after they are query processed
        //we are going to calculate the tf-idf of the word we are getting through the following queries
        String search = "football mexico";
        String [] searchQuery = search.split(" ");
        //for each word we will compute term frequency

        //add all these words in one statement



        Connection conn = DriverManager.getConnection(SQLurl,username,pass);
        //first we get all documents where all these words appears
        String finalWords = "(";
        for (int i=0;i<searchQuery.length;i++)
        {
            finalWords += searchQuery[i] +", ";
        }
        finalWords += ")";


        //we are going to assume for two words at least to appear

        String queryGetDocumentsWhereAllWordsAppears = "SELECT  document_name" +
                "FROM "+documentWordTable +
                "where word in" + finalWords +
                "GROUP BY document_name" +
                "HAVING COUNT(*) > 1;";

        Statement stmtDocumentsWhereAllWordsAppears = conn.createStatement();
        ResultSet rsDocumentsWhereAllWordsAppears = stmtDocumentsWhereAllWordsAppears.executeQuery(queryGetDocumentsWhereAllWordsAppears);
        String [] mainDocuments = new String[100];
        int counter = 0;
        while(rsDocumentsWhereAllWordsAppears.next())
        {
            mainDocuments[counter] = rsDocumentsWhereAllWordsAppears.getString("document_name");
            counter+=1;
        }


        int currentIndex = 0; //this will be used to loop through our documents
        while(mainDocuments[currentIndex] != null)
        {
            //here we will loop through our query words, get tf-idf of that word relative to that documment and save the
            //result in a priority queue based in the score



            currentIndex +=1;
        }

        /*we are going to create number of threads equivalent to the number of words in our query where each query
          will get number of documents of this word and compute the TF of the word
         */

        float [] docDataTF_IDF = new float[100];
        String [] doc_name = new String[100];
        for (int i=0;i<searchQuery.length;i++)
        {
            String getNumberDocumentsForWord = "select count(distinct document_name) as total from " +documentWordTable
            + " where word = '"+ searchQuery[i] + "'" ;

            String getTotalNumberOfDocuments = "select count( distinct document_name) as numberOfDocuments from DOCUMENT_WORD ";

            String TFOfWord = "select document_name,tf_idf from DOCUMENT_WORD" +
            "where word = '"+ searchQuery[i] + "'" +
                    "order by tf_idf DESC";

            //getting  documents where this word appeared and their corresponding TF-idf
            Statement stmtWordTF = conn.createStatement();
            ResultSet rsWordTF  =stmtWordTF.executeQuery(TFOfWord);

            int index = 0;
            while(rsWordTF.next())
            {
                doc_name[index] = rsWordTF.getString("document_name");
                docDataTF_IDF[index] = rsWordTF.getFloat("tf_idf");
                index += 1;

            }
            //now we saved td_idf descendingly and their corresponding tf_idf

            //we have to compute the idf of this word
            Statement stmtWordIDF = conn.createStatement();
            ResultSet rsWordIDF = stmtWordIDF.executeQuery(getNumberDocumentsForWord);
            int countWordInWeb =0;
            while(rsWordIDF.next()) {
                countWordInWeb = rsWordIDF.getInt("total");
            }
            //this means that we don't have documents with that word
            if (countWordInWeb ==0) {
                System.out.println("Couldn't find any documents containing word" + searchQuery[i]);

            }

            //Total number of documents
            Statement stmtTotalNumberOfDocuments = conn.createStatement();
            ResultSet rsTotalNumberOfDocuments = stmtWordIDF.executeQuery(getTotalNumberOfDocuments);
            int countTotalNumberOfDocuments = 0;
            while(rsTotalNumberOfDocuments.next())
            {
                countTotalNumberOfDocuments = rsTotalNumberOfDocuments.getInt("numnberOfDocuments");
            }
            if (countTotalNumberOfDocuments == 0)
                System.out.println("Number of documents in database  = 0");

            double idf = Math.log(countTotalNumberOfDocuments/countWordInWeb);









        }



    }

}
