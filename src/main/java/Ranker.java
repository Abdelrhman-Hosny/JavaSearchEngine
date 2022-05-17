import java.sql.*;
import java.lang.Math;
import java.util.*;

public class Ranker {
    //here we will define some general variables that we will change locally


    public static String SQLurl = "jdbc:sqlserver://DESKTOP-V25J5E2\\SQLEXPRESS:1433;databaseName=test;integratedSecurity=true;"                        + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "encrypt=false;";
    public static String username = "sa";
    public static String pass = "123456789";
    public static Connection conn;

    //word_document table info
    public String wordTableName = "WORD_DOCUMENT";
    public static String documentWordTable = "DOCUMENT_WORD";
    //document table info
    String documentTableName = "document_data";
    String documentTableIdColumn = "id";
    public static int numberOfThreads = 4;




    //this will be used in the priority queue
    public static class Entry {
        private String key;
        private double value;

        public double getValue() {
            return this.value;
        }
        public String getKey()
        {
            return this.key;
        }


        public Entry(String key, double value) {
            this.key = key;
            this.value = value;
        }
    }

    public static ResultSet createQuery(Connection conn, String query)  {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return (ResultSet) e;
        }

    }


    public static void main(String args[]) throws SQLException, InterruptedException {
        //assuming that the connection to the sql server is made
        //and we have a string of words after they are query processed
        //we are going to calculate the tf-idf of the word we are getting through the following queries
        String search = "football keka";
        String[] searchQuery = search.split(" ");
        //for each word we will compute term frequency
        Ranker.conn = DriverManager.getConnection(Ranker.SQLurl,Ranker.username,Ranker.pass);
        System.out.println("connected");
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



        //first we get all documents where all these words appears
        String finalWords = "(";
        for (int i = 0; i < searchQuery.length; i++) {
            finalWords += "'"+ searchQuery[i] +"'"+ ",";
        }
        int index = finalWords.lastIndexOf(',');
        finalWords = finalWords.substring(0,index);
        finalWords += ")";

        System.out.println(finalWords);
        //we are going to assume for two words at least to appear

        String queryGetDocumentsWhereAllWordsAppears = "SELECT  document_name" +
                " FROM " + Ranker.documentWordTable +
                " where word in " + finalWords +
                " GROUP BY document_name" +
                " HAVING COUNT(*) > 1;";
        ResultSet rsDocumentsWhereAllWordsAppears = createQuery(conn,queryGetDocumentsWhereAllWordsAppears);
//        String[] mainDocuments = new String[100];
        ArrayList<String> mainDocuments = new ArrayList<String>();


        while (rsDocumentsWhereAllWordsAppears.next()) {
            mainDocuments.add(rsDocumentsWhereAllWordsAppears.getString("document_name"));
        }


        int currentIndex = 0; //this will be used to loop through our documents


        //first step is to calculate total number of documents
        String getTotalNumberOfDocuments = "select count( distinct document_name) as numberOfDocuments from DOCUMENT_WORD ";
        ResultSet rsTotalNumberOfDocuments = createQuery(conn,getTotalNumberOfDocuments);
        int countTotalNumberOfDocuments = 0;
        while (rsTotalNumberOfDocuments.next()) {
            countTotalNumberOfDocuments = rsTotalNumberOfDocuments.getInt("numberOfDocuments");
        }
        System.out.println("Total number of documents is " + countTotalNumberOfDocuments);

        //-----------------------------------------------------------------------------------------------------------
        /*while (mainDocuments[currentIndex] != null) {
            //here we will loop through our query words, get tf-idf of that word relative to that documment and save the
            //result in a priority queue based in the score
            String currentDocument = mainDocuments[currentIndex];

            //we will loop through our search query items
            float tf_idf = 0;
            for (int i = 0; i < searchQuery.length; i++) {

                //getting tf of word
                String queryTf = "SELECT  tf_idf as tf" +
                        " FROM " + documentWordTable +
                        " where word = '" + searchQuery[i] + "'" +
                        " and document_name = '" + currentDocument + "'";
                System.out.println(queryTf);
                ResultSet rsWordTF = createQuery(conn,queryTf);
                float current_tf = 0;
                while (rsWordTF.next()) {
                    current_tf = rsWordTF.getFloat("tf");
                }
                System.out.println("Current word tf is "+current_tf);
                //------------------------------------------------------------------------
                //getting idf of word
                String getNumberDocumentsForWord = "select count(distinct document_name) as total from " + documentWordTable
                        + " where word = '" + searchQuery[i] + "'";
                ResultSet rsWordIDF = createQuery(conn,getNumberDocumentsForWord);
                int countWordInWeb = 0;
                while (rsWordIDF.next()) {
                    countWordInWeb = rsWordIDF.getInt("total");
                }
                System.out.println("Current word idf is "+countWordInWeb);
                //--------------------------------------------------------------------------
                double idfOfWord = Math.log((double)countTotalNumberOfDocuments / (double) countWordInWeb);
                tf_idf += (current_tf * idfOfWord);
                System.out.println("Current TF-idf is" +tf_idf);

            }
            Entry finalValues = new Entry(currentDocument, (double) tf_idf);
            rankerResult.add(finalValues);

            currentIndex += 1;
        }*/

        int shareOfThread = mainDocuments.size() / Ranker.numberOfThreads;
        //this means that the number of documents is less than number of threads there for we only need one thread
        Thread []listOfThreads = new Thread[4];
        if(shareOfThread == 0)
        {
            documentRank currentShareForThread = new documentRank(mainDocuments,searchQuery,rankerResult,countTotalNumberOfDocuments);

            listOfThreads[0] = new Thread(currentShareForThread);
            listOfThreads[0].start();
            listOfThreads[0].join();
        }

        else {

            ArrayList<ArrayList<String>> listOfShares = new ArrayList<ArrayList<String>>();
            for (int i = 0; i < numberOfThreads; i++) {
                ArrayList<String> shareOfDocumentsForThread = new ArrayList<String>();
                for (int j = 0; j < shareOfThread; j++) {
                    System.out.println(mainDocuments.get(j + i * shareOfThread));
                    shareOfDocumentsForThread.add(mainDocuments.get(j + i * shareOfThread));
                }
                System.out.println(shareOfDocumentsForThread);

                listOfShares.add(shareOfDocumentsForThread);
            }

            //now we loop through this list and create a thread for each share
//            Thread[] listOfThreads = new Thread[4];
            for (int i = 0; i < numberOfThreads; i++) {
                documentRank currentShareForThread = new documentRank(listOfShares.get(i), searchQuery, rankerResult, countTotalNumberOfDocuments);
                System.out.println(listOfShares.get(i).size());
                listOfThreads[i] = new Thread(currentShareForThread);
                listOfThreads[i].start();
            }
            for(int i=0;i<numberOfThreads;i++)
            {
                System.out.println("Joining threads");
                try {
                    listOfThreads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        /*we are going to create number of threads equivalent to the number of words in our query where each query
          will get number of documents of this word and compute the TF of the word
         */
        Iterator v = rankerResult.iterator();
        System.out.println("The iterator values are: ");
        Entry test = rankerResult.peek();
        System.out.println(test.getValue());

    }
}
