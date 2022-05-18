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

    public ArrayList<Entry> getPhraseLevel(ArrayList<Entry> urlArrayEntry, String sentence) throws IOException {
        
        ArrayList<String> urlArray = new ArrayList<>();
        for(int i=0;i<urlArrayEntry.size();i++)
        {
            urlArray.add(urlArrayEntry.get(i).getKey());
        }

        Utils utils = new Utils();
        HashMap<String, String> urlPathMap = utils.getUrlPathMap();

        ArrayList<Integer> takenIndices = new ArrayList<>();
        for (int i = 0; i < urlArray.size(); i++) {
           if (!urlPathMap.containsKey(urlArray.get(i)))
                continue;
           
           File file = new File(urlArray.get(i));
           Document doc = Jsoup.parse(file, "UTF-8");

            if (doc.body().text().contains(sentence) || doc.title().contains(sentence)) {
                takenIndices.add(i);
            }
        }
        ArrayList<Entry> newResults = new ArrayList<>();
        for(int i=0;i<takenIndices.size();i++)
        {
            newResults.add(new Entry(urlArrayEntry.get(i).getKey(), urlArrayEntry.get(i).getValue()));
        }

        return newResults;
    }
    
}
