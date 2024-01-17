package cz.vut.feec.xklaso00.groupsignature.cryptocore;

import com.herumi.mcl.Fr;
import com.herumi.mcl.G1;
import com.herumi.mcl.G2;
import com.herumi.mcl.Mcl;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.UUID;

public class GroupManager {
    private BigInteger n;
    private BigInteger managerID;
    private BigInteger managerPrivateECKey;
    private G2 managerPublicKey;
    PaillierKeyPair kp;
    ServerTwoPartyObject twoPartyObject;
    HashMap<UUID, G2> clientKeys;

    public GroupManager() {
        SecureRandom random = new SecureRandom();
        n = new BigInteger("2523648240000001BA344D8000000007FF9F800000000010A10000000000000D", 16);

        managerID = new BigInteger(32, random);
        managerPrivateECKey = new BigInteger(254, random);
        managerPrivateECKey = managerPrivateECKey.mod(n);
        managerPublicKey = new G2();

        kp = new PaillierKeyPair(4561);
        twoPartyObject = NIZKPKFunctions.computeE1andZKManager(kp, managerPrivateECKey, managerID);

        Fr managerKey = new Fr(managerPrivateECKey.toString(), 10);
        Mcl.mul(managerPublicKey, GroupSignatureFunctions.getG2(), managerKey);

        clientKeys = new HashMap<>();
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getManagerID() {
        return managerID;
    }

    public G2 getManagerPublicKey() {
        return managerPublicKey;
    }

    public ServerTwoPartyObject getTwoPartyObject() {
        return twoPartyObject;
    }

    public void saveClientKey(G2 clientPublicKey) {
        UUID uuid = UUID.randomUUID();
        G2 invPubKey = new G2();
        Mcl.neg(invPubKey, clientPublicKey);
        clientKeys.put(uuid, invPubKey);
    }

    public void removeClientKey(UUID uuid) {
        clientKeys.remove(uuid);
    }

    public UUID getClientUUID(SignatureProof sp) {
        for (UUID uuid : clientKeys.keySet()) {
            if (GroupSignatureFunctions.checkSignatureWithPK(clientKeys.get(uuid), sp.getSiAph(), sp.getSiDash()) == 0) {
                return uuid;
            }
        }

        return null;
    }

    public boolean checkClientZK(UserZKObject clientZK) {
        return NIZKPKFunctions.checkPKUser(clientZK.getZets(), clientZK.getE2(), clientZK.getC2Goth(), clientZK.geteClientHash(), clientZK.getClientPubKey(), twoPartyObject.getE1(), n, kp);
    }

    public G1 computeSigningKeyRand(UserZKObject userZK) {
        return NIZKPKFunctions.computeSigningKeyRandomized(userZK.getE2(), kp, n);
    }
}
