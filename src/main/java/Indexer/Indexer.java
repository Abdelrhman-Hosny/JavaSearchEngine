package Indexer;
import Preprocessing.Preprocessor;
import java.util.HashMap;

public class Indexer{
    
    
    private static Preprocessor preprocessorObj = new Preprocessor();
    private static Parser parserObj = new Parser();
    private static double spamPercentage = 0.4; // if certain word exceed that percentage report spam
    
    //TODO :: REMOVE OR NOT ??
    // we will consider spam if document > certain size 
            // as if document is 20 words and word present like 5 times thats not spam
            // so we will set a threshold of 100 word/document for ex for spamming
    private static int documentSizeThreshold = 100;
    private static int documentSize = 0;

    
    private static HashMap<String, Word> wordHashMap = new HashMap<String, Word>();
    
    // this function take blockOftext either h1 text, or h2 ,....
    // and pass Category with it to know where does these words exists
    // these are all possible categories : TITLE,H1,H2,H3,H4_H6,TEXT,BOLD
    public static boolean Index(String blockOfText, BlockCategories Category) {
        if(blockOfText.length() == 0){
            return ;
        }
        // getting each word by splitting by space
        String[] WordsArray = blockOfText.split(" ");
        for (String _word: WordsArray){
            Word currentWord = null ; 
            if(wordHashMap.containsKey(_word)){
                // check if word existed before
                currentWord = wordHashMap.get(_word);
            }else{
                // it doesnt exist before so will make a new word 
                currentWord = new Word(_word);
            }
            currentWord.Increment(Category);
            if(documentSize >= documentSizeThreshold &&
            (float)currentWord.count / WordsArray.length > spamPercentage){
                // report spam
                return false;
            }
            wordHashMap.put(_word, currentWord);
        }
        return true;
    }
    public static boolean IndexAll(){
        // if anyone returned false it will stop since they are anded
        return Index(parserObj.H1, BlockCategories.H1) && Index(parserObj.H2, BlockCategories.H2)
        && Index(parserObj.H3, BlockCategories.H3) && Index(parserObj.remainingHeaders, BlockCategories.H4_H5_H6)
        && Index(parserObj.Title, BlockCategories.TITLE) && Index(parserObj.normalText, BlockCategories.NORMAL_TEXT)
        && Index(parserObj.Bolded_inside_normalText, BlockCategories.BOLD);

    }

    public static void main(String[] args) {
        String HTMLString = "<!DOCTYPE html>" + "<html>" + "<head>" + "<title>JSoup Example</title>" + "</head>" + 
        "<body><h1>HelloWorld00</h1>" + "<table><tr><td> <h1>i  we are HelloWorld</h1><h1>HelloWorld2</h1></tr>" + 
        "</table>" +"1 2 3 4 5 <p> i am computer <b>computer</b> hi man </p>" +"</body>" + 
        "<p>sadkj <b>sada</b> bndsjk <p>"+
        "<table><tr><th>eboo</th></tr><tr><td>Emil</td><td>Tobias</td><td>Linus</td></tr></table>" + "</html>";
        
        String s1 = preprocessorObj.removeStopwords(HTMLString);
        System.out.println(s1);
        parserObj.Parse(s1);
        System.out.println("parserObj.H1 " + parserObj.H1);
        System.out.println("parserObj.H2 " + parserObj.H2);
        System.out.println("parserObj.H3 " + parserObj.H3);
        System.out.println("parserObj.remainingHeaders " + parserObj.remainingHeaders);
        System.out.println("parserObj.Title " + parserObj.Title);
        System.out.println("parserObj.normalText " + parserObj.normalText);
        System.out.println("parserObj.Bolded_inside_normalText " + parserObj.Bolded_inside_normalText);

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
            // spam
        }        

        for (String key: wordHashMap.keySet()) {
            Word value = wordHashMap.get(key);
            System.out.println(key + " :: value.count " + value.count
            +" value.h1 "  + value.h1+ " value.h2 "  + value.h2+ " value.h3 "  + value.h3+
            " value.h4 "  + value.h4_6+ " value.title "  + value.title+" value.normal "  + value.text
            +" value.bold "  + value.bold
            );
        }

    }
}