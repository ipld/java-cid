package io.ipfs.cid;

import io.ipfs.multihash.*;
import org.junit.jupiter.api.Test;
import io.ipfs.multibase.*;

import java.io.*;
import java.security.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CidTest {

    @Test
    void validStrings() {
        List<String> examples = Arrays.asList(
                "QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB",
                "QmatmE9msSfkKxoffpHwNLNKgwZG8eT9Bud6YoPab52vpy",
                "bafyreigivjmlrue5db7rpwmbonv7oq57hvnp7yzhlsoy3fbwi5jzhwgali"
        );
        for (String example: examples) {
            Cid cid = Cid.decode(example);
            String encoded = cid.toString();
            if (!encoded.equals(example))
                throw new IllegalStateException("Incorrect cid string! " + example + " => " + encoded);
        }
    }

    @Test
    void emptyStringShouldFail() throws IOException {
        try {
            Cid cid = Cid.decode("");
            throw new RuntimeException();
        } catch (IllegalStateException e) {}
    }

    @Test
    void basicMarshalling() throws Exception {
        MessageDigest hasher = MessageDigest.getInstance("SHA-512");
        byte[] hash = hasher.digest("TEST".getBytes());

        Cid cid = new Cid(1, Cid.Codec.Raw, Multihash.Type.sha2_512, hash);
        byte[] data = cid.toBytes();

        Cid cast = Cid.cast(data);
        assertEquals(cast, cid, "Invertible serialization");

        Cid fromString = Cid.decode(cid.toString());
        assertEquals(fromString, cid, "Invertible toString");
    }

    @Test
    void version0Handling() throws Exception {
        String hashString = "QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB";
        Cid cid = Cid.decode(hashString);

        assertEquals(0, cid.version, "version 0");

        assertEquals(cid.toString(), hashString, "Correct hash");
    }

    @Test
    void version0Error() throws Exception {
        String invalidString = "QmdfTbBqBPQ7VNxZEYEj14VmRuZBkqFbiwReogJgS1zIII";
        try {
            Cid cid = Cid.decode(invalidString);
            throw new RuntimeException();
        } catch (IllegalStateException e) {}
    }

    @Test
    void lookByOfficialIPLDName() {
        Cid.Codec raw = Cid.Codec.lookupIPLDName("raw");
        assertEquals("Raw", raw.name(), "Raw Codec");
        Cid.Codec dagcbor = Cid.Codec.lookupIPLDName("dag-cbor");
        assertEquals("DagCbor", dagcbor.name(), "DagCbor Codec");
    }
}
