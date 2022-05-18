package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static Constants.Constants.*;

public class Utils {


    public HashMap<String, HashSet<String>> cleanPageDegreeFile(String filePath) throws IOException {

        HashMap<String, HashSet<String>> pageDegreeMap = new HashMap<>();

        List<String> lines = Files.readAllLines(Paths.get(CRAWLER_PROGRESS_PATH + VISITED_SAVE_FILE));

        lines.replaceAll(line -> line.split("\t")[0]);

        HashSet<String> visited = new HashSet<>(lines);
        //HashSet<String> visited = new HashSet<String>(List.of(new String[]{"1", "2", "3", "4", "5"}));

        BufferedReader br = Files.newBufferedReader(Paths.get(filePath));

        String line;

        while ((line = br.readLine()) != null) {

            String[] split = line.split("\t");
//            System.out.println(split[0]);

            if (split.length <= 1 && !visited.contains(split[0])) continue;

            String page = split[0];

            HashSet<String> outLinks = new HashSet<>();
            for (int i = 1; i < split.length; i++) {
               if (!visited.contains(split[i])) continue;
//                if(page.equals(split[i]))
//                    continue;
                    outLinks.add(split[i]);

            }
            if(outLinks.size() == 0) {
                System.out.println("adding sink url"+page);

                for (String x : visited) {
//                    if(x.equals(page))
//                        continue;
                    outLinks.add(x);

                }
            }
                pageDegreeMap.put(page, outLinks);


        }

        return pageDegreeMap;
    }

    public HashMap<String, HashSet<String>> getInPageMap(HashMap<String, HashSet<String>> pageDegreeMap) {
        HashMap<String, HashSet<String>> inPageMap = new HashMap<>();
        for (String currentPage : pageDegreeMap.keySet()) {

            HashSet<String> inLinks = new HashSet<>();

            for (String loopingPage : pageDegreeMap.keySet()) {
//                if (loopingPage.equals(currentPage)) continue;
                    if (pageDegreeMap.get(loopingPage).contains(currentPage)) {
                        inLinks.add(loopingPage);
                    }
            }

            inPageMap.put(currentPage, inLinks);
        }

        return inPageMap;
    }
    public static HashMap<String,Double> calculatePageRank(HashMap<String, HashSet<String>> outgoing,HashMap<String, HashSet<String>> ingoing)
    {
        //the algorithm takes two hashmaps, one that takes each url for ex(A) and states all outgoing urls from that url(A)

        //the other hashMap takes url(A) and states all incoming urls pointing towards url(A)

        //Damping factor which is 85% that states if the algorithm output didn't change by 15% this means we will stop

        //we will save the rank of each url in each step in a HashMap where this value will be updated each iteration
        double dampingFactor = 1;
        HashMap<String,Double> pageRankPrev = new HashMap<String,Double>();
        HashMap<String,Double> pageRankCurrent = new HashMap<String,Double>();
        double initialPageRank = 1/(double)outgoing.size();
        //System.out.printf("%f",initialPageRank);
        System.out.println(initialPageRank);


        //save all urls in an array to be used 
        String[] myUrls = outgoing.keySet().toArray(new String[0]);
        int totalNodes = myUrls.length;
        //1st step initialize all urls  with the initial value
        for(int i = 0;i<totalNodes;i++)
        {
            pageRankPrev.put(myUrls[i],initialPageRank);
        }

        //debugging purpose checking that all values were added correctlye
        //TODO: Remove the following lines




        //we will iterate 100 times or when the margin of error is 0.01
        int iterations = 1;
        
        while(iterations<=100)
        {
            //if page has no outgoing links it is a sink which will cause error in our algorithm

            //looping through our nodes
            for(int i=0;i<totalNodes;i++)
            {
                //first step get all nodes that are pointing towards current node
                String currentNode = myUrls[i];
                String[] allIncomingNodesTowardsCurrentNode = ingoing.get(currentNode).toArray(new String[0]);
                double prOfCurrentNode = (1-dampingFactor)/(double)totalNodes;
                //now we loop through all of these nodes and get the probability of each one and add it to the current pr
                double intermediateVal = 0;
                for(int j=0;j<allIncomingNodesTowardsCurrentNode.length;j++)
                {
                    //get previous rank of node
                    double prevPROfNode = pageRankPrev.get(allIncomingNodesTowardsCurrentNode[j]);
                    //divide that value by number of outgoings links of that node
                    int countOfOutgoingLinks = outgoing.get(allIncomingNodesTowardsCurrentNode[j]).size();
                    prevPROfNode = prevPROfNode/(double)countOfOutgoingLinks;
                    //multiply by damping factor
                    //prevPROfNode *= dampingFactor;
                    //add it to the current value
                    intermediateVal += prevPROfNode;
                }
                intermediateVal *= dampingFactor;
                prOfCurrentNode += intermediateVal;
                //updating the value of the current node
                pageRankCurrent.put(currentNode, prOfCurrentNode);
                

            }
            //after the iteration is done we will change previous to our current
            //now we check the error margin and see if for all new ranks the differnece less than 0.01 if so we stop iterating
            double errorMargin = 99;
            Double[] oldValues = pageRankPrev.values().toArray(new Double[0]);
            Double[] newValues = pageRankCurrent.values().toArray(new Double[0]);
            double sum = 0;
            for (int i=0;i<newValues.length;i++)
            {
                sum+= newValues[i];
            }
            //System.out.println(sum);
            for(int i=0;i<oldValues.length;i++)
            {
                double currentError = Math.abs(oldValues[i] - newValues[i]);
                if(currentError<errorMargin)
                    errorMargin =currentError;
            }





            pageRankPrev = pageRankCurrent;
            pageRankCurrent =  new HashMap<String,Double>();
            iterations+=1;
        }
        return pageRankPrev;

        

    }

    public static void main(String[] args) {
        try {
            Utils utils = new Utils();
            HashMap<String, HashSet<String>> x = utils.cleanPageDegreeFile(CRAWLER_PROGRESS_PATH + PAGE_DEGREE_SAVE_FILE);
            //System.out.print(x);
            //System.out.println(x);
            HashMap<String,Double> pageRank = calculatePageRank(x,utils.getInPageMap(x));
            double sum = 0;
            for (String lambda: pageRank.keySet())
            {
                sum+= pageRank.get(lambda);
            }
            System.out.println(sum);
            System.out.print(pageRank);
            //System.out.println("Count in\n\n");
            //System.out.println(utils.getInPageMap(x));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

