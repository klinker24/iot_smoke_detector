package edu.uiowa.engineering.iot_smoke;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.Date;
import java.util.UUID;

public class Util {

    /**
     * Generate a unique authorization token for an account.
     *
     * @param email email of the account to generate for
     * @return String of the authorization token
     */
    public static String generateAuthToken(String email) {
        String key = UUID.randomUUID().toString().toUpperCase() +
                "|" + "iot_smoke_project_token" +
                "|" + email +
                "|" + new Date().getTime();

        StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
        jasypt.setPassword("iot_smoke");

        String authToken = jasypt.encrypt(key);

        if (authToken.length() > 25) {
            return authToken.substring(0, 25);
        } else {
            return authToken;
        }
    }
}
