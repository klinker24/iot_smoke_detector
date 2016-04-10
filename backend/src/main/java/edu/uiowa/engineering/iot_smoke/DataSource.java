package edu.uiowa.engineering.iot_smoke;

import edu.uiowa.engineering.iot_smoke.data.AccountRecord;
import edu.uiowa.engineering.iot_smoke.data.DeviceRecord;

import static edu.uiowa.engineering.iot_smoke.OfyService.ofy;

public class DataSource {

    public static AccountRecord findAccountByEmail(String email) {
        return ofy().load().type(AccountRecord.class).filter("email", email).first().now();
    }

    public static AccountRecord findAccountByAuthToken(String authToken) {
        return ofy().load().type(AccountRecord.class).filter("authToken", authToken).first().now();
    }

    public static DeviceRecord findDevice(AccountRecord account, String regId) {
        if (account != null) {
            for (DeviceRecord device : account.getDeviceList()) {
                if (device.getRegId().equals(regId)) {
                    return device;
                }
            }
        }

        return null;
    }
}
