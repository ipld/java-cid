package io.ipfs.cid;

import io.ipfs.multihash.*;
import org.junit.*;
import io.ipfs.multibase.*;

import java.io.*;
import java.security.*;
import java.util.*;

public class CidTest {

    @Test
    public void validStrings() {
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
    public void emptyStringShouldFail() throws IOException {
        try {
            Cid cid = Cid.decode("");
            throw new RuntimeException();
        } catch (IllegalStateException e) {}
    }

    @Test
    public void basicMarshalling() throws Exception {
        MessageDigest hasher = MessageDigest.getInstance("SHA-512");
        byte[] hash = hasher.digest("TEST".getBytes());

        Cid cid = new Cid(1, Cid.Codec.Raw, Multihash.Type.sha2_512, hash);
        byte[] data = cid.toBytes();

        Cid cast = Cid.cast(data);
        Assert.assertTrue("Invertible serialization", cast.equals(cid));

        Cid fromString = Cid.decode(cid.toString());
        Assert.assertTrue("Invertible toString", fromString.equals(cid));
    }

    @Test
    public void version0Handling() throws Exception {
        String hashString = "QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB";
        Cid cid = Cid.decode(hashString);

        Assert.assertTrue("version 0", cid.version == 0);

        Assert.assertTrue("Correct hash", cid.toString().equals(hashString));
    }

    @Test
    public void version0Error() throws Exception {
        String invalidString = "QmdfTbBqBPQ7VNxZEYEj14VmRuZBkqFbiwReogJgS1zIII";
        try {
            Cid cid = Cid.decode(invalidString);
            throw new RuntimeException();
        } catch (IllegalStateException e) {}
    }

    @Test
    public void lookByOfficialIPLDName() {
        Cid.Codec raw = Cid.Codec.lookupIPLDName("raw");
        Assert.assertTrue("Raw Codec", raw.name().equals("Raw"));
        Cid.Codec dagcbor = Cid.Codec.lookupIPLDName("dag-cbor");
        Assert.assertTrue("DagCbor Codec", dagcbor.name().equals("DagCbor"));
    }
}
