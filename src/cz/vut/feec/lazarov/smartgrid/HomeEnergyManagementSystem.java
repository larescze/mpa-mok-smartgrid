package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.Fr;

import com.herumi.mcl.G1;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.Client;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.ServerTwoPartyObject;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.SignatureProof;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.UserZKObject;

import java.math.BigInteger;

public class HomeEnergyManagementSystem {
    private String name;
    private Client client;
    private SmartMeter smartMeter;

    public HomeEnergyManagementSystem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public SmartMeter getSmartMeter() {
        return smartMeter;
    }

    public UserZKObject getUserZK() {
        return client.getUserZK();
    }

    public void connectSmartMeter(SmartMeter smartMeter, Client client) {
        this.smartMeter = smartMeter;
        smartMeter.setClient(client);
        smartMeter.setUp();
    }

    public boolean agreeTariff(ServerTwoPartyObject twoPartyObject) {
        this.client = new Client();

        if (client.setUserZk(twoPartyObject)) {
            return true;
        }

        return false;
    }

    public void setSignKey(G1 signKeyRand) {
        client.computeKeyFromManager(signKeyRand);
    }

    public void cancelTariff() {
        SignatureProof sp = client.signMessage("test");
        client = null;
    }
}
