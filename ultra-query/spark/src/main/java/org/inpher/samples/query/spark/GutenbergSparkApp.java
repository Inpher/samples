package org.inpher.samples.query.spark;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function2;

import java.io.IOException;

public class GutenbergSparkApp {
    public static final String ROOT_PATH = GutenbergSparkApp.class.getResource("/gutenberg/").getPath();
    public static void main(String[] args) throws IOException {
        InpherSparkWordCountUtils iswcu = new InpherSparkWordCountUtils();

        // Get WordCount from the bible
        JavaPairRDD bibleWordCount = iswcu.getWordCountsFromFile(ROOT_PATH + "/bible.txt");

        // Get WordCount from the CIA fact book
        JavaPairRDD ciaWordCount = iswcu.getWordCountsFromFile(ROOT_PATH + "/cia-factbook.txt");

        // Create a common map containing the aggregated word sum of both books
        JavaPairRDD combinedWordCount = ciaWordCount.union(bibleWordCount)
                .reduceByKey((Function2<Integer, Integer, Integer>) (a, b) -> a + b);

        // Encrypt and store the map containing the aggregated word sum of both books
        iswcu.storeEncriptedMap(iswcu.encryptWordCountPairs(combinedWordCount), ROOT_PATH + "/enc-wordpairs");

        // Load, decrypt and print them again
        iswcu.decryptWordCountPairs(iswcu.loadEncryptedMap(ROOT_PATH + "/enc-wordpairs")).top(100, new TupleComparator()).stream().forEach(tup ->{
                System.out.println("Word: " + tup._1);
                System.out.println("Count: "+ tup._2);
                System.out.println();
        });

        // Press enter to exit
        System.out.println('\n' + "press enter to exit");
        System.in.read();
    }
}

