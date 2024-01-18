package cz.vut.feec.lazarov.smartgrid;

public class SmartMeter {
    private int serialNumber;
    private String manufacturer;

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
}
