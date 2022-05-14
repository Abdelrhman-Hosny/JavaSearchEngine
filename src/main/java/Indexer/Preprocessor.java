package Indexer;


public class Preprocessor{
    // this preprocessor class will work on a single document for now
    // TODO:: in threading may take multiple docs based on separation
    
    
    public String Process(String HTMLString) {
        
        // will change whole html into lower case to avoid having same word multiple times in many formats  
        HTMLString = HTMLString.toLowerCase();
        String processed = removeStopwords(HTMLString);
        System.out.println(processed);
        return processed;

        // TODO::
        // we will handle symbols after parsing since before parsing we cant remove them since
        // we will destroy the tags <>

        // also single letters will remove them later since <b> will be removed and i need it 
    }

    public static String removeStopwords(String toBeProcessedString){
        // some of stop words which are widely used in english so we wont save in our indexer 
        // will also remove single numbers
        String stopWords = "(i|me|my|myself|we|our|ours|ourselves|you|your|yours|yourself|yourselves"
        +"|he|him|his|himself|she|her|hers|herself|it|its|itself|they|them|their|theirs|themselves"
        +"|what|which|who|whom|this|that|these|those|am|is|are|was|were|be|been|being|have|has|had"
        +"|having|do|does|did|doing|a|an|the|and|but|if|or|because|as|until|while|of|at|by|for|with"
        +"|about|against|between|into|through|during|before|after|above|below|to|from|up|down|in|out"
        +"|on|off|over|under|again|further|then|once|here|there|when|where|why|how|all|any|both|each"
        +"|few|more|most|other|some|such|no|nor|not|only|own|ame|so|than|too|very|an|will|just"
        +"|should|now|[0-9])";

        // we match whole word ex: i am ahmed -> here match i , am
        // but dont match i inside a word example : i inside playing - > so we will put boundaries by \b
        return toBeProcessedString.replaceAll("\\b"+stopWords+"\\b", "");
    }

}
