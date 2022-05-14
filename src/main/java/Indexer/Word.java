package Indexer;

public class Word {
    public String word ; // word itself
    public String document ; // document where word exists
    public int title ; // no of occurences in title
    public int h1 ; // no of occurences in h1
    public int h2 ; // no of occurences in h2
    public int h3 ; // no of occurences in h3
    public int h4_6 ; // no of occurences in h4,h5,h6
    public int text ; // no of occurences in other texts as <p>, <table> , ...
    public int bold ; // no of occurences in bold  
    public int count ; // total count of word in document

    public Word(String inWord){
        // TODO : document init
        word = inWord;
        title = 0;
        h1 = 0;
        h2 = 0;
        h3 = 0;
        h4_6 = 0;
        text = 0;
        bold = 0;
    }
    public void Increment(BlockCategories Category){
        // take Category enum
        if(Category == BlockCategories.H1){
            h1++;
        }else if(Category == BlockCategories.H2){
            h2++;
        }else if(Category == BlockCategories.H3){
            h3++;
        }else if(Category == BlockCategories.H4H5H6){
            h4_6++;
        }else if(Category == BlockCategories.TITLE){
            title++;
        }else if(Category == BlockCategories.TEXT){
            text++;
        }else if(Category == BlockCategories.BOLD){
            bold++;
        }
        count++;
    }
    public static void main(String[] args) {
        BlockCategories b = BlockCategories.H1;
        Word w= new Word("das") ;
        w.Increment(b);
    }
}
