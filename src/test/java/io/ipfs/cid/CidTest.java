package io.ipfs.cid;

import org.junit.*;
import io.ipfs.multibase.*;

import java.io.*;
import java.util.*;

public class CidTest {

    @Test
    public void stringTest() throws IOException {
        List<String> examples = Arrays.asList(
                "QmPZ9gcCEpqKTo6aq61g2nXGUhM4iCL3ewB6LDXZCtioEB",
                "QmatmE9msSfkKxoffpHwNLNKgwZG8eT9Bud6YoPab52vpy",
                "zdpuAyvkgEDQm9TenwGkd5eNaosSxjgEYd8QatfPetgB1CdEZ"
        );
        for (String example: examples) {
            Cid cid = Cid.decode(example);
            String encoded = cid.toString();
            if (!encoded.equals(example))
                throw new IllegalStateException("Incorrect cid string! " + example + " => " + encoded);
        }
    }
}
