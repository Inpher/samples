package org.inpher.samples.encrypt.spark;

import scala.Tuple2;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares two String/Integer tuples by their Integer values
 */
class TupleComparator implements Comparator<Tuple2<String, Integer>>, Serializable {
    @Override
    public int compare(Tuple2<String, Integer> tuple1, Tuple2<String, Integer> tuple2) {
        return tuple1._2.compareTo(tuple2._2);
    }
}
