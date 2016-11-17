package org.inpher.samples.query.spark;

import com.google.gson.Gson;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.inpher.crypto.CryptoEngine;
import org.inpher.crypto.CryptoModule;
import org.inpher.crypto.engines.AuthenticatedEncryptionEngine;
import org.inpher.crypto.engines.ore.AbstractOREEngine;
import org.inpher.crypto.engines.ore.AbstractOREFactory;
import org.inpher.crypto.engines.ore.CipherTextComparator;
import scala.Tuple2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;


class InpherSparkWordCountUtils implements Serializable {
    private final JavaSparkContext sc;
    private static final String APP_NAME = "Encrypted BookWordCountIndexer";
    private CryptoEngine aesEngine;
    private AbstractOREEngine<Integer> oreEngine;
    private WrappedComparator comparator;

    /**
     * Constructs this helper class using new random keys
     */
    public InpherSparkWordCountUtils() {
        SparkConf conf = new SparkConf().setAppName(APP_NAME);
        this.sc = new JavaSparkContext("local", "test", conf);

        //Engine used to encrypt. We want a random key to be generated
        AbstractOREFactory<Integer> factory = CryptoModule.newIntegerOrderRevealingEncryptionFactory();
        this.oreEngine = factory.createEngine();
        this.aesEngine = CryptoModule.newAuthenticatedEncryptionEngine();
        this.comparator = new WrappedComparator(oreEngine.getCipherTextComparator());
    }

    /**
     * Constructs this helper class using given keys
     *
     * @param aesKey
     * @param oreKey
     */
    public InpherSparkWordCountUtils(byte[] aesKey, byte[] oreKey){
        SparkConf conf = new SparkConf().setAppName(APP_NAME);
        this.sc = new JavaSparkContext(conf);

        //Engine used to encrypt. We want a random key to be generated
        AbstractOREFactory<Integer> factory = CryptoModule.newIntegerOrderRevealingEncryptionFactory();
        this.oreEngine = factory.createEngine(oreKey);
        this.aesEngine = CryptoModule.newAuthenticatedEncryptionEngine(aesKey);
        this.comparator = new WrappedComparator(oreEngine.getCipherTextComparator());
    }

    /**
     * Extracts the word cound from a given file (can be local, hdfs or any spark compatible storage backend)
     *
     * @param path of the file
     * @return map containing plaintext wordcount
     */
    public JavaPairRDD<String, Integer> getWordCountsFromFile(String path){
        // Create flat map from text file
        return  sc.textFile(path).map(x -> x.replace(',',' ').replace('.',' ').replace('-',' ').toLowerCase())
                .flatMap(s -> Arrays.asList(s.split(" ")).iterator())
                .filter(s -> !s.equals(""))
                .mapToPair(s -> new Tuple2<>(s, 1))
                .reduceByKey((a, b) -> a + b);
    }

    /**
     * Encrypts the given word/count pair using the inpher crypto module. The count is encrypted using
     * ORE in order to be able to retrieve sorted values without decrypting the data first
     *
     * @param pairs
     * @return Encrypted byte tuple containing
     */
    public JavaRDD<Tuple2<byte[], byte[]>> encryptWordCountPairs(JavaPairRDD<String, Integer> pairs){
        return pairs.map(e -> new Tuple2<>(aesEngine.encrypt(e._1.getBytes()), oreEngine.encrypt(e._2)));
    }

    /**
     * Decrypts the give word/count pair using the inpher crypto module and the default keys
     *
     * @param encPairs encrypted word/count pair
     * @return decrypted word/count pair
     */
    public JavaPairRDD<String, Integer> decryptWordCountPairs(JavaRDD<Tuple2<byte[],byte[]>> encPairs){
        return JavaPairRDD.fromJavaRDD(encPairs.map(e ->
                new Tuple2<>(aesEngine.decrypt(e._1).toString(), oreEngine.decrypt(e._2))));
    }

    /**
     * Stores the encrypted word / count pairs on the chosen storage backend. You can use a local
     * path, an hdfs path, or any spark compatible backend
     *
     * @param encPairs
     * @param storagePath
     */
    public void storeEncriptedMap(JavaRDD encPairs, String storagePath) {
        encPairs.saveAsObjectFile(storagePath);
    }

    /**
     * Loads the encrypted word / count pairs from the chosen storage backend.
     *
     * @param storagePath
     * @return JavaRDD encrypted word/count pairs
     */
    public JavaRDD<Tuple2<byte[],byte[]>> loadEncryptedMap(String storagePath){
        return sc.objectFile(storagePath);
    }

    /**
     * Returns the AESkey
     *
     * @return aes key
     */
    public byte[] getAESkey(){
        return this.aesEngine.getKey();
    }

    /**
     * Returns OREkey
     *
     * @return ore key
     */
    public byte[] getOREkey(){
        return this.getOREkey();
    }

    /**
     * Returns Ciphertext comparator
     *
     * @return comparator
     */
    public WrappedComparator getOREComparator(){
        return comparator;
    }

    /**
     * Returns the decrypted tuple
     *
     * @param tup encrypted tuple
     * @return decrypted tuple
     */
    public Tuple2<String,Integer> decryptTuple(Tuple2<byte[], byte[]> tup) {
        return new Tuple2<>(aesEngine.decrypt(tup._1).toString(), oreEngine.decrypt(tup._2));
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        Gson gson = new Gson();
        KeyWrapper wrap = new KeyWrapper(aesEngine.getKey(), oreEngine.getKey());
        os.writeObject(gson.toJson(wrap));
    }

    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();
        String json = (String) is.readObject();
        KeyWrapper wrap = gson.fromJson(json, KeyWrapper.class);
        aesEngine = new AuthenticatedEncryptionEngine(wrap.getAesKey());
        oreEngine = CryptoModule.newIntegerOrderRevealingEncryptionFactory().createEngine(wrap.getOreKey());
    }
}

class WrappedComparator implements Comparator<Tuple2<byte[], byte[]>>, Serializable{
    private CipherTextComparator comparator;

    public WrappedComparator(CipherTextComparator cipherTextComparator) {
        comparator = cipherTextComparator;
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        Gson gson = new Gson();
        System.out.println(gson.toJson(comparator));
        os.writeObject(gson.toJson(comparator));
    }

    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();
        String json = (String) is.readObject();
        this.comparator = gson.fromJson(json, CipherTextComparator.class);
    }

    @Override
    public int compare(Tuple2<byte[], byte[]> o1, Tuple2<byte[], byte[]> o2) {
        return this.comparator.compare(o1._2, o2._2);
    }
}
