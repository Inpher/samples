package org.inpher.samples.query.spark;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function2;
import org.inpher.crypto.engines.paillier.PaillierEngine;
import scala.Tuple2;
import scala.Tuple3;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;

public class GutenbergSparkApp {
    public static final String ROOT_PATH = GutenbergSparkApp.class.getResource("/gutenberg/").getPath();
    public static void main(String[] args) throws IOException {
        InpherSparkWordCountUtils iswcu = new InpherSparkWordCountUtils();

        // Get WordCount from the bible
        JavaPairRDD<String, Integer> bibleWordCount = iswcu.getWordCountsFromFile(ROOT_PATH + "/bible-short.txt");

        // Get WordCount from the CIA fact book
        JavaPairRDD ciaWordCount = iswcu.getWordCountsFromFile(ROOT_PATH + "/cia-factbook-short.txt");

        // Create a common map containing the aggregated word sum of both books
        JavaPairRDD combinedWordCount = ciaWordCount.union(bibleWordCount)
                .reduceByKey((Function2<Integer, Integer, Integer>) (a, b) -> a+b);

        System.out.println("Number of unique words:" + combinedWordCount.count());

        // Collect, and sort top 100 words **without** decrypting, then load and decrypt for printing
        combinedWordCount.top(10, new TupleComparator()).stream().forEach(tup -> {
            Tuple2<String, Integer> tups = (Tuple2) tup;
            System.out.println("Word: " + tups._1);
            System.out.println("Count: " + tups._2);
        });

        // Encrypt and store the map containing the aggregated word sum of both books
        JavaRDD<Tuple3<byte[], byte[], byte[]>> encWordCounts = iswcu.encryptWordCountPairs(combinedWordCount);

        // Collect, and sort top 10 words **without** decrypting, then load and decrypt for printing
        encWordCounts.top(10, InpherSparkWordCountUtils.comparator).stream().forEach(tup -> {
            Tuple2<String, Integer> decr = iswcu.decryptTuple(tup);
            System.out.println("Word: " + decr._1);
            System.out.println("Count: " + decr._2);
        });

        // Collect, and sort top 10 words **without** decrypting, then load and decrypt for printing
        System.out.println("Total number of words:" +
                PaillierEngine.decrypt(iswcu.getPaillierPrivateKey(), new BigInteger(
                        encWordCounts.top(10, InpherSparkWordCountUtils.comparator)
                                .stream()
                                .reduce((tup1, tup2) -> new Tuple3<>(null,null,iswcu.paillierAddition(tup1,tup2))).get()._3())));

        // Press enter to exit
        System.out.println('\n' + "press enter to exit");
        System.in.read();
    }
}

class TupleComparator implements Comparator<Tuple2<String, Integer>>, Serializable {
    @Override
    public int compare(Tuple2<String, Integer> tuple1, Tuple2<String, Integer> tuple2) {
        return tuple1._2.compareTo(tuple2._2);
    }
}

