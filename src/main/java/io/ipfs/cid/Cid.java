package io.ipfs.cid;

import io.ipfs.multibase.*;
import io.ipfs.multihash.*;

import java.io.*;
import java.util.*;

public class Cid {
    public enum Codec {
        Raw(0x55),
        DagProtobuf(0x70),
        DagCbor(0x71),
        EthereumBlock(0x90),
        EthereumTx(0x91),
        BitcoinBlock(0xb0),
        BitcoinTx(0xb1),
        ZcashBlock(0xc0),
        ZcashTx(0xc1);

        public long type;

        Codec(long type) {
            this.type = type;
        }

        private static Map<Long, Codec> lookup = new TreeMap<>();
        static {
            for (Codec c: Codec.values())
                lookup.put(c.type, c);
        }

        public static Codec lookup(long c) {
            if (!lookup.containsKey(c))
                throw new IllegalStateException("Unknown Codec type: " + c);
            return lookup.get(c);
        }
    }

    public final long version;
    public final Codec codec;
    public final Multihash hash;

    public Cid(long version, Codec codec, Multihash hash) {
        this.version = version;
        this.codec = codec;
        this.hash = hash;
    }

    private byte[] toBytesV0() {
        return hash.toBytes();
    }

    private static  final int MAX_VARINT_LEN64 = 10;

    private byte[] toBytesV1() {
        byte[] hashBytes = hash.toBytes();
        byte[] res = new byte[2 * MAX_VARINT_LEN64 + hashBytes.length];
        int index = putUvarint(res, 0, version);
        index = putUvarint(res, index, codec.type);
        System.arraycopy(hashBytes, 0, res, index, hashBytes.length);
        return Arrays.copyOfRange(res, 0, index + hashBytes.length);
    }

    public byte[] toBytes() {
        if (version == 0)
            return toBytesV0();
        else if (version == 1)
            return toBytesV1();
        throw new IllegalStateException("Unknown cid version: " + version);
    }

    @Override
    public String toString() {
        if (version == 0) {
            return hash.toString();
        } else if (version == 1) {
            return Multibase.encode(Multibase.Base.Base58BTC, toBytesV1());
        }
        throw new IllegalStateException("Unknown Cid version: " + version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cid cid = (Cid) o;

        if (version != cid.version) return false;
        if (codec != cid.codec) return false;
        return hash != null ? hash.equals(cid.hash) : cid.hash == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (version ^ (version >>> 32));
        result = 31 * result + (codec != null ? codec.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
    }

    public static Cid buildCidV0(Multihash hash) {
        return new Cid(0, Codec.DagProtobuf, hash);
    }

    public static Cid buildCidV1(Codec c, Multihash hash) {
        return new Cid(1, c, hash);
    }

    public static Cid decode(String v) throws IOException {
        if (v.length() < 2)
            throw new IllegalStateException("Cid too short!");

        // support legacy format
        if (v.length() == 46 && v.startsWith("Qm"))
            return buildCidV0(Multihash.fromBase58(v));

        byte[] data = Multibase.decode(v);
        return cast(data);
    }

    public static Cid cast(byte[] data) throws IOException {
        if (data.length == 34 && data[0] == 0x18 && data[1] == 32)
            return buildCidV0(new Multihash(data));

        InputStream in = new ByteArrayInputStream(data);
        long version = readVarint(in);
        if (version != 0 && version != 1)
            throw new IllegalStateException("Invalid Cif version number: " + version);

        long codec = readVarint(in);
        if (version != 0 && version != 1)
            throw new IllegalStateException("Invalid Cif version number: " + version);

        Multihash hash = Multihash.deserialize(new DataInputStream(in));

        return new Cid(version, Codec.lookup(codec), hash);
    }

    private static long readVarint(InputStream in) throws IOException {
        long x = 0;
        int s=0;
        for (int i=0; i < 10; i++) {
            int b = in.read();
            if (b == -1)
                throw new EOFException();
            if (b < 0x80) {
                if (i > 9 || i == 9 && b > 1) {
                    throw new IllegalStateException("Overflow reading varint" +(-(i + 1)));
                }
                return x | (((long)b) << s);
            }
            x |= ((long)b & 0x7f) << s;
            s += 7;
        }
        throw new IllegalStateException("Varint too long!");
    }

    private static int putUvarint(byte[] buf, int index, long x) {
        while (x >= 0x80) {
            buf[index] = (byte)(x | 0x80);
            x >>= 7;
            index++;
        }
        buf[index] = (byte)x;
        return index + 1;
    }
}
