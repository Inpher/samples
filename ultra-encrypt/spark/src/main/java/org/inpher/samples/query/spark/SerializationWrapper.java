package org.inpher.samples.query.spark;

import org.inpher.crypto.engines.paillier.PaillierKeyPair;
import org.inpher.crypto.engines.paillier.PaillierPrivateKey;
import org.inpher.crypto.engines.paillier.PaillierPublicKey;

/**
 * Created by alex on 11/16/16.
 */
class SerializationWrapper {
    private final byte[] paillierPrivKey;
    private final byte[] paillierPubKey;
    private final byte[] aesKey;
    private final byte[] oreKey;

    public SerializationWrapper(byte[] aesKey, byte[] oreKey, PaillierKeyPair paillierKeyPair){
        this.aesKey = aesKey;
        this.oreKey = oreKey;
        this.paillierPubKey = paillierKeyPair.getPublicKey().serialize();
        this.paillierPrivKey = paillierKeyPair.getPrivateKey().serialize();
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    public byte[] getOreKey() {
        return oreKey;
    }

    public PaillierKeyPair getPaillerKeyPair() {
        return new PaillierKeyPair(PaillierPublicKey.deserialize(paillierPubKey),
                PaillierPrivateKey.deserialize(paillierPrivKey));
    }
}
