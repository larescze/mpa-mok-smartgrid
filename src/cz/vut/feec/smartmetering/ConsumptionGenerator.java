package cz.vut.feec.smartmetering;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public class ConsumptionGenerator {
    static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    LocalDate installationDate;
    int initialConsumption;
    double consumptionFactor;
    String meterName;

    /**
     * @param installationDate   Installation date of the meter
     * @param initialConsumption Initial consumption of the meter at installation date (Wh)
     * @param consumptionFactor  Factor of consumption increase (linear function tilt) (Wh/15min)
     * @param meterName          Name of the meter
     */
    public ConsumptionGenerator(LocalDate installationDate, int initialConsumption, double consumptionFactor, String meterName) {
        this.installationDate = installationDate;
        this.initialConsumption = initialConsumption;
        this.consumptionFactor = consumptionFactor;
        this.meterName = meterName;
    }

    public ConsumptionGenerator(int serialNumber, LocalDate installationDate) {
        Random random = new Random(serialNumber);

        this.meterName = "SM " + serialNumber;
        this.installationDate = installationDate;
        this.initialConsumption = getRandom(random, 0, 1000);
        this.consumptionFactor = getRandom(random, 250, 8000);
    }

    /**
     * Generates consumption at given time
     *
     * @param time               Time to generate consumption at
     * @param currentConsumption If true, returns consumption at given time, otherwise returns cumulative consumption at given time (overall consumption)
     * @return Consumption at given time/overall consumption at given time
     */
    public long generateConsumptionAtTime(LocalDateTime time, boolean currentConsumption) {
        Random random = new Random();

        long newConsumption = 0;
        long previousSumConsumption;
        long newSumConsumption = 0;
        int lastRandom;

        LocalDateTime midnight = time.toLocalDate().atStartOfDay();
        int days = (int) ChronoUnit.DAYS.between(installationDate, midnight);
        int entry = 96 * days;

        // Preload sum values
        lastRandom = getRandomAdditionToConsumption(random, midnight.minusMinutes(15));
        long beforeMidnightConsumption = getConsumptionAtPoint(entry - 1);
        previousSumConsumption = beforeMidnightConsumption + lastRandom;

        LocalDateTime currentTime = midnight;

        while (currentTime.isBefore(time) || currentTime.isEqual(time)) {
            // Get consumption data
            lastRandom = getRandomAdditionToConsumption(random, currentTime);
            newSumConsumption = getConsumptionAtPoint(entry) + lastRandom;
            newConsumption = newSumConsumption - previousSumConsumption;
            entry++;
            currentTime = currentTime.plusMinutes(15);
            previousSumConsumption = newSumConsumption;
        }

        if (currentConsumption) {
            return newConsumption;
        }

        return newSumConsumption;
    }

    private static int getRandom(Random random, int min, int max) {
        return random.nextInt(max - min) + min;
    }

    private int getRandomAdditionToConsumption(Random random, LocalDateTime atTime) {
        random.setSeed((meterName + getTimeSeed(atTime)).hashCode());

        if (atTime.toLocalTime().equals(LocalTime.of(0, 0))) {
            return 0;
        }

        return getRandom(random, 0, (int) Math.floor(consumptionFactor));
    }

    private static String getTimeSeed(LocalDateTime time) {
        return time.format(DATETIME);
    }

    private long getConsumptionAtPoint(int timePoint) {
        return initialConsumption + (long) (consumptionFactor * timePoint);
    }
}
