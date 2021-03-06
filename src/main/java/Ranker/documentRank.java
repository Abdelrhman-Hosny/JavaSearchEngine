package Ranker;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.PriorityQueue;

import Database.RankerDAO;


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
    protected RankerDAO rankerDB = new RankerDAO();

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
                ResultSet rsWordTF = null;
                synchronized(rankerDB){
                    rsWordTF = rankerDB.SearchWordIndex(searchQuery[i], currentDocument);
                }
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
                current_tf = current_tf*finalWeights;
                //------------------------------------------------------------------------
                //getting idf of word
                int countWordInWeb = 0 ;
                synchronized(rankerDB){
                    countWordInWeb = rankerDB.getNumberOfDocumentsForWord(searchQuery[i]);
                }
                //System.out.println("Current word idf is " + countWordInWeb);
                //--------------------------------------------------------------------------
                double idfOfWord = Math.log((double) countTotalNumberOfDocuments / (double) countWordInWeb);
                tf_idf += (current_tf * idfOfWord);
            }
            double final_rank = 0 ;
            synchronized(rankerDB){
                final_rank = rankerDB.getDocumentRank(currentDocument) * tf_idf;
            }
            synchronized (rankerResult) {
                rankerResult.add(new Ranker.Entry(currentDocument, final_rank)); //adding to priority queue
            }

        }
    }
}
