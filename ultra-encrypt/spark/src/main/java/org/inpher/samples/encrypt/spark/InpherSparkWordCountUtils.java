package org.inpher.samples.encrypt.spark;

import com.google.gson.Gson;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.inpher.crypto.CryptoEngine;
import org.inpher.crypto.CryptoModule;
import org.inpher.crypto.engines.AuthenticatedEncryptionEngine;
import scala.Tuple2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

class InpherSparkWordCountUtils implements Serializable {
    private final JavaSparkContext sc;
    private static final String APP_NAME = "Encrypted BookWordCountIndexer";
    private CryptoEngine aesEngine;

    /**
     * Constructs this helper class using new random keys
     */
    public InpherSparkWordCountUtils() {
        SparkConf conf = new SparkConf().setAppName(APP_NAME);
        this.sc = new JavaSparkContext("local", "test", conf);

        //Engine used to encrypt. We want a random key to be generated
        this.aesEngine = CryptoModule.newAuthenticatedEncryptionEngine();
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
        this.aesEngine = CryptoModule.newAuthenticatedEncryptionEngine(aesKey);
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
        return pairs.map(e -> new Tuple2<>(aesEngine.encrypt(e._1.getBytes()), aesEngine.encrypt(Integer.toString(e._2).getBytes())));
    }

    /**
     * Decrypts the give word/count pair using the inpher crypto module and the default keys
     *
     * @param encPairs encrypted word/count pair
     * @return decrypted word/count pair
     */
    public JavaPairRDD<String, Integer> decryptWordCountPairs(JavaRDD<Tuple2<byte[],byte[]>> encPairs){
        return JavaPairRDD.fromJavaRDD(encPairs.map(e ->
                new Tuple2<>(new String(aesEngine.decrypt(e._1)), Integer.valueOf(new String(aesEngine.decrypt(e._2))))));
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
     * Serializes the AES engine using gson
     *
     * @param os OutputStream
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream os) throws IOException {
        Gson gson = new Gson();
        os.writeObject(gson.toJson(aesEngine.getKey()));
    }

    /**
     * Deserializes the AES engine using gson
     *
     * @param is InputStream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();
        String json = (String) is.readObject();
        aesEngine = new AuthenticatedEncryptionEngine(gson.fromJson(json, byte[].class));
    }
}
