package cz.vut.feec.lazarov.smartgrid;

import cz.vut.feec.xklaso00.groupsignature.cryptocore.Client;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.ServerTwoPartyObject;

public class HomeEnergyManagementSystem {
    private String name;
    private Client client;

    public HomeEnergyManagementSystem(String name) {
        this.name = name;
    }

    public Client getClient() {
        return client;
    }

    public void agreeTariff(ServerTwoPartyObject twoPartyObject) {
        this.client = new Client(twoPartyObject);
    }
}
