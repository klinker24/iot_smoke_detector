package edu.uiowa.engineering.iot_smoke;

import java.util.Comparator;

import edu.uiowa.engineering.iot_smoke.data.AirQualityRecord;

public class TimeCompare implements Comparator<AirQualityRecord> {
    @Override
    public int compare(AirQualityRecord o1, AirQualityRecord o2) {
        return ((Long) o2.getTime()).compareTo((Long) o1.getTime());
    }
}