package cz.vut.feec.xklaso00.groupsignature.cryptocore;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class PaillierKeyPair {
    private PaillierPrivateKey paillierPrivateKey;
    private PaillierPublicKey paillierPublicKey;
    private BigInteger q;
    private BigInteger p;
    private BigInteger Mu;
    private String TAG = "TimeStampsPaillier";

    public PaillierKeyPair(int bitSize) {
        Random rng = new SecureRandom();
        long st = System.nanoTime();
        p = NIZKPKFunctions.generateRandomPrime(bitSize / 2);
        q = NIZKPKFunctions.generateRandomPrime(bitSize / 2);
        long et = System.nanoTime();

        BigInteger n = p.multiply(q);
        BigInteger nn = n.pow(2);

        BigInteger lambda = this.lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        BigInteger g = generateG(bitSize, n, phi, nn);

        //Goth
        BigInteger pGoth = NIZKPKFunctions.generateRandomPrime(bitSize / 2);
        BigInteger qGoth = NIZKPKFunctions.generateRandomPrime(bitSize / 2);
        BigInteger nGoth = pGoth.multiply(qGoth);

        pGoth = pGoth.subtract(new BigInteger("1"));
        qGoth = qGoth.subtract(new BigInteger("1"));
        BigInteger phiNGoth = pGoth.multiply(qGoth);

        BigInteger hGoth = NIZKPKFunctions.getRandom(bitSize, nGoth);
        BigInteger randGoth = NIZKPKFunctions.getRandom(phiNGoth.bitLength(), phiNGoth);
        BigInteger gGoth = hGoth.modPow(randGoth, nGoth);

        paillierPublicKey = new PaillierPublicKey(n, g, nn, bitSize, nGoth, hGoth, gGoth);
        paillierPrivateKey = new PaillierPrivateKey(lambda, Mu, n, nn, phi);
    }

    //The setup function pretty much, generates the parameters, this one takes a generated goth group
    public PaillierKeyPair(int bitSize, GothGroup gothGroup) {
        long st = System.nanoTime();
        p = NIZKPKFunctions.generateRandomPrime(bitSize / 2);
        q = NIZKPKFunctions.generateRandomPrime(bitSize / 2);
        long et = System.nanoTime();
        System.out.println("Prime generation took " + (et - st) / 1000000 + " ms");
        BigInteger n = p.multiply(q);
        BigInteger nn = n.pow(2);
        BigInteger lambda = this.lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));

        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        long st2 = System.nanoTime();
        BigInteger g = generateG(bitSize, n, phi, nn);
        et = System.nanoTime();
        System.out.println("G generation took " + (et - st2) / 1000000 + " ms");

        paillierPublicKey = new PaillierPublicKey(n, g, nn, bitSize, gothGroup.getnGoth(), gothGroup.gethGoth(), gothGroup.getgGoth());
        paillierPrivateKey = new PaillierPrivateKey(lambda, Mu, n, nn, phi);

        et = System.nanoTime();
    }

    private BigInteger lcm(BigInteger a, BigInteger b) {
        if (a.signum() == 0 || b.signum() == 0) {
            return BigInteger.ZERO;
        }

        return a.divide(a.gcd(b)).multiply(b).abs();
    }

    private BigInteger generateG(int bitSize, BigInteger n, BigInteger lambda, BigInteger nn) {
        BigInteger generator;
        BigInteger comp;
        Random rng = new SecureRandom();
        long st = System.nanoTime();

        do {
            generator = new BigInteger(bitSize, rng);
            generator = NIZKPKFunctions.myModPow(generator, n, nn);
            comp = NIZKPKFunctions.myModPow(generator, lambda, nn);
        } while (!comp.gcd(n).equals(BigInteger.ONE));

        long et = System.nanoTime();

        st = System.nanoTime();
        this.Mu = comp.modInverse(n);
        et = System.nanoTime();

        return generator;
    }

    public PaillierPrivateKey getPaillierPrivateKey() {
        return paillierPrivateKey;
    }

    public native String prime(String beforeNumber);

    public PaillierPublicKey getPaillierPublicKey() {
        return paillierPublicKey;
    }
}
