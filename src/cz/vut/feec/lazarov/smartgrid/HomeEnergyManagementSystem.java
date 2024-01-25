package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.G1;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.Client;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.ServerTwoPartyObject;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.SignatureProof;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.UserZKObject;

import java.math.BigInteger;
import java.util.HashMap;

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

    public boolean agreeTariffWithTrader(int port) {
        try {
            this.client = new Client();

            SecureChannel.EchoClient sender = new SecureChannel.EchoClient("localhost", port, name);
            System.out.printf("[%s] Sending AgreeTariff\n", name);
            AgreeTariff agreeTariff = new AgreeTariff(client.getClientID(), name);
            ServerTwoPartyObject twoPartyObject = (ServerTwoPartyObject) sender.sendAndReceiveData(agreeTariff);
            System.out.printf("[%s] Received ServerTwoPartyObject\n", name);

            System.out.printf("[%s] Checking issuer\n", name);
            if (client.setUserZk(twoPartyObject)) {
                System.out.printf("[%s] Issuer is valid\n", name);
                System.out.printf("[%s] Sending SenderObject\n", name);
                sender.sendAndReceiveData(client.getUserZK());
                System.out.printf("[%s] Received SignKeyRandObject\n", name);
                Object signKeyRandObject = sender.sendAndReceiveData(client.getUserZK());
                G1 signKeyRand = new G1();
                signKeyRand.deserialize((byte[]) signKeyRandObject);
                System.out.printf("[%s] Computing key from manager\n", name);
                computeSignKey(signKeyRand);

                sender.close();
                return true;
            }

            System.out.printf("[%s] Issuer is not valid\n", name);
            sender.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void computeSignKey(G1 signKeyRand) {
        client.computeKeyFromManager(signKeyRand);
    }

    public void cancelTariff() {
        SignatureProof sp = client.signMessage("CancelTariff");
        client = null;
    }
}
