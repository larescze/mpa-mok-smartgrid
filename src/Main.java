//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import com.herumi.mcl.Fr;
import com.herumi.mcl.G1;
import com.herumi.mcl.Mcl;
import cz.vut.feec.lazarov.smartgrid.SmartGrid;
import cz.vut.feec.lazarov.smartgrid.HomeEnergyManagementSystem;
import cz.vut.feec.lazarov.smartgrid.SmartMeter;
import cz.vut.feec.lazarov.smartgrid.Trader;
import cz.vut.feec.xklaso00.groupsignature.cryptocore.*;

import java.math.BigInteger;

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

        HomeEnergyManagementSystem hems1 = new HomeEnergyManagementSystem("HEMS1");

        Trader t1 = new Trader("Trader1");
        ServerTwoPartyObject twoPartyObject = t1.getTwoPartyObject();

        hems1.agreeTariff(twoPartyObject);
        G1 signKey = t1.agreeTariff(hems1.getUserZK());
        hems1.setSignKey(signKey);

        SmartMeter sm1 = new SmartMeter(1524, "manufacturer1");
        hems1.connectSmartMeter(sm1, hems1.getClient());

        long consumption = sm1.getConsumption();
        SignatureProof sp = sm1.signConsumption(consumption);

        SmartGrid sg1 = new SmartGrid("SmartGrid1");
        sg1.aggregateData(sp, consumption, t1.getPublicKey());

        t1.saveClientConsumption(sp, consumption);
        t1.showClientsConsumption();
    }
}