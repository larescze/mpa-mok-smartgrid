package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.Fr;

import cz.vut.feec.xklaso00.groupsignature.cryptocore.Client;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.GroupSignatureFunctions;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.ServerTwoPartyObject;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.SignatureProof;

import java.math.BigInteger;

public class HomeEnergyManagementSystem {
    private String name;
    private Client client;

    public HomeEnergyManagementSystem(String name) {
        this.name = name;
    }

    public Client getClient() {
        return client;
    }

    public boolean agreeTariff(ServerTwoPartyObject twoPartyObject) {
        this.client = new Client();

        if (client.setUserZk(twoPartyObject)) {
            return true;
        }

        return false;
    }

    public void cancelTariff() {
        SignatureProof sp = client.signMessage("test");
        client = null;
    }
}
