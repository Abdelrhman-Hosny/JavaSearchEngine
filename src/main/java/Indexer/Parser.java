package Indexer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Parser {
    public String H1; 
    public String H2;
    public String remainingHeaders ;  // contains h3-h6 
    public String normalText; // paragraphs
    public String Title;
    public String Bold_Strong; // contain bold-strong words
    
    
    public void Parse(String preprocessedHtml){
        Document html = Jsoup.parse(preprocessedHtml);
        
        H1 = html.select("h1").text();
        H2 = html.select("h2").text();
        remainingHeaders = html.select("h3,h4,h5,h6").text();
        Bold_Strong = html.select("b,strong").text();
        Title = html.select("title").text();
        normalText = html.select("p,").text();
    }


}
