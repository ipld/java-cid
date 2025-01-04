package io.ipfs.cid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import io.ipfs.multibase.Multibase;
import io.ipfs.multihash.Multihash;

public class Cid extends Multihash {

    public static final class CidEncodingException extends RuntimeException {

        public CidEncodingException(String message) {
            super(message);
        }

        public CidEncodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public enum Codec {
        // https://github.com/multiformats/multicodec/blob/master/table.csv
        Cbor(0x51, "cbor"),
        Raw(0x55, "raw"),
        DagProtobuf(0x70, "dag-pb"),
        DagCbor(0x71, "dag-cbor"),
        Libp2pKey(0x72, "libp2p-key"),
        EthereumBlock(0x90, "eth-block"),
        EthereumTx(0x91, "eth-block-list"),
        BitcoinBlock(0xb0, "bitcoin-block"),
        BitcoinTx(0xb1, "bitcoin-tx"),
        ZcashBlock(0xc0, "zcash-block"),
        ZcashTx(0xc1, "zcash-tx");

        public final long type;
        public final String name;

        Codec(long type, String name) {
            this.type = type;
            this.name = name;
        }

        private static Map<Long, Codec> lookup = new TreeMap<>();
        private static Map<String, Codec> nameLookup = new TreeMap<>();
        static {
            for (Codec c : Codec.values()) {
                lookup.put(c.type, c);
                nameLookup.put(c.name, c);
            }
        }

        public static Codec lookup(long c) {
            Codec codec = lookup.get(c);
            if (codec == null)
                throw new IllegalStateException("Unknown Codec type: " + c);
            return codec;
        }

        public static Codec lookupIPLDName(String name) {
            Codec codec = nameLookup.get(name);
            if (codec == null)
                throw new IllegalStateException("Unknown Codec type: " + name);
            return codec;
        }
    }

    public final long version;
    public final Codec codec;

    public Cid(long version, Codec codec, Multihash.Type type, byte[] hash) {
        super(type, hash);
        this.version = version;
        this.codec = codec;
    }

    public static Cid build(long version, Codec codec, Multihash h) {
        return new Cid(version, codec, h.getType(), h.getHash());
    }

    private byte[] toBytesV0() {
        return super.toBytes();
    }

    private byte[] toBytesV1() {
        try {
            ByteArrayOutputStream res = new ByteArrayOutputStream();
            putUvarint(res, version);
            putUvarint(res, codec.type);
            super.serialize(res);
            return res.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte[] toBytes() {
        if (version == 0)
            return toBytesV0();
        else if (version == 1)
            return toBytesV1();
        throw new IllegalStateException("Unknown CID version: " + version);
    }

    @Override
    public String toString() {
        if (version == 0) {
            return super.toString();
        } else if (version == 1) {
            return Multibase.encode(Multibase.Base.Base32, toBytesV1());
        }
        throw new IllegalStateException("Unknown CID version: " + version);
    }

    @Override
    public Multihash bareMultihash() {
        return new Multihash(getType(), getHash());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Multihash))
            return false;
        if (!super.equals(o))
            return false;

        if (o instanceof Cid) {
            Cid cid = (Cid) o;

            if (version != cid.version)
                return false;
            return codec == cid.codec;
        }
        // o must be a Multihash
        return version == 0 && super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        if (version == 0)
            return result;
        result = 31 * result + (int) (version ^ (version >>> 32));
        result = 31 * result + (codec != null ? codec.hashCode() : 0);
        return result;
    }

    public static Cid buildV0(Multihash h) {
        return Cid.build(0, Codec.DagProtobuf, h);
    }

    public static Cid buildCidV1(Codec c, Multihash.Type type, byte[] hash) {
        return new Cid(1, c, type, hash);
    }

    public static Cid decode(String v) {
        if (v.length() < 2)
            throw new IllegalStateException("CID too short: " + v);

        // support legacy format
        if (v.length() == 46 && v.startsWith("Qm"))
            return buildV0(Multihash.fromBase58(v));

        byte[] data = Multibase.decode(v);
        return cast(data);
    }

    public static Cid cast(byte[] data) {
        if (data.length == 34 && data[0] == 18 && data[1] == 32)
            return buildV0(new Multihash(Type.lookup(data[0] & 0xff), Arrays.copyOfRange(data, 2, data.length)));

        InputStream in = new ByteArrayInputStream(data);
        try {
            long version = readVarint(in);
            if (version != 0 && version != 1)
                throw new CidEncodingException("Invalid CID version number: " + version);

            long codec = readVarint(in);
            Multihash hash = Multihash.deserialize(in);

            return new Cid(version, Codec.lookup(codec), hash.getType(), hash.getHash());
        } catch (CidEncodingException cee) {
            throw cee;
        } catch (Exception e) {
            throw new CidEncodingException("Invalid CID bytes: " + bytesToHex(data), e);
        }
    }

    private static String[] HEX_DIGITS = new String[] {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
    private static String[] HEX = new String[256];
    static {
        for (int i = 0; i < 256; i++)
            HEX[i] = HEX_DIGITS[(i >> 4) & 0xF] + HEX_DIGITS[i & 0xF];
    }

    private static String byteToHex(byte b) {
        return HEX[b & 0xFF];
    }

    private static String bytesToHex(byte[] data) {
        StringBuilder s = new StringBuilder(data.length * 2);
        for (byte b : data)
            s.append(byteToHex(b));
        return s.toString();
    }
}
