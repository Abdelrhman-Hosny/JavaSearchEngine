package Indexer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import Database.IndexerDAO;
import static Constants.Constants.*;


public class Indexer implements Runnable{
    
    private IndexerDAO indexerManager;
    private Parser parserObj = new Parser();
    private int documentSize = 0;
    
    // TODO :: document URL
    private String documentURL;
    private HashMap<String, Word> wordHashMap = new HashMap<String, Word>();
    private File HTMLfile;
    
    Indexer(IndexerDAO inManager, File inFile){
        //TODO : need to change some eof variables here to constants
        indexerManager = inManager;
        HTMLfile = inFile;
    }


    // this function take blockOftext either h1 text, or h2 ,....
    // and pass Category with it to know where does these words exists
    // these are all possible categories : TITLE,H1,H2,H3,H4_H6,TEXT,BOLD
    public boolean Index(String blockOfText, BlockCategories Category) {
        if(blockOfText == "" || blockOfText == " "){
            return true;
        }
        // getting each word by splitting by space
        String[] WordsArray = blockOfText.split(" ");
        for (String _word: WordsArray){
            if(_word == "" || _word == " "){
                // as string may contain wrong spaces even after normalising 
                // we dont need to index these and also single letters 
                continue ;
            }
            Word currentWord = null ; 
            if(wordHashMap.containsKey(_word)){
                // check if word existed before
                currentWord = wordHashMap.get(_word);
            }else{
                // it doesnt exist before so will make a new word 
                currentWord = new Word(_word,documentURL);
            }
            currentWord.Increment(Category);
            if(documentSize >= documentSizeThreshold &&
            (float)currentWord.count / documentSize > spamPercentage){
                // we will consider spam if document > certain size threshold
                // as if document is 20 words and word present like 5 times thats not spam
                // so we will set a threshold of 100 word/document for ex for spamming
                System.out.println("word spamming" + _word + " " + currentWord.count);
                return false;
            }
            wordHashMap.put(_word, currentWord);
        }
        return true;
    }
    public boolean IndexAll(){
        // if anyone returned false it will stop since they are anded
        return Index(parserObj.H1, BlockCategories.H1) && Index(parserObj.H2, BlockCategories.H2)
        && Index(parserObj.H3, BlockCategories.H3) && Index(parserObj.remainingHeaders, BlockCategories.H4_H5_H6)
        && Index(parserObj.Title, BlockCategories.TITLE) && Index(parserObj.normalText, BlockCategories.NORMAL_TEXT)
        && Index(parserObj.Bolded_inside_normalText, BlockCategories.BOLD);

    }

    public boolean spamRollback(){
        // since its spam -> will delete(rollback) the url in my database if added before
        synchronized(indexerManager){
            return indexerManager.delete_rollBack_Url(documentURL); 
        }
    }
    
    @Override
    public void run() {
        
        
        //TODO:: maybe will need to extract url eariler here
        // File input = new File(DOWNLOAD_PATH+HTMLfile) ;
        Document html ;
        try {
            html = Jsoup.parse(HTMLfile, "UTF-8", "");
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }
        
        parserObj.Parse(html); // parsing html document into main categories h1, h2, ...
        parserObj.removeStopwordsForAllCategories(); // remove stop words and lower case for all categories

        // System.out.println("parserObj.H1 " + parserObj.H1);
        // System.out.println("parserObj.H2 " + parserObj.H2);
        // System.out.println("parserObj.H3 " + parserObj.H3);
        // System.out.println("parserObj.remainingHeaders " + parserObj.remainingHeaders);
        // System.out.println("parserObj.Title " + parserObj.Title);
        // System.out.println("parserObj.normalText " + parserObj.normalText);
        // System.out.println("parserObj.Bolded_inside_normalText " + parserObj.Bolded_inside_normalText);
        // System.out.println("parserObj.Url " + parserObj.Url);
        
        documentURL = parserObj.Url;
        // getting total number of words in document
        // to check spam if certain word is present > certain percentage in document -> report spam
        // we will consider spam if document > certain size 
            // as if document is 20 words and word present like 5 times thats not spam
            // so we will set a threshold of 100 word/document for ex for spamming
        documentSize = parserObj.H1.split(" ").length + parserObj.H2.split(" ").length + 
                parserObj.H3.split(" ").length + parserObj.remainingHeaders.split(" ").length + 
                parserObj.Title.split(" ").length + parserObj.normalText.split(" ").length;


        // index all partitions h1,h2, .....
        if(IndexAll() == false){
            // therefore its spam 
            // will return after cleaning database since we wont need to continue and add data
            spamRollback();
            System.out.println("spaaam !!!");
            return ;
        }        

        
        synchronized(indexerManager){
            System.out.println("wordHashMap.size()" + wordHashMap.size());
            indexerManager.InsertWordIndex(wordHashMap, HTMLfile.getName(), documentSize);
        }
           
    }
}