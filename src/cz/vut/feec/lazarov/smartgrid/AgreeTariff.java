package cz.vut.feec.lazarov.smartgrid;

import java.io.Serializable;
import java.math.BigInteger;

public class AgreeTariff implements Serializable {
    private BigInteger clientID;
    private String name;

    public AgreeTariff(BigInteger clientID, String name) {
        this.clientID = clientID;
        this.name = name;
    }

    public BigInteger getClientID() {
        return clientID;
    }

    public String getName() {
        return name;
    }
}
