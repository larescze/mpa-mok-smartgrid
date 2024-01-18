package cz.vut.feec.datel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GeneratorSettings {
    private final static DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    LocalDate installationDate;
//    String seed;

    int initialConsumption;
    int initialGeneration;

    double consumptionFactor;
    double generationFactor;
    int generationHours;

    double reactiveLoadFactor;
    double capacityLoadFactor;

    int maxCurrent;

    boolean threePhases;

    int lowTariffHours;

    int meterID;

    public GeneratorSettings(LocalDate installationDate, int initialConsumption, int initialGeneration, double consumptionFactor, double generationFactor, int generationHours, double reactiveLoadFactor, double capacityLoadFactor, int maxCurrent, boolean threePhases, int lowTariffHours, int meterID) {
        this.installationDate = installationDate;
        this.initialConsumption = initialConsumption;
        this.initialGeneration = initialGeneration;
        this.consumptionFactor = consumptionFactor;
        this.generationFactor = generationFactor;
        this.generationHours = generationHours;
        this.reactiveLoadFactor = reactiveLoadFactor;
        this.capacityLoadFactor = capacityLoadFactor;
        this.maxCurrent = maxCurrent;
        this.threePhases = threePhases;
        this.lowTariffHours = lowTariffHours;
        this.meterID = meterID;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public int getInitialConsumption() {
        return initialConsumption;
    }

    public int getInitialGeneration() {
        return initialGeneration;
    }

    public double getConsumptionFactor() {
        return consumptionFactor;
    }

    public double getGenerationFactor() {
        return generationFactor;
    }

    public int getMaxCurrent() {
        return maxCurrent;
    }

    public boolean isThreePhases() {
        return threePhases;
    }

    public int getLowTariffHours() {
        return lowTariffHours;
    }

    public boolean isLowTariff() {
        return lowTariffHours > 0;
    }

    public double getReactiveLoadFactor() {
        return reactiveLoadFactor;
    }

    public double getCapacityLoadFactor() {
        return capacityLoadFactor;
    }

    public int getGenerationHours() {
        return generationHours;
    }

    public TariffStatus isTimeForNT(LocalDateTime current) {
        if (lowTariffHours == 0) {
            return TariffStatus.HIGH;
        }
        //od 0:15 do h + hodiny NT
        LocalTime midnight = LocalTime.of(0, 15);

        if ((current.toLocalTime().isAfter(midnight) || current.toLocalTime().equals(midnight)) && current.toLocalTime().isBefore(midnight.plusHours(lowTariffHours))) {

            if (current.toLocalTime().equals(midnight.plusHours(lowTariffHours).minusMinutes(15))) {
                return TariffStatus.LOW_LAST;
            }

            return TariffStatus.LOW;
        }

        return TariffStatus.HIGH;
    }

    public void setConsumptionFactor(double v) {
        consumptionFactor = v;
    }

    public String getMeterName() {
        return meterID + "";
    }
}
