package Indexer;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;

public class Parser {
    public String Url; // url of page
    public String H1; // contain text in h1 
    public String H2; // contain text in h2
    public String H3; // contain text in h3
    public String remainingHeaders ;  // contains h4-h6 
    public String Title; // contain text in title
    public String normalText; // paragraphs , text in tables , in items , spans , .. 
    public String Bolded_inside_normalText; // contain bold-strong words
    
    public String processTextBlock(String inpuString){
        if(inpuString.length() == 0 || inpuString == " "){
            return "";
        }
        // inpuString : represent set of words after choosing it from say h1, or p or whatever
        // we need it to have words only 
        // so we choose all symbols with [^a-zA-Z0-9] and replace it with space
        // we cant just replace it with "" since it maybe between 2 words : football,basketball
        // so if we removed it directly would damage our statistics
        // also trim : to remove leading and ending spaces
        return StringUtil.normaliseWhitespace(inpuString.replaceAll("[^a-zA-Z0-9]", " ")).trim();
    }
    
    public void Parse(String preprocessedHtml){
        if(preprocessedHtml.length() == 0 )
            return;
        
        Document html = Jsoup.parse(preprocessedHtml);
        Url = html.location();
        H1 = processTextBlock(html.select("h1").text()); // will select h1 and remove all symbols from it
        H2 = processTextBlock(html.select("h2").text());
        H3 = processTextBlock(html.select("h3").text());
        remainingHeaders = processTextBlock(html.select("h4,h5,h6").text());
        Title = processTextBlock(html.select("title").text());
        normalText = processTextBlock(html.select("p,li,td,th").text()); // normal text bolded
        // i assumed every text must be put in <p> tag since if we included div element
        // the div element can have <p> inside which will repeat elements and damaga statistics
        
        // getting <b>,<strong> tags found inside paragraphs
        Bolded_inside_normalText = processTextBlock(html.select("p,li,td,th").select("b,strong").text());
    }

}
