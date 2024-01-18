package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.Fr;
import com.herumi.mcl.G1;
import com.herumi.mcl.G2;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

public class Trader {
    private String name;
    private GroupManager groupManager;
    HashMap<UUID, Long> clientsConsumption;

    public Trader(String name) {
        this.name = name;
        this.groupManager = new GroupManager();
        this.clientsConsumption = new HashMap<>();
    }

    public G2 getPublicKey() {
        return groupManager.getManagerPublicKey();
    }

    public ServerTwoPartyObject getTwoPartyObject() {
        return groupManager.getTwoPartyObject();
    }

    public G1 agreeTariff(UserZKObject clientZK) {
        if (!groupManager.checkClientZK(clientZK)) {
            return null;
        }

        groupManager.saveClientKey(clientZK.getClientPubKey());

        return groupManager.computeSigningKeyRand(clientZK);
    }

    public void cancelTariff(UUID clientUUID) {
        groupManager.removeClientKey(clientUUID);
    }

    public boolean checkSignature(SignatureProof sp, String m) {
        BigInteger hashBig = new BigInteger(m.getBytes());
        Fr msg = new Fr(hashBig.toString(10));

        return GroupSignatureFunctions.checkProof(sp, msg, groupManager.getManagerPublicKey());
    }

    public void showClientsConsumption() {
        for (UUID clientUUID : clientsConsumption.keySet()) {
            System.out.println(clientUUID + ": " + clientsConsumption.get(clientUUID));
        }
    }

    public long getClientConsumption(UUID clientUUID) {
        return clientsConsumption.get(clientUUID);
    }

    public boolean saveClientConsumption(SignatureProof sp, long consumption) {
        String m = consumption + "";
        BigInteger hashBig = new BigInteger(m.getBytes());
        Fr msg = new Fr(hashBig.toString(10));

        if (!checkSignature(sp, m)) {
            return false;
        }

        UUID clientUUID = groupManager.getClientUUID(sp);
        clientsConsumption.put(clientUUID, consumption);

        return true;
    }
}
