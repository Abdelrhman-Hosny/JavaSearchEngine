package Indexer;
import java.io.File;
import Database.IndexerDAO;
import static Constants.Constants.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexerThreading {

    // Indexer DB Manager
    private static IndexerDAO indexerManager = new IndexerDAO();
    
    public static void main(String[] args) throws InterruptedException {
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
        System.out.println("Finished all threads");

    }
    
}
