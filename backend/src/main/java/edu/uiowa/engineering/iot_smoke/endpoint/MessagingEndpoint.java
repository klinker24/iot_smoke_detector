/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package edu.uiowa.engineering.iot_smoke.endpoint;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Named;

import edu.uiowa.engineering.iot_smoke.data.AccountRecord;
import edu.uiowa.engineering.iot_smoke.data.DeviceRecord;

import static edu.uiowa.engineering.iot_smoke.OfyService.ofy;

@Api(
  name = "messaging",
  version = "v1",
  namespace = @ApiNamespace(
    ownerDomain = "iot_smoke.engineering.uiowa.edu",
    ownerName = "iot_smoke.engineering.uiowa.edu",
    packagePath=""
  )
)
public class MessagingEndpoint {

    private static final Logger log = Logger.getLogger(MessagingEndpoint.class.getName());
    private static final String API_KEY = System.getProperty("gcm.api.key");

    public void sendMessage(@Named("message") String message) throws IOException {
        if(message == null || message.trim().length() == 0) {
            log.warning("Not sending message because it is empty");
            return;
        }

        Sender sender = new Sender(API_KEY);
        Message gcmMessage = new Message.Builder().addData("message", message).build();
        List<AccountRecord> accounts = ofy().load().type(AccountRecord.class).limit(10).list();

        for (AccountRecord account : accounts) {
            for (DeviceRecord record : account.getDeviceList()) {
                Result result = sender.send(gcmMessage, record.getRegId(), 5);
                if (result.getMessageId() != null) {
                    log.info("Message sent to " + record.getRegId());
                } else {
                    log.warning("Error when sending message : " + result.getErrorCodeName());
                }
            }
        }
    }
}
