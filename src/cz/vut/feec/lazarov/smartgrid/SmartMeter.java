package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.Fr;
import cz.vut.feec.smartmetering.ConsumptionGenerator;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.Client;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.GroupSignatureFunctions;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.SignatureProof;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class SmartMeter {
    private int serialNumber;
    private String manufacturer;
    ConsumptionGenerator generator;
    private Client client;

    public SmartMeter(int serialNumber, String manufacturer) {
        this.serialNumber = serialNumber;
        this.manufacturer = manufacturer;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setUp() {
        generator = new ConsumptionGenerator(serialNumber, LocalDate.now());
    }

    public long getConsumption() {
        return generator.generateConsumptionAtTime(LocalDateTime.now(), true);
    }

    public SignatureProof signConsumption(long consumption) {
        String m = consumption + "";
        BigInteger hashBig = new BigInteger(m.getBytes());
        Fr msg = new Fr(hashBig.toString(10));

        return GroupSignatureFunctions.computeGroupSignature(msg, client.getN(), client.getSignKey(), client.getUserKey(), client.getGroupID());
    }

    public boolean signAndSendConsumption(int port) {
        try {
            SecureChannel.EchoClient sender = new SecureChannel.EchoClient("localhost", port, "SM" + serialNumber);

            System.out.printf("[%s] Measure consumption\n", "SM" + serialNumber);
            long consumption = getConsumption();
            System.out.printf("[%s] Consumption: %d\n", "SM" + serialNumber, consumption);
            System.out.printf("[%s] Sign consumption\n", "SM" + serialNumber);
            SignatureProof sp = signConsumption(consumption);

            HashMap<Long, SignatureProof> signedData = new HashMap<>();
            signedData.put(consumption, sp);

            System.out.printf("[%s] Sending data to: %d\n", "SM" + serialNumber, port);
            sender.sendAndReceiveData(signedData);

            sender.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
