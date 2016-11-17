package org.inpher.samples.query.spark;

/**
 * Created by alex on 11/16/16.
 */
class KeyWrapper {
    private byte[] aesKey;
    private byte[] oreKey;

    public KeyWrapper(byte[] aesKey, byte[] oreKey){
        this.aesKey = aesKey;
        this.oreKey = oreKey;
    }

    public byte[] getAesKey() {
        return aesKey;
    }

    public void setAesKey(byte[] aesKey) {
        this.aesKey = aesKey;
    }

    public byte[] getOreKey() {
        return oreKey;
    }

    public void setOreKey(byte[] oreKey) {
        this.oreKey = oreKey;
    }
}
