package cz.vut.feec.smartmetering;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

public class GenerateProfile {
    private static final DateTimeFormatter date = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static boolean WITHOUT_RANDOM = false;
    private static boolean PRINT_VALUES = false;

    public static void changeTestingValues(boolean withoutRandom, boolean printValues) {
        WITHOUT_RANDOM = withoutRandom;
        PRINT_VALUES = printValues;
    }

    public static void main(String[] args) {
        PRINT_VALUES = true;
        GeneratorSettings settings = new GeneratorSettings(
                LocalDateTime.of(2020, 1, 1, 0, 0).toLocalDate(),
                1000,
                0,
                3885,
                350,
                8,
                12.0 / 1000,
                36.0 / 1000,
                100,
                false,
                2,
                5);

        boolean allInOne = true;
        int dayStart = 0;
        int dayEnd = 15;

        LocalDateTime start = LocalDateTime.of(2020, 1, dayStart + 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, 1, dayEnd, 23, 45);

        if (allInOne) {
            getProfileData(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()), Date.from(end.atZone(ZoneId.systemDefault()).toInstant()), "1.0.99.1.0.255", settings);
        } else {
            double[] factor = {80, 220, 280, 350, 500, 600, 700, 800, 900, 1000, 1100, 2500, 3300, 8000};

            for (int i = dayStart; i <= dayEnd; i++) {
                settings.setConsumptionFactor(factor[i]);

                start = LocalDateTime.of(2020, 1, 5, 0, 0);
                end = LocalDateTime.of(2020, 1, 5, 0, 0);

                System.out.println("\n----" + factor[i] + "----");
                getProfileData(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()),
                        Date.from(end.atZone(ZoneId.systemDefault()).toInstant()), "1.0.99.1.0.255", settings);
            }
        }
    }

    public static Collection<? extends Object[]> getProfileData(Date start, Date end, String lnName, GeneratorSettings settings) {
        //Change time, crop to nearest 15 minutes
        LocalDateTime ldtStart = roundToNearest15Minutes(LocalDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault()), true);
        LocalDateTime ldtEnd = roundToNearest15Minutes(LocalDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault()), false);

        if (ldtEnd.isAfter(LocalDateTime.of(2024, 12, 31, 23, 45))) {
            ldtEnd = LocalDateTime.of(2024, 12, 31, 23, 45);
        }

        //If end is before installation date, return null
        if (ldtEnd.toLocalDate().isBefore(settings.installationDate)) {
            return null;
        }

        //If start is before installation date, crop to installation date
        if (ldtStart.toLocalDate().isBefore(settings.installationDate)) {
            ldtStart = settings.installationDate.atStartOfDay();
        }

        //LP1
        if (lnName.equals("1.0.99.1.0.255")) {
            return generateValues(ldtStart, ldtEnd, settings, true);
        }

        //Power quality
        if (lnName.equals("1.0.99.19.0.255")) {
            return generateValues(ldtStart, ldtEnd, settings, false);
        }

        return null;
    }


    private static Collection<? extends Object[]> generateValues(LocalDateTime start, LocalDateTime end, GeneratorSettings settings, boolean lp1) {
        ArrayList<Object[]> values = new ArrayList<>();
        Random random = new Random();

        long sumVT;
        long sumNT;
        long newConsumption;
        long previousSumConsumption;
        long newSumConsumption;
        long generation = 0;
        int lastRandom;

        LocalDateTime midnight = start.toLocalDate().atStartOfDay();
        int days = (int) ChronoUnit.DAYS.between(settings.installationDate, midnight);
        int entry = 96 * days;
        int genEntry = days * (4 * settings.getGenerationHours());

        //Preload sum values
        lastRandom = getRandomAdditionToConsumption(random, midnight.minusMinutes(15), settings);
        long beforeMidnightConsumption = getConsumptionAtPoint(entry - 1, settings);
        previousSumConsumption = beforeMidnightConsumption + lastRandom;

        //Proportion of values before midnight
        var split = splitMidnightConsumption(beforeMidnightConsumption, settings);
        sumNT = split[0];
        sumVT = split[1] + lastRandom;

        LocalDateTime currentTime = midnight;

        long lastGeneration = 0;

        while (currentTime.isBefore(end) || currentTime.isEqual(end)) {
            //Get consumption data
            lastRandom = getRandomAdditionToConsumption(random, currentTime, settings);
            newSumConsumption = getConsumptionAtPoint(entry, settings) + lastRandom;
            newConsumption = newSumConsumption - previousSumConsumption;

            switch (settings.isTimeForNT(currentTime)) {
                case HIGH:
                    sumVT += newConsumption;
                    break;
                case LOW_LAST:
                    newConsumption -= lastRandom;
                    newSumConsumption -= lastRandom;
                case LOW:
                    sumNT += newConsumption;
                    break;
            }

            boolean canGenerate = false;

            //Init generation point
            if (settings.getGenerationHours() > 0) {
                LocalTime genStart = LocalTime.of(7, 59);
                if (currentTime.toLocalTime().isAfter(genStart)
                        && currentTime.toLocalTime().isBefore(genStart.plusHours(settings.getGenerationHours()))) {
                    canGenerate = true;
                    genEntry++;
                }
            }

            //Use value only if it is between start and end
            if ((currentTime.isAfter(start) || currentTime.isEqual(start)) && (currentTime.isBefore(end) || currentTime.isEqual(end))) {
                if (lp1) {
                    int reactiveLoad = (int) ((0.0 + newSumConsumption) * settings.getReactiveLoadFactor());
                    int capacityLoad = (int) ((0.0 + newSumConsumption) * settings.getCapacityLoadFactor());

                    if (canGenerate) {
                        random.setSeed((settings.getMeterName() + (genEntry - 1)).hashCode());
                        generation = getGenerationAtPoint((genEntry - 1), settings) + (WITHOUT_RANDOM ? 0 : getRandom(random, 0, (int) Math.floor(settings.generationFactor)));
                    }

                    if (PRINT_VALUES) {
                        System.out.printf("%s;0;%d;%d;%d;%d;%d;%d (%d)\n", currentTime.format(date),
                                sumVT + sumNT, sumVT, sumNT, generation, reactiveLoad, capacityLoad, (generation - lastGeneration));
                    }

                    values.add(new Object[]{currentTime, 0, sumVT + sumNT, sumVT, sumNT, generation, reactiveLoad, capacityLoad});
                } else {
                    if (settings.isThreePhases()) {
                        //RNG
                        double pf1, pf2, pf3;
                        int u1, u2, u3;

                        int q1, q2, q3; //0,01
                        int i1, i2, i3;

                        int p1, p2, p3; //spotreba na fazich

                        pf1 = (0.0 + getRandom(random, 65, 99)) / 100;
                        pf2 = (0.0 + getRandom(random, 65, 99)) / 100;
                        pf3 = (0.0 + getRandom(random, 65, 99)) / 100;

                        double r1 = (0.0 + getRandom(random, 10, 40)) / 100;
                        double r2 = (0.0 + getRandom(random, 10, 40)) / 100;
                        double r3 = 1.0 - r1 - r2;

                        p1 = (int) (r1 * ((0.0 + newConsumption) /** 0.01*/));
                        p2 = (int) (r2 * ((0.0 + newConsumption) /** 0.01*/));
                        p3 = (int) (r3 * ((0.0 + newConsumption) /** 0.01*/));

                        u1 = getRandom(random, 2250, 2490);
                        u2 = getRandom(random, 2250, 2490);
                        u3 = getRandom(random, 2250, 2490);

                        i1 = (int) ((p1 / (((0.0 + u1) * 0.1) * pf1)) * 100);
                        i2 = (int) ((p2 / (((0.0 + u2) * 0.1) * pf2)) * 100);
                        i3 = (int) ((p3 / (((0.0 + u3) * 0.1) * pf3)) * 100);

                        if (PRINT_VALUES) {
                            System.out.printf("%s;0;%d;%d;%d;%d;%d;%d;%d;%d;%d\n", currentTime.format(date),
                                    u1, u2, u3,
                                    i1, i2, i3,
                                    (int) (pf1 * 100), (int) (pf2 * 100), (int) (pf3 * 100));
                        }

                        values.add(new Object[]{currentTime, u1, u2, u3, i1, i2, i3, (int) (pf1 * 100), (int) (pf2 * 100), (int) (pf3 * 100)});
                    } else {
                        double pf1;
                        int u1;
                        int i1;
                        double p1;

                        pf1 = (0.0 + getRandom(random, 65, 99)) / 100;
                        p1 = ((0.0 + newConsumption)); //* 0.01);
                        u1 = getRandom(random, 2250, 2490);
                        i1 = (int) ((p1 / (((0.0 + u1) * 0.1) * pf1)) * 100);

                        if (PRINT_VALUES) {
                            System.out.printf("%s;0;%d;%d;%d;%d\n", currentTime.format(date), u1, i1, (int) (pf1 * 100), newConsumption);
                        }

                        values.add(new Object[]{currentTime, u1, i1, (int) (pf1 * 100)});
                    }
                }
            }

            entry++;
            currentTime = currentTime.plusMinutes(15);
            previousSumConsumption = newSumConsumption;
            lastGeneration = generation;
        }

        return values;
    }

    private static long[] splitMidnightConsumption(long consumption, GeneratorSettings settings) {
        long[] split = new long[2];

        if (settings.getLowTariffHours() == 0) {
            split[0] = 0;
            split[1] = consumption;
            return split;
        }

        double factor = (0.0 + settings.getLowTariffHours()) / 24.0;
        double nt = (0.0 + consumption) * factor;

        split[0] = (long) Math.ceil(nt);
        split[1] = consumption - split[0];

        return split;
    }

    private static int getRandomAdditionToConsumption(Random random, LocalDateTime atTime, GeneratorSettings settings) {
        if (WITHOUT_RANDOM) {
            return 0;
        }

        random.setSeed((settings.getMeterName() + getTimeSeed(atTime)).hashCode());

        if (atTime.toLocalTime().equals(LocalTime.of(0, 0))) {
            return 0;
        }

        return getRandom(random, 0, (int) Math.floor(settings.consumptionFactor));
    }

    private static int getRandom(Random random, int min, int max) {
        return random.nextInt(max - min) + min;
    }

    private static void setSeed(Random random, String seed, LocalDateTime time) {
        random = new Random((seed + time.toString()).hashCode());
    }

    private static LocalDateTime roundToNearest15Minutes(LocalDateTime dateTime, boolean roundUp) {
        int minute = dateTime.getMinute();
        int remainder = minute % 15;

        LocalDateTime roundedDateTime;

        if (remainder > 0 && roundUp) {
            roundedDateTime = dateTime.plusMinutes(15 - remainder);
        } else {
            roundedDateTime = dateTime.minusMinutes(remainder);
        }

        return roundedDateTime.withSecond(0).withNano(0);
    }

    private static long getConsumptionAtPoint(int timePoint, GeneratorSettings settings) {
        return settings.initialConsumption + (long) (settings.consumptionFactor * timePoint);
    }

    private static long getGenerationAtPoint(int timePoint, GeneratorSettings settings) {
        return settings.initialGeneration + (long) (settings.generationFactor * timePoint);
    }

    private static String getTimeSeed(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
