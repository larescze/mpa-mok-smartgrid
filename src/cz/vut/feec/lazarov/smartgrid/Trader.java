package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.G1;
import com.herumi.mcl.G2;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.*;

import java.util.HashMap;
import java.util.UUID;

public class Trader {
    private String name;
    private GroupManager groupManager;
    HashMap<UUID, String> clientsConsumption;

    public Trader(String name) {
        this.name = name;
        this.groupManager = new GroupManager();
        this.clientsConsumption = new HashMap<>();
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

    public void showClientsConsumption() {
        for (UUID clientUUID : clientsConsumption.keySet()) {
            System.out.println(clientUUID + ": " + clientsConsumption.get(clientUUID));
        }
    }

    public String getClientConsumption(UUID clientUUID) {
        return clientsConsumption.get(clientUUID);
    }

    public void saveClientConsumption(SignatureProof sp, String msg) {
        UUID clientUUID = groupManager.getClientUUID(sp);
        clientsConsumption.put(clientUUID, msg);
    }
}
