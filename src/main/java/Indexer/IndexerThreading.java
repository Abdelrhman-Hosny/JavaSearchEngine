package Indexer;
import java.io.File;
import java.io.IOException;

import Database.IndexerDAO;
import Ranker.Ranker;
import Utils.Utils;

import static Constants.Constants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexerThreading {

    // Indexer DB Manager
    private static IndexerDAO indexerManager = new IndexerDAO();
    
    public static void main(String[] args) throws InterruptedException, IOException {
        // retrireving html files from folder crawler made
        File folder = new File(DOWNLOAD_PATH);
        File[] listOfFiles = folder.listFiles();

        // dividing work among threads and when one finish it will be self assigned a new indexer task
        ExecutorService executor = Executors.newFixedThreadPool(numThreadsIndexer);
        for (int i = 0; i < listOfFiles.length; i++) {
            Runnable worker = new Indexer(indexerManager,listOfFiles[i]);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads for indexer");
        
        Ranker rankerObj = new Ranker();
        // calculating page rank and saving in db
        Utils ut = new Utils();
        HashMap<String, HashSet<String>> x = ut.cleanPageDegreeFile(CRAWLER_PROGRESS_PATH + PAGE_DEGREE_SAVE_FILE);
        HashMap<String,Double> pageRank = rankerObj.calculatePageRank(x,ut.getInPageMap(x));
        rankerObj.uploadPageRank(pageRank);
    }
    
}
