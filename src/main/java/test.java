import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class test {
    public static class Entry implements Comparable<Entry> {
        private String key;
        private double value;

        public double getValue()
        {
            return this.value;
        }



        public Entry(String key, double value) {
            this.key = key;
            this.value = value;
        }

        // getters

 
    }

    public static void main(String args[])
    {
        PriorityQueue<Entry> q = new PriorityQueue<Entry>(new Comparator<Entry>() {
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
        Entry t1 = new Entry("d1",0.2);
        Entry t2 = new Entry("d2",0.6);
        Entry t3 = new Entry("d3",0.3);
        q.add(t1);
        q.add(t2);
        q.add(t3);


        Iterator value = q.iterator();

        // Displaying the values after iterating through the queue
        System.out.println("The iterator values are: ");
        Entry test = q.peek();
        System.out.println(test.getValue());

    }
}
