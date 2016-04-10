package edu.uiowa.engineering.iot_smoke;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import edu.uiowa.engineering.iot_smoke.data.AccountRecord;
import edu.uiowa.engineering.iot_smoke.data.AirQualityRecord;
import edu.uiowa.engineering.iot_smoke.data.DeviceRecord;

public class OfyService {

    static {
        ObjectifyService.register(DeviceRecord.class);
        ObjectifyService.register(AccountRecord.class);
        ObjectifyService.register(AirQualityRecord.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
