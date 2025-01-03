package cz.vut.feec.xklaso00.groupsignature.cryptocore;

import com.herumi.mcl.*;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Random;

public class GroupSignatureFunctions {
    public static SignatureProof computeGroupSignature(Fr msg, BigInteger n, G1 signKey, Fr UserKey, BigInteger groupID) {
        Fr rand = getRandomFr(n);
        G1 GtoR = getG1();
        Mcl.mul(GtoR, GtoR, rand);
        G1 SiAph = new G1();
        Mcl.mul(SiAph, signKey, rand);

        Fr minusKey = new Fr();
        Mcl.mul(minusKey, UserKey, new Fr(-1));

        G1 SiDash = new G1();
        Mcl.mul(SiDash, SiAph, minusKey);

        //computation of t
        Fr randR = getRandomFr(n);
        Fr randSki = getRandomFr(n);
        G1 t = new G1();
        Mcl.mul(t, SiAph, randSki);
        G1 g1toRandR = new G1();
        Mcl.mul(g1toRandR, getG1(), randR);
        Mcl.add(t, t, g1toRandR);
        //end of computation of t

        Fr Sr = new Fr();
        Fr e = createEHash(msg, GtoR, SiAph, SiDash, t, n);

        Fr er = new Fr();
        Mcl.mul(er, e, rand);
        Mcl.sub(Sr, randR, er); //here we compute Sr
        Fr SSki = new Fr();
        Fr eSki = new Fr();
        Mcl.mul(eSki, e, UserKey);
        Mcl.add(SSki, randSki, eSki); //here we compute SSki
        //end of 2 proofs computation
        return new SignatureProof(GtoR, SiAph, SiDash, e, Sr, SSki, groupID);
    }

    //Function that hashes t and others in the sig proof/check, returns a sha256 hash mod N of the curve
    public static Fr createEHash(Fr msg, G1 GtoR, G1 SiAph, G1 SiDash, G1 t, BigInteger n) {
        MessageDigest hashing;
        try {
            hashing = MessageDigest.getInstance("SHA-256");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(GtoR.serialize());
            outputStream.write(SiAph.serialize());
            outputStream.write(SiDash.serialize());
            outputStream.write(t.serialize());
            outputStream.write(msg.serialize());

            byte[] chained = outputStream.toByteArray();
            hashing.update(chained);
            byte[] hash = hashing.digest();
            BigInteger hashBig = new BigInteger(hash);
            hashBig = hashBig.mod(n);
            Fr hashFrCut = new Fr(hashBig.toString(10));

            return hashFrCut;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    //Function that generates random number in modulus of the curve and returns it as MCLs FR
    public static Fr getRandomFr(BigInteger n) {
        Fr fr = new Fr();
        BigInteger rand;

        do {
            rand = new BigInteger(254, new Random());
        } while (rand.compareTo(n) >= 0);
        fr.setStr(rand.toString(), 10);

        return fr;
    }

    //Check functions for pc, returns true for valid sig, false for not valid sig, does not check revocation, that is to be implemented separately with the use of the checkSignatureWithPK
    public static boolean checkProof(SignatureProof sp, Fr msg, G2 groupPublicKey) {
        //checking of pairing
        long proofStart = System.nanoTime();
        GT pair1 = new GT();
        G1 SiG = new G1();
        Mcl.add(SiG, sp.getSiDash(), sp.getGToR());
        Mcl.pairing(pair1, SiG, getG2());
        GT pair2 = new GT();
        Mcl.pairing(pair2, sp.getSiAph(), groupPublicKey);


        if (!pair1.equals(pair2)) {
            return false;
        }

        G1 t2 = new G1();
        G1 add1 = new G1();
        Mcl.add(add1, sp.getSiDash(), sp.getGToR());
        Mcl.mul(add1, add1, sp.getE());
        //now we have Si*g' to e
        G1 SiToSSki = new G1();
        Mcl.mul(SiToSSki, sp.getSiAph(), sp.getSSki());
        G1 gToSr = new G1();
        Mcl.mul(gToSr, getG1(), sp.getSr());

        Mcl.add(t2, add1, SiToSSki);
        Mcl.add(t2, t2, gToSr);
        Fr e2 = createEHash(msg, sp.getGToR(), sp.getSiAph(), sp.getSiDash(), t2, genNinBigInt());

        if (sp.getE().equals(e2)) {
            return true;
        }

        return false;
    }

    //Function to check revocation and opening, it checks the pairing used in it and returns 0 if the pairings equal and -1 if not
    public static int checkSignatureWithPK(G2 PKiInv, G1 SiAph, G1 SiDash) {
        GT pair1 = new GT();
        GT pair2 = new GT();
        Mcl.pairing(pair1, SiAph, PKiInv);
        Mcl.pairing(pair2, SiDash, getG2());

        if (pair1.equals(pair2)) {
            return 0;
        }

        return -1;
    }

    //Function to get the order of the curve in bigInt
    public static BigInteger genNinBigInt() {
        return new BigInteger("2523648240000001BA344D8000000007FF9F800000000010A10000000000000D", 16);
    }

    //Function that returns the G2 generator of the curve, as the MCL does not have function for that
    public static G2 getG2() {
        Fp fp1 = new Fp("061a10bb519eb62feb8d8c7e8c61edb6a4648bbb4898bf0d91ee4224c803fb2b", 16);
        Fp fp2 = new Fp("0516aaf9ba737833310aa78c5982aa5b1f4d746bae3784b70d8c34c1e7d54cf3", 16);
        Fp fp3 = new Fp("021897a06baf93439a90e096698c822329bd0ae6bdbe09bd19f0e07891cd2b9a", 16);
        Fp fp4 = new Fp("0ebb2b0e7c8b15268f6d4456f5f38d37b09006ffd739c9578a2d1aec6b3ace9b", 16);
        G2 gen2 = new G2(fp1, fp2, fp3, fp4);

        return gen2;
    }

    //Function that returns the G1 generator of the curve
    public static G1 getG1() {
        Fp fr1 = new Fp("2523648240000001BA344D80000000086121000000000013A700000000000012", 16);
        Fp fr2 = new Fp("1", 16);
        G1 generator = new G1(fr1, fr2);

        return generator;
    }

}
