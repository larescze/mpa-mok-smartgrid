package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.Fr;
import com.herumi.mcl.G1;
import com.herumi.mcl.G2;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Trader extends SecureChannel.EchoServer {
    private String name;
    private int port;
    private GroupManager groupManager;
    HashMap<UUID, Long> clientsConsumption;

    private final Thread thread;

    public Trader(String name, int port) throws Exception {
        super(port, name);
        this.name = name;
        this.port = port;
        this.groupManager = new GroupManager();
        this.clientsConsumption = new HashMap<>();

        thread = new Thread(this);
        thread.start();
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    @Override
    protected Object createResponse(Object dataReceived) {
        Object response = null;

        if (dataReceived instanceof String && dataReceived.equals("AgreeTariff")) {
            System.out.printf("[%s] Received AgreeTariff\n", name);
            response = getTwoPartyObject();
        } else if (dataReceived instanceof UserZKObject) {
            System.out.printf("[%s] Received UserZKObject\n", name);
            UserZKObject clientZK = (UserZKObject) dataReceived;
            G1 signKeyRand = agreeTariff(clientZK);
            System.out.printf("[%s] Sending SignKeyRandObject\n", name);
            response = signKeyRand.serialize();
        } else if (dataReceived instanceof Map) {
            System.out.printf("[%s] Received SignedData", name);
            Map<Long, SignatureProof> signedData = (Map<Long, SignatureProof>) dataReceived;
            long consumption = signedData.keySet().iterator().next();
            SignatureProof sp = signedData.get(consumption);
            System.out.printf("[%s] Checking signature\n", name);
            if (saveClientConsumption(sp, consumption)) {
                System.out.printf("[%s] Signature is valid\n", name);
                System.out.printf("[%s] Saving client consumption\n", name);
                response = new String("OK");
            } else {
                response = new String("Signature is not valid!");
            }
        }

        return response;
    }

    @Override
    public void close() {
        super.close();
        thread.interrupt();
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
        System.out.printf("[%s] clients:\n", name);

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

