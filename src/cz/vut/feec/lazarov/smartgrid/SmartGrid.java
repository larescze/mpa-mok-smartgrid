package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.Fr;
import com.herumi.mcl.G2;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.GroupSignatureFunctions;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.SignatureProof;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class SmartGrid {
    private String name;
    private long totalConsumption;

    public SmartGrid(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean aggregateData(SignatureProof sp, long consumption, G2 publicKey) {
        String m = consumption + "";
        BigInteger hashBig = new BigInteger(m.getBytes());
        Fr msg = new Fr(hashBig.toString(10));

        if (GroupSignatureFunctions.checkProof(sp, msg, publicKey)) {
            totalConsumption += consumption;
        }

        return false;
    }
}
