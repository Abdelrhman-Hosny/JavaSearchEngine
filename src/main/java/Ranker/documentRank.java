package Ranker;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.PriorityQueue;


public class documentRank implements Runnable {

    protected String[] searchQuery;
    public PriorityQueue<Ranker.Entry> rankerResult;
    protected int countTotalNumberOfDocuments;
    protected ArrayList<String> shareOfDocs;
    protected int titleWeight = 7;
    protected int h1Weight = 6;
    protected int h2Weight = 5;
    protected int h3Weight = 4;
    protected int h4Weight = 3;
    protected int boldWeight = 2;
    protected int textWeight = 1;


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
                String queryTf = "SELECT title,h1,h2,h3,h4,bold,text,tf_idf as tf" +
                        " FROM " + Ranker.documentWordTable +
                        " where word = '" + searchQuery[i] + "'" +
                        " and document_name = '" + currentDocument + "'";
                //System.out.println(queryTf);
                ResultSet rsWordTF = Ranker.createQuery(Ranker.conn, queryTf);
                int h1 = 0;
                int h2 = 0;
                int h3 = 0;
                int h4 = 0;
                int title = 0;
                int bold = 0;
                int text = 0;
                float current_tf = 0;
                while (true) {
                    try {
                        if (!rsWordTF.next()) break;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try {
                        current_tf = rsWordTF.getFloat("tf");
                        title = rsWordTF.getInt("title");
                        h1=rsWordTF.getInt("h1");
                        h2=rsWordTF.getInt("h2");
                        h3=rsWordTF.getInt("h3");
                        h4=rsWordTF.getInt("h4");
                        bold=rsWordTF.getInt("bold");
                        text=rsWordTF.getInt("text");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                int finalWeights = titleWeight*title + h1Weight*h1 + h2Weight*h2 + h3Weight*h3 + h4Weight*h4 + boldWeight*bold + textWeight*text;
                System.out.println("weights are " + finalWeights);
                System.out.println("Current word tf is " + current_tf);
                System.out.println("WordTF after weights is " + current_tf*finalWeights);
                current_tf = current_tf*finalWeights;
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
                //System.out.println("Current word idf is " + countWordInWeb);
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
