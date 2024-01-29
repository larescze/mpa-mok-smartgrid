import com.herumi.mcl.Mcl;
import cz.vut.feec.lazarov.smartgrid.*;

public class PoC {
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

    public static void main(String[] args) throws Exception {
        Mcl.SystemInit(Mcl.BN254);

        Trader t1 = new Trader("E.ON", 10000);
        Trader t2 = new Trader("FONERGY", 10001);
        SmartGrid sg1 = new SmartGrid("EG.D", 20000);
        SmartGrid sg2 = new SmartGrid("ČEZ Distribuce", 20001);
        SmartGrid sg3 = new SmartGrid("PREdistribuce", 20002);

        sg1.addTrader(t1.getPublicKey(), t1.getPort());
        sg1.addTrader(t2.getPublicKey(), t2.getPort());
        sg2.addTrader(t1.getPublicKey(), t1.getPort());
        sg2.addTrader(t2.getPublicKey(), t2.getPort());
        sg3.addTrader(t1.getPublicKey(), t1.getPort());
        sg3.addTrader(t2.getPublicKey(), t2.getPort());

        HomeEnergyManagementSystem hems1 = new HomeEnergyManagementSystem("Preslova 301/29, 602 00 Brno");
        hems1.agreeTariffWithTrader(t1.getPort());
        SmartMeter sm1 = new SmartMeter(1524, "Itron");
        hems1.connectSmartMeter(sm1, hems1.getClient());
        sm1.signAndSendConsumption(sg1.getPort());

        HomeEnergyManagementSystem hems2 = new HomeEnergyManagementSystem("Sochorova 3214/44, 616 00 Brno");
        hems2.agreeTariffWithTrader(t2.getPort());
        SmartMeter sm2 = new SmartMeter(1525, "Itron");
        hems2.connectSmartMeter(sm2, hems2.getClient());
        sm2.signAndSendConsumption(sg1.getPort());

        HomeEnergyManagementSystem hems3 = new HomeEnergyManagementSystem("Raisova 889/3, 460 01 Liberec");
        hems3.agreeTariffWithTrader(t1.getPort());
        SmartMeter sm3 = new SmartMeter(3000, "Landis+Gyr");
        hems3.connectSmartMeter(sm3, hems3.getClient());
        sm3.signAndSendConsumption(sg2.getPort());

        HomeEnergyManagementSystem hems4 = new HomeEnergyManagementSystem("Boleslavova 1529/28, 140 00 Praha");
        hems4.agreeTariffWithTrader(t2.getPort());
        SmartMeter sm4 = new SmartMeter(3001, "Landis+Gyr");
        hems4.connectSmartMeter(sm4, hems4.getClient());
        sm4.signAndSendConsumption(sg3.getPort());

        t1.showClientsConsumption();
        t2.showClientsConsumption();

        sg1.close();
        sg2.close();
        sg3.close();
        t1.close();
        t2.close();
    }
}
