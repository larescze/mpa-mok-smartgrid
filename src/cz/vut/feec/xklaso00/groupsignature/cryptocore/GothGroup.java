package cz.vut.feec.xklaso00.groupsignature.cryptocore;

import cz.vut.feec.xklaso00.groupsignature.cryptocore.NIZKPKFunctions;

import java.io.Serializable;
import java.math.BigInteger;
//a class that holds goth parameters, as they should not be generated by the manager
//that is why we will generate these before and just load them each time
public class GothGroup implements Serializable {
    private BigInteger nGoth;
    private BigInteger gGoth;
    private BigInteger hGoth;


    public GothGroup(BigInteger nGoth, BigInteger gGoth, BigInteger hGoth) {
        this.nGoth = nGoth;
        this.gGoth = gGoth;
        this.hGoth = hGoth;
    }
    //constructor to create new Goth parameters
    public GothGroup(int bitSize) {
        BigInteger pGoth= NIZKPKFunctions.generateRandomPrime(bitSize/2);
        BigInteger qGoth= NIZKPKFunctions.generateRandomPrime(bitSize/2);
        nGoth=pGoth.multiply(qGoth);

        pGoth=pGoth.subtract(new BigInteger("1"));
        qGoth=qGoth.subtract(new BigInteger("1"));
        BigInteger phiNGoth=pGoth.multiply(qGoth);

        hGoth= NIZKPKFunctions.getRandom(bitSize,nGoth);
        BigInteger randGoth= NIZKPKFunctions.getRandom(phiNGoth.bitLength(),phiNGoth);
        gGoth=hGoth.modPow(randGoth,nGoth);
    }

    public BigInteger getnGoth() {
        return nGoth;
    }

    public BigInteger getgGoth() {
        return gGoth;
    }

    public BigInteger gethGoth() {
        return hGoth;
    }
}
