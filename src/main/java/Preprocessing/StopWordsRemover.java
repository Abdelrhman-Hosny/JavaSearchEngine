package Preprocessing;

import org.jsoup.internal.StringUtil;

public class StopWordsRemover {
    
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
        //normaliseWhitespace : make only 1 single Whitespace since we removed many items in middle 
        return StringUtil.normaliseWhitespace(toBeProcessedString.replaceAll("\\b"+stopWords+"\\b", ""));
    }

}
