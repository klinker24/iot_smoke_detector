/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package edu.uiowa.engineering.iot_smoke.endpoint;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.oauth.OAuthRequestException;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.inject.Named;

import edu.uiowa.engineering.iot_smoke.Constants;
import edu.uiowa.engineering.iot_smoke.DataSource;
import edu.uiowa.engineering.iot_smoke.Util;
import edu.uiowa.engineering.iot_smoke.data.AccountRecord;
import edu.uiowa.engineering.iot_smoke.data.DeviceRecord;

import static edu.uiowa.engineering.iot_smoke.OfyService.ofy;

@Api(
  name = "registration",
  version = "v1",
  scopes = {Constants.EMAIL_SCOPE},
  clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
  audiences = {Constants.ANDROID_AUDIENCE},
  namespace = @ApiNamespace(
    ownerDomain = "iot_smoke.engineering.uiowa.edu",
    ownerName = "iot_smoke.engineering.uiowa.edu",
    packagePath=""
  )
)
public class RegistrationEndpoint {

    private static final Logger log = Logger.getLogger(RegistrationEndpoint.class.getName());

    @ApiMethod(name = "register")
    public CollectionResponse<AccountRecord> registerDevice(@Named("deviceType") String deviceType, @Named("regId") String regId, User user)
            throws OAuthRequestException {

        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }

        AccountRecord account = DataSource.findAccountByEmail(user.getEmail());
        DeviceRecord device = DataSource.findDevice(account, regId);

        if (account == null) {
            // there is no account for this user yet, so we need to set one up.
            account = new AccountRecord();
            account.setEmail(user.getEmail());
            account.setAuthToken(Util.generateAuthToken(user.getEmail()));
        }

        if (device == null) {
            device = new DeviceRecord();
            device.setDeviceType(deviceType);
            device.setRegId(regId);

            account.addDevice(device);
        }

        ofy().save().entity(account).now();

        return CollectionResponse
                .<AccountRecord>builder()
                .setItems(Arrays.asList(account))
                .build();
    }

    @ApiMethod(name = "unregister")
    public void unregisterDevice(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }

        AccountRecord account = DataSource.findAccountByEmail(user.getEmail());
        if(account == null) {
            log.info(user.getEmail() + " not registered, skipping unregister");
            return;
        }

        ofy().delete().entity(account).now();
    }

    @ApiMethod(name = "listDevices")
    public CollectionResponse<AccountRecord> listDevices(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }

        List<AccountRecord> records = ofy().load().type(AccountRecord.class).limit(100).list();
        return CollectionResponse.<AccountRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "deleteAll")
    public void deleteAllUsers(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }

        List<AccountRecord> records = ofy().load().type(AccountRecord.class).list();
        ofy().delete().entities(records).now();
    }
}
