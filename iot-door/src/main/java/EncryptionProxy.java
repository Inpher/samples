import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.inpher.crypto.CryptoEngine;
import org.inpher.crypto.CryptoModule;
import org.inpher.crypto.engines.DeterministicEngine;
import org.inpher.crypto.engines.ore.AbstractOREEngine;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 1/26/17.
 */
public class EncryptionProxy {
    // Keys
    private static final String oreKey = "AnO+eobH43Rz4QFOw2At4Lr9fonsQRq5SPDp1uIYSGFVAQFdhdDYfHliI5/RMqaDhQR9uYaOFWnBuriYMm5ajEq3";
    private static final String aesKey = "Acl6agqoU4LNVSb68MbYfYzESpJVEpUAVgf5NVJG7d9Q";
    private static final String deterKey = "AKpZFvxE8gURzCQHQP/RfdBZCfdxOjqs6l/3/HjG3LDcKl+5AGhiUjYW/Sa/2BiEKQ==";

    // Engines
    private static final AbstractOREEngine oreDateEngine = CryptoModule.newDateOrderRevealingEncryptionFactory().createEngine(fromB64(oreKey));
    private static final CryptoEngine aesEngine = CryptoModule.newAuthenticatedEncryptionEngine(fromB64(aesKey));
    private static final CryptoEngine deterEngine = new DeterministicEngine(fromB64(deterKey));

    // Stuff
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final String elasticIP = "demo-zeppelin.inpher.io";
    private static TransportClient client;

    // Setup elastic client
    static {
        try {
            Settings settings = Settings.builder().put("cluster.name", "inpher-es").build();
            client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticIP), 9300));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void encryptAndIndexJSON(Map<String, String> jsonClear) throws ParseException, IOException {
        System.out.println("ORE Key: " + toB64(oreDateEngine.getKey()));
        System.out.println("AES Key: " + toB64(aesEngine.getKey()));
        System.out.println("Deterministic Key: " + toB64(deterEngine.getKey()));
        // Encrypt
        Map jsonEnc = new HashMap();
        jsonEnc.put("time", oreDateEncrypt(jsonClear.get("time")));
        jsonEnc.put("zip", deterEncrypt(jsonClear.get("zip")));
        jsonEnc.put("door_id", deterEncrypt(jsonClear.get("door_id")));
        jsonEnc.put("last_name", deterEncrypt(jsonClear.get("last_name")));
        jsonEnc.put("first_name", deterEncrypt(jsonClear.get("first_name")));
        jsonEnc.put("building", deterEncrypt(jsonClear.get("building")));
        jsonEnc.put("msg", aesEncrypt(new ObjectMapper().writeValueAsString(jsonClear)));

        System.out.println("INPHER PROXY: DEBUG encrypted event:\n" + jsonEnc +"\n");

        // Send to elastic search
        IndexResponse response = client.prepareIndex("iotindex", "access")
                .setSource(jsonEnc)
                .get();
        System.out.println(response);


    }

    private static String oreDateEncrypt(String date) throws ParseException {
        return toB64(oreDateEngine.encrypt(dateFormat.parse(date)));
    }

    private static String deterEncrypt(String str){
        return toB64(deterEngine.encrypt(str.getBytes()));
    }

    private static String aesEncrypt(String str){
        return toB64(aesEngine.encrypt(str.getBytes()));
    }

    private static String toB64(byte[] b){
        return Base64.getEncoder().encodeToString(b);
    }

    private static byte[] fromB64(String s){
        return Base64.getDecoder().decode(s);
    }
}
