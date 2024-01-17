package cz.vut.feec.xklaso00.groupsignature.cryptocore;

import com.herumi.mcl.Fr;
import com.herumi.mcl.G1;
import com.herumi.mcl.G2;
import com.herumi.mcl.Mcl;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Client {
    private BigInteger n;
    private BigInteger clientID;
    private BigInteger groupID;
    private BigInteger r1;
    private BigInteger clientPrivateKey;
    private G2 clientPublicKey;
    private Fr userKey;
    private UserZKObject userZK;
    private G1 signKey;

    public Client(ServerTwoPartyObject tpo) {
        SecureRandom random = new SecureRandom();
        n = new BigInteger("2523648240000001BA344D8000000007FF9F800000000010A10000000000000D", 16);
        clientID = new BigInteger(16, random);
        groupID = tpo.getGroupID();

        clientPrivateKey = new BigInteger(254, random);
        clientPrivateKey = clientPrivateKey.mod(GroupSignatureFunctions.genNinBigInt());
        clientPublicKey = new G2();
        userKey = new Fr(clientPrivateKey.toString(), 10);
        Mcl.mul(clientPublicKey, WeakBB.getG2(), userKey);

        r1 = NIZKPKFunctions.getRandom(n.bitLength(), n);

        boolean isIssuerZKValid = NIZKPKFunctions.checkIssuerZK(tpo.getPaillierPublicKey(), tpo.getZKs(), tpo.getE1(), tpo.getcGoth(), tpo.geteHash());
        userZK = NIZKPKFunctions.computeE2AndUserZK(r1, n, clientPrivateKey, tpo.getPaillierPublicKey(), tpo.getE1(), clientPublicKey.serialize(), clientID);
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getGroupID() {
        return groupID;
    }

    public Fr getUserKey() {
        return userKey;
    }

    public UserZKObject getUserZK() {
        return userZK;
    }

    public void computeKeyFromManager(G1 managerPublicKey) {
        this.signKey = NIZKPKFunctions.computeKeyFromManager(managerPublicKey, r1);
    }

    public G1 getSignKey() {
        return signKey;
    }
}
