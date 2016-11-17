package org.inpher.samples.query.spark;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Comparator;

public class GutenbergSparkApp {
    public static final String ROOT_PATH = GutenbergSparkApp.class.getResource("/gutenberg/").getPath();
    public static void main(String[] args){
        InpherSparkWordCountUtils iswcu = new InpherSparkWordCountUtils();

        // Get WordCount from the bible
        JavaPairRDD<String, Integer> bibleWordCount = iswcu.getWordCountsFromFile(ROOT_PATH + "/bible.txt");

        // Get WordCount from the CIA fact book
        JavaPairRDD ciaWordCount = iswcu.getWordCountsFromFile(ROOT_PATH + "/cia-factbook.txt");

        // Create a common map containing the aggregated word sum of both books
        JavaPairRDD combinedWordCount = ciaWordCount.union(bibleWordCount)
                .reduceByKey((Function2<Integer, Integer, Integer>) (a, b) -> a + b);

        // Collect, and sort top 100 words **without** decrypting, then load and decrypt for printing
        combinedWordCount.top(100, new TupleComparator()).stream().forEach(tup -> {
            Tuple2<String, Integer> tups = (Tuple2<String, Integer>) tup;
            System.out.println("Word: " + tups._1);
            System.out.println("Count: " + tups._2);
        });

        // Encrypt and store the map containing the aggregated word sum of both books
        JavaRDD<Tuple2<byte[], byte[]>> encWordCounts = iswcu.encryptWordCountPairs(bibleWordCount);

        // Collect, and sort top 100 words **without** decrypting, then load and decrypt for printing
        encWordCounts.top(100, iswcu.getOREComparator()).stream().forEach(tup -> {
            Tuple2<String, Integer> decr = iswcu.decryptTuple((Tuple2<byte[], byte[]>) tup);
            System.out.println("Word: " + decr._1);
            System.out.println("Count: " + decr._2);
        });
    }
}

class TupleComparator implements Comparator<Tuple2<String, Integer>>, Serializable {
    @Override
    public int compare(Tuple2<String, Integer> tuple1, Tuple2<String, Integer> tuple2) {
        return tuple1._2.compareTo(tuple2._2);
    }
}

