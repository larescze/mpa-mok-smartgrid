# MPA-MOK - Enhancing Privacy in Smart Metering with Group Signatures

This repository contains the code for the project "Enhancing Privacy in Smart Metering with Group Signatures".

## Dependencies
- Java 11
- MCL library

## Packages
- com.herumi.mcl: MCL library
- cz.vut.feec.lazarov.smartgrid: Main package
- cz.vut.feec.smartmetering: Consumption generator
- cz.vut.feec.xklaso00.groupsignature.cryptocore: Group signatures

## Prerequisites

Main class should contain the following code to load the MCL library:
```
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
```

DLL for the MCL library can be found in libs directory.

## Example

```
Mcl.SystemInit(Mcl.BN254);

HomeEnergyManagementSystem hems1 = new HomeEnergyManagementSystem("HEMS1");

Trader t1 = new Trader("Trader1", 666);
ServerTwoPartyObject twoPartyObject = t1.getTwoPartyObject();

hems1.agreeTariff(twoPartyObject);
G1 signKeyRand = t1.agreeTariff(hems1.getUserZK());
hems1.computeSignKey(signKeyRand);

SmartMeter sm1 = new SmartMeter(1524, "Manufacturer1");
hems1.connectSmartMeter(sm1, hems1.getClient());

long consumption = sm1.getConsumption();
SignatureProof sp = sm1.signConsumption(consumption);

SmartGrid sg1 = new SmartGrid("SmartGrid1", 333);
sg1.aggregateData(sp, consumption, t1.getPublicKey());

t1.saveClientConsumption(sp, consumption);
t1.showClientsConsumption();

sg1.close();
t1.close();
```

Inside the PoC.java is an example for Czech Smart Grid system.
