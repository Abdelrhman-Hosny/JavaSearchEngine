package PhraseLevel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import Ranker.Ranker.Entry;
import Utils.Utils;

public class PhraseLevel {

    public Object[] getPhraseLevel(Object[] urlArrayEntry, String sentence) throws IOException {
        
        ArrayList<String> urlArray = new ArrayList<>();
        for(int i=0;i<urlArrayEntry.length;i++)
        {
            urlArray.add(((Entry)urlArrayEntry[i]).getKey());
        }

        Utils utils = new Utils();
        HashMap<String, String> urlPathMap = utils.getUrlPathMap();

        ArrayList<Integer> takenIndices = new ArrayList<>();
        ArrayList<Object> newResults = new ArrayList<>();
        

        for (int i = 0; i < urlArray.size(); i++) {
           if (!urlPathMap.containsKey(urlArray.get(i)))
                continue;
           
           File file = new File(urlPathMap.get(urlArray.get(i)));
           
           Document doc = Jsoup.parse(file, "UTF-8");
            if (doc.body().text().toLowerCase().contains(sentence) || doc.title().toLowerCase().contains(sentence)) {
                // takenIndices.add(i);
                newResults.add(new Entry(((Entry)urlArrayEntry[i]).getKey(), ((Entry)urlArrayEntry[i]).getValue()));
            }
        }
        
        return newResults.toArray();
    }
    
}
