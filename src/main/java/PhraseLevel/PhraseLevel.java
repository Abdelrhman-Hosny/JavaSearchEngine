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

    public ArrayList<Integer> getPhraseLevel(ArrayList<Entry> urlArrayEntry, String sentence) throws IOException {
        
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

        return takenIndices;
    }

    public void getSentence() throws IOException{
        // get all urls for documents

        ArrayList<String> documents = new ArrayList<>();
        String sentence = "";
        
        // run get phrase level
        ArrayList<Integer> takenIndices = getPhraseLevel(documents, sentence);

        ArrayList<String> takenDocuments = new ArrayList<>();
        for (int i = 0; i < takenIndices.size(); i++) {
            takenDocuments.add(documents.get(takenIndices.get(i)));
        }


    }
    
}
