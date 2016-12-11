package io.ipfs.cid;

import org.junit.*;
import io.ipfs.multibase.*;
import java.util.*;

public class CidTest {

    @Test
    public void base58Test() {
        List<String> examples = Arrays.asList("QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB",
                "QmatmE9msSfkKxoffpHwNLNKgwZG8eT9Bud6YoPab52vpy");
        for (String example: examples) {
            byte[] output = Base58.decode(example);
            String encoded = Base58.encode(output);
            if (!encoded.equals(encoded))
                throw new IllegalStateException("Incorrect base58! " + example + " => " + encoded);
        }
    }
}
