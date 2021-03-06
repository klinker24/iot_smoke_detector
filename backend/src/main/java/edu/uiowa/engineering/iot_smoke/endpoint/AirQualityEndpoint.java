/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package edu.uiowa.engineering.iot_smoke.endpoint;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.oauth.OAuthRequestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Named;

import edu.uiowa.engineering.iot_smoke.TimeCompare;
import edu.uiowa.engineering.iot_smoke.Constants;
import edu.uiowa.engineering.iot_smoke.DataSource;
import edu.uiowa.engineering.iot_smoke.data.AccountRecord;
import edu.uiowa.engineering.iot_smoke.data.AirQualityRecord;
import edu.uiowa.engineering.iot_smoke.data.DeviceRecord;

import static edu.uiowa.engineering.iot_smoke.OfyService.ofy;

@Api(
  name = "airQuality",
  version = "v1",
  namespace = @ApiNamespace(
    ownerDomain = "iot_smoke.engineering.uiowa.edu",
    ownerName = "iot_smoke.engineering.uiowa.edu",
    packagePath=""
  )
)
public class AirQualityEndpoint {

    private static final Logger log = Logger.getLogger(AirQualityEndpoint.class.getName());
    private static final String API_KEY = System.getProperty("gcm.api.key");

    @ApiMethod(name = "insertReading")
    public CollectionResponse<AirQualityRecord> insertReading(
            @Named("temperature") float temperature, @Named("relativeHumidity") float relativeHumidity,
            @Named("particleDensity") float particleDensity, @Named("authToken") String authToken)
            throws IOException, RuntimeException {

        AccountRecord account = DataSource.findAccountByAuthToken(authToken);

        if (account == null) {
            throw new RuntimeException("Invalid Auth Token.");
        }

        temperature = (float) (temperature * 1.8 + 32);

        if (temperature != 75.2) {
            AirQualityRecord record = new AirQualityRecord();
            record.setAccount(account.getId());
            record.setTemperature(temperature);
            record.setRelativeHumidity(relativeHumidity);
            record.setParticleDensity(particleDensity);
            record.setTime(System.nanoTime());

            if (temperature > 100) {
                notifyDevices(account, "Temperature has reached an abnormal level (" + temperature + "°F)");
            }

            if (particleDensity > 25) {
                notifyDevices(account, "Based on our analysis, particle density has reached a harmful level!");
            }

            ofy().save().entity(record).now();

            return CollectionResponse
                    .<AirQualityRecord>builder()
                    .setItems(Arrays.asList(record))
                    .build();
        } else {
            return CollectionResponse
                    .<AirQualityRecord>builder()
                    .setItems(new ArrayList<AirQualityRecord>())
                    .build();
        }
    }

    private void notifyDevices(AccountRecord account, String message) throws IOException {
        Sender sender = new Sender(API_KEY);
        Message gcmMessage = new Message.Builder().addData("message", message).build();

        for (DeviceRecord record : account.getDeviceList()) {
            Result result = sender.send(gcmMessage, record.getRegId(), 5);
            if (result.getMessageId() != null) {
                log.info("Message sent to " + record.getRegId());
            } else {
                log.warning("Error when sending message : " + result.getErrorCodeName());
            }
        }
    }

    @ApiMethod(
            name = "listReadings",
            scopes = {Constants.EMAIL_SCOPE},
            clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
            audiences = {Constants.ANDROID_AUDIENCE}
    )
    public CollectionResponse<AirQualityRecord> listReadings(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }

        AccountRecord account = DataSource.findAccountByEmail(user.getEmail());

        List<AirQualityRecord> records = ofy().load()
                .type(AirQualityRecord.class)
                .filter("account", account.getId())
                .list();

        Collections.sort(records, new TimeCompare());
        return CollectionResponse.<AirQualityRecord>builder().setItems(records).build();
    }

    @ApiMethod(
            name = "deleteAllReadings",
            scopes = {Constants.EMAIL_SCOPE},
            clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
            audiences = {Constants.ANDROID_AUDIENCE}
    )
    public void deleteAllReadings(User user) throws OAuthRequestException {
        if (user == null || !user.getEmail().equals("lklinker1@gmail.com")) {
            throw new OAuthRequestException("User not authorized.");
        }

        List<AirQualityRecord> records = ofy().load().type(AirQualityRecord.class).list();
        ofy().delete().entities(records).now();
    }
}
