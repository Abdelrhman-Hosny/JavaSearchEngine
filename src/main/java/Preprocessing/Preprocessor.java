package Preprocessing;


public class Preprocessor{
    // this preprocessor class will work on a single document for now
    // TODO:: in threading may take multiple docs based on separation
    
    Stemmer StemmerObj = new Stemmer();
    StopWordsRemover StopWordsRemoverObj = new StopWordsRemover();

    public String removeStopwords(String HTMLString) {
        if(HTMLString=="" || HTMLString == " ")
            return "";
        
        // will change whole html into lower case to avoid having same word multiple times in many formats  
        HTMLString = HTMLString.toLowerCase();
        return StopWordsRemover.removeStopwords(HTMLString);
        

        // TODO::
        // we will handle symbols after parsing since before parsing we cant remove them since
        // we will destroy the tags <>

        // also single letters will remove them later since <b> will be removed and i need it 
    }
    public String stem(String inpuString) {
        
        return StemmerObj.stemString(inpuString);
    }
}
