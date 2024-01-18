package cz.vut.feec.lazarov.smartgrid;

import com.herumi.mcl.Fr;
import cz.vut.feec.smartmetering.ConsumptionGenerator;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.Client;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.GroupSignatureFunctions;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.SignatureProof;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
