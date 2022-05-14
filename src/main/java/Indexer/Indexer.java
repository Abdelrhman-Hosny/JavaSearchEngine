package Indexer;

public class Indexer {
 
    static Preprocessor pre = new Preprocessor();
    static Parser par = new Parser();
    static String HTMLString = "<!DOCTYPE html>" + "<html>" + "<head>" + "<title>JSoup Example</title>" + "</head>" + 
        "<body><h1>HelloWorld00</h1>" + "<table><tr><td> <h1>i  we are HelloWorld</h1><h1>HelloWorld2</h1></tr>" + 
        "</table>" +"1 2 3 4 5 <p> i am <b>strong</b> </p>" +"</body>" + "</html>";

    
    public static void main(String[] args) {
        String s1 = pre.Process(HTMLString);
        par.Parse(s1);
        System.out.println(par.H1);
        System.out.println(par.H2);
        System.out.println(par.Title);
        System.out.println(par.normalText);
        System.out.println(par.Bold_Strong);

    }
}