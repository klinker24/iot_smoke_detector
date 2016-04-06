/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Backend with Google Cloud Messaging" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/GcmEndpoints
*/

package edu.uiowa.engineering.iot_smoke;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.oauth.OAuthRequestException;

import java.util.List;
import java.util.logging.Logger;
import javax.inject.Named;

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
    public void registerDevice(@Named("regId") String regId, User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }

        RegistrationRecord record = findRecord(user.getEmail());
        if(record != null) {
            ofy().delete().entity(record).now();
        }

        record = new RegistrationRecord();
        record.setEmail(user.getEmail());
        record.setRegId(regId);
        ofy().save().entity(record).now();
    }

    @ApiMethod(name = "unregister")
    public void unregisterDevice(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }
        RegistrationRecord record = findRecord(user.getEmail());
        if(record == null) {
            log.info(user.getEmail() + " not registered, skipping unregister");
            return;
        }
        ofy().delete().entity(record).now();
    }

    @ApiMethod(name = "listDevices")
    public CollectionResponse<RegistrationRecord> listDevices(@Named("count") int count, User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }

        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).limit(count).list();
        return CollectionResponse.<RegistrationRecord>builder().setItems(records).build();
    }

    @ApiMethod(name = "deleteAll")
    public void deleteAllUsers(User user) throws OAuthRequestException {
        if (user == null) {
            throw new OAuthRequestException("User not authorized.");
        }

        List<RegistrationRecord> records = ofy().load().type(RegistrationRecord.class).list();
        ofy().delete().entities(records).now();
    }

    private RegistrationRecord findRecord(String email) {
        return ofy().load().type(RegistrationRecord.class).filter("email", email).first().now();
    }
}
