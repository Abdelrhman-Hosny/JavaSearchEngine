package Indexer;
import Preprocessing.Preprocessor;
import java.util.HashMap;

public class Indexer{
 
    static Preprocessor preprocessorObj = new Preprocessor();
    static Parser parserObj = new Parser();
    
    static HashMap<String, Word> wordHashMap = new HashMap<String, Word>();
    
    // this function take blockOftext either h1 text, or h2 ,....
    // and pass Category with it to know where does these words exists
    // these are all possible categories : TITLE,H1,H2,H3,H4_H6,TEXT,BOLD
    public static void Index(String blockOfText, BlockCategories Category) {
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
            wordHashMap.put(_word, currentWord);
        }
    }
    public static void IndexAll(){
        Index(parserObj.H1, BlockCategories.H1);
        Index(parserObj.H2, BlockCategories.H2);
        Index(parserObj.H3, BlockCategories.H3);
        Index(parserObj.remainingHeaders, BlockCategories.H4_H5_H6);
        Index(parserObj.Title, BlockCategories.TITLE);
        Index(parserObj.normalText, BlockCategories.NORMAL_TEXT);
        Index(parserObj.Bolded_inside_normalText, BlockCategories.BOLD);
        return;
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
        
        // index all partitions h1,h2, .....
        IndexAll();        

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