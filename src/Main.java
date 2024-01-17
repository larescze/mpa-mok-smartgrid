//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import com.herumi.mcl.Fr;
import com.herumi.mcl.G1;
import com.herumi.mcl.Mcl;
import cz.vut.feec.lazarov.smartgrid.HomeEnergyManagementSystem;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.*;

import java.math.BigInteger;
import java.util.UUID;

public class Main {
    static {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            System.out.println("Running on Windows");
            System.loadLibrary("mcljava-x64");
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            System.out.println("Running on Linux");
            System.loadLibrary("mcljava");
        } else {
            System.loadLibrary("mcljava-x64");
        }
    }

    public static void main(String[] args) {
        Mcl.SystemInit(Mcl.BN254);

//        GroupManager groupManager = new GroupManager();
//        ServerTwoPartyObject twoPartyObject = groupManager.getTwoPartyObject();
//
//        HomeEnergyManagementSystem hems1 = new HomeEnergyManagementSystem("HEMS1");
//        hems1.setClient(twoPartyObject);
//        Client s1 = hems1.getClient();
//
//        boolean proof = groupManager.checkPKUser(s1.getUserZK());
//
//        groupManager.saveClientKey(s1.getUserZK().getClientPubKey());
//
//        G1 signKeyRand = groupManager.computeSigningKeyRand(s1.getUserZK());
//        s1.computeKeyFromManager(signKeyRand);
//
//        String m = "test";
//        BigInteger hashBig = new BigInteger(m.getBytes());
//        Fr msg = new Fr(hashBig.toString(10));
//
//        SignatureProof sp = GroupSignatureFunctions.computeGroupSignature(msg, s1.getN(), s1.getSignKey(), s1.getUserKey(), s1.getGroupID());
//
//        boolean checkSign = GroupSignatureFunctions.checkProof(sp, msg, groupManager.getManagerPublicKey());
//        System.out.println(checkSign);
//
//        UUID clietntUUID = groupManager.getClientUUID(sp);
//        System.out.println(clietntUUID);
    }
}