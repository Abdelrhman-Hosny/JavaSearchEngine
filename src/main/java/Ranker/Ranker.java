package Ranker;
import java.sql.*;
import java.io.IOException;
import java.lang.Math;
import java.util.*;
import Database.RankerDAO;


public class Ranker {
    //here we will define some general variables that we will change locally

    public RankerDAO rankerDB = new RankerDAO();
    public static int numberOfThreads = 2;

    //this will be used in the priority queue
    public static class Entry {
        private String key;
        private double value;

        public double getValue() {
            return this.value;
        }
        public String getKey()
        {
            return this.key;
        }


        public Entry(String key, double value) {
            this.key = key;
            this.value = value;
        }
    }
    public HashMap<String,Double> calculatePageRank(HashMap<String, HashSet<String>> outgoing,HashMap<String, HashSet<String>> ingoing)
    {
        //the algorithm takes two hashmaps, one that takes each url for ex(A) and states all outgoing urls from that url(A)

        //the other hashMap takes url(A) and states all incoming urls pointing towards url(A)

        //Damping factor which is 85% that states if the algorithm output didn't change by 15% this means we will stop

        //we will save the rank of each url in each step in a HashMap where this value will be updated each iteration
        double dampingFactor = 1;
        HashMap<String,Double> pageRankPrev = new HashMap<String,Double>();
        HashMap<String,Double> pageRankCurrent = new HashMap<String,Double>();
        double initialPageRank = 1/(double)outgoing.size();
        


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
    public static void createInsertQuery(Connection conn, String query)  {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
             stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public static void addPageRankToDB(Connection conn,HashMap<String,Double> pageRank)
    {
        String[] documentNames = pageRank.keySet().toArray(new String[0]);
        for(int i=0;i<documentNames.length;i++)
        {
            String queryAddtoDB = "insert into document_rank "+
            " values('"+documentNames[i]+"',"+pageRank.get(documentNames[i]) +")";
            System.out.println(queryAddtoDB);
            createInsertQuery(conn, queryAddtoDB);
        }
    }
    public void uploadPageRank(HashMap<String,Double> pageRank) throws IOException
    {
        // calling ranker on db
        rankerDB.addPageRank(pageRank);

    }

    


    public Object[] process(String search,boolean isPhrase) throws SQLException, InterruptedException, IOException {
        //assuming that the connection to the sql server is made
        //and we have a string of words after they are query processed
        //we are going to calculate the tf-idf of the word we are getting through the following queries
        // String search = "algorithms computer";
        String[] searchQuery = search.split(" ");
        //for each word we will compute term frequency
        
        //add all these words in one statement
        PriorityQueue<Entry> rankerResult = new PriorityQueue<Entry>(new Comparator<Ranker.Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                if (o1.getValue() < o2.getValue())
                    return 1;
                else if (o1.getValue() > o2.getValue())
                    return -1;
                else
                    return 0;
            }
        });

        
        

        //first we get all documents where all these words appears
        String finalWords = "(";
        for (int i = 0; i < searchQuery.length; i++) {
            finalWords += "'"+ searchQuery[i] +"'"+ ",";
        }
        int index = finalWords.lastIndexOf(',');
        finalWords = finalWords.substring(0,index);
        finalWords += ")";
        System.out.println(finalWords);
        //we are going to assume for two words at least to appear

        ResultSet rsDocumentsWhereAllWordsAppears = null ;
        if(!isPhrase){
            rsDocumentsWhereAllWordsAppears = rankerDB.GetDocumentsWhereAllWordsAppears(finalWords);
        }else{
            rsDocumentsWhereAllWordsAppears = rankerDB.GetPhraseLevelDocumentsNames(search);
        }
//        String[] mainDocuments = new String[100];
        ArrayList<String> mainDocuments = new ArrayList<String>();

        while (rsDocumentsWhereAllWordsAppears.next()) {
            mainDocuments.add(rsDocumentsWhereAllWordsAppears.getString("document_name"));
        }

        //first step is to calculate total number of documents
        int countTotalNumberOfDocuments = rankerDB.getTotalNumberOfDocument();
        System.out.println("Total number of documents is " + countTotalNumberOfDocuments);

        //-----------------------------------------------------------------------------------------------------------

        
        int shareOfThread = mainDocuments.size() / Ranker.numberOfThreads;
        //this means that the number of documents is less than number of threads there for we only need one thread
        Thread []listOfThreads = new Thread[numberOfThreads];
        if(shareOfThread == 0)
        {
            documentRank currentShareForThread = new documentRank(mainDocuments,searchQuery,rankerResult,countTotalNumberOfDocuments);

            listOfThreads[0] = new Thread(currentShareForThread);
            listOfThreads[0].start();
            listOfThreads[0].join();
        }

        else {

            ArrayList<ArrayList<String>> listOfShares = new ArrayList<ArrayList<String>>();
            for (int i = 0; i < numberOfThreads; i++) {
                ArrayList<String> shareOfDocumentsForThread = new ArrayList<String>();
                for (int j = 0; j < shareOfThread; j++) {
                    System.out.println(mainDocuments.get(j + i * shareOfThread));
                    shareOfDocumentsForThread.add(mainDocuments.get(j + i * shareOfThread));
                }
                System.out.println(shareOfDocumentsForThread);

                listOfShares.add(shareOfDocumentsForThread);
            }

            //now we loop through this list and create a thread for each share
//            Thread[] listOfThreads = new Thread[4];
            for (int i = 0; i < numberOfThreads; i++) {
                documentRank currentShareForThread = new documentRank(listOfShares.get(i), searchQuery, rankerResult, countTotalNumberOfDocuments);
                System.out.println(listOfShares.get(i).size());
                listOfThreads[i] = new Thread(currentShareForThread);
                listOfThreads[i].start();
            }
            for(int i=0;i<numberOfThreads;i++)
            {
                System.out.println("Joining threads");
                try {
                    listOfThreads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


        /*we are going to create number of threads equivalent to the number of words in our query where each query
          will get number of documents of this word and compute the TF of the word
        */

        return rankerResult.toArray();
    }
}
