import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class documentRank implements Runnable {
//    protected String currentDocument;
    protected String[] searchQuery;
    public PriorityQueue<Ranker.Entry> rankerResult;
    protected int countTotalNumberOfDocuments;
    protected ArrayList<String> shareOfDocs;


    public documentRank(ArrayList<String> shareOfDocs, String[] sQ, PriorityQueue<Ranker.Entry> q, int countTotalNumberOfDocuments)
    {
        this.shareOfDocs = shareOfDocs;
        this.searchQuery=sQ;
        this.rankerResult=q;
        this.countTotalNumberOfDocuments = countTotalNumberOfDocuments;
    }
    public void run() {
        float tf_idf = 0;

        for (int k = 0; k < shareOfDocs.size(); k++) {
            String currentDocument = shareOfDocs.get(k);
            for (int i = 0; i < searchQuery.length; i++) {
                //getting tf of word
                String queryTf = "SELECT  tf_idf as tf" +
                        " FROM " + Ranker.documentWordTable +
                        " where word = '" + searchQuery[i] + "'" +
                        " and document_name = '" + currentDocument + "'";
                System.out.println(queryTf);
                ResultSet rsWordTF = Ranker.createQuery(Ranker.conn, queryTf);
                float current_tf = 0;
                while (true) {
                    try {
                        if (!rsWordTF.next()) break;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try {
                        current_tf = rsWordTF.getFloat("tf");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Current word tf is " + current_tf);
                //------------------------------------------------------------------------
                //getting idf of word
                String getNumberDocumentsForWord = "select count(distinct document_name) as total from " + Ranker.documentWordTable
                        + " where word = '" + searchQuery[i] + "'";
                ResultSet rsWordIDF = Ranker.createQuery(Ranker.conn, getNumberDocumentsForWord);
                int countWordInWeb = 0;
                while (true) {
                    try {
                        if (!rsWordIDF.next()) break;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try {
                        countWordInWeb = rsWordIDF.getInt("total");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("Current word idf is " + countWordInWeb);
                //--------------------------------------------------------------------------
                double idfOfWord = Math.log((double) countTotalNumberOfDocuments / (double) countWordInWeb);
                tf_idf += (current_tf * idfOfWord);
                System.out.println("Current TF-idf is" + tf_idf);
            }
            synchronized (rankerResult) {
                rankerResult.add(new Ranker.Entry(currentDocument, tf_idf)); //adding to priority queue

            }

        }
    }
}
