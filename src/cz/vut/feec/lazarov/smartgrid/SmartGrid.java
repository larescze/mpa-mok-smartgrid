package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.Fr;
import com.herumi.mcl.G2;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.GroupSignatureFunctions;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.SignatureProof;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SmartGrid extends SecureChannel.EchoServer {
    private String name;
    private int port;
    private HashMap<G2, Integer> traders;
    private long totalConsumption;
    private final Thread thread;

    public SmartGrid(String name, int port) throws Exception {
        super(port, name);
        this.name = name;
        this.port = port;
        this.traders = new HashMap<>();

        thread = new Thread(this);
        thread.start();
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public long getTotalConsumption() {
        return totalConsumption;
    }

    public void addTrader(G2 publicKey, int port) {
        traders.put(publicKey, port);
    }

    public boolean aggregateData(SignatureProof sp, long consumption, G2 publicKey) {
        String m = consumption + "";
        BigInteger hashBig = new BigInteger(m.getBytes());
        Fr msg = new Fr(hashBig.toString(10));

        if (GroupSignatureFunctions.checkProof(sp, msg, publicKey)) {
            totalConsumption += consumption;
            return true;
        }

        return false;
    }

    @Override
    protected Object createResponse(Object dataReceived) {
        Object response = null;

        try {
            if (dataReceived instanceof Map) {
                System.out.printf("[%s] Received SignedData\n", name);
                Map<Long, SignatureProof> signedData = (Map<Long, SignatureProof>) dataReceived;
                long consumption = signedData.keySet().iterator().next();
                SignatureProof sp = signedData.get(consumption);

                System.out.printf("[%s] Checking signature\n", name);
                for (G2 publicKey : traders.keySet()) {
                    if (aggregateData(sp, consumption, publicKey)) {
                        System.out.printf("[%s] Consumption: " + consumption + "\n", name);
                        System.out.printf("[%s] Total consumption: " + totalConsumption + "\n", name);
                        int port = traders.get(publicKey);
                        System.out.printf("[%s] Sending data to: " + port + "\n", name);
                        response = new String("OK");

                        SecureChannel.EchoClient sender = new SecureChannel.EchoClient("localhost", port, name);
                        Object received = sender.sendAndReceiveData(signedData);
                        sender.close();
                    }
                }

                System.out.printf("[%s] Signature is not valid\n", name);
                return new String("Invalid signature!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
}
