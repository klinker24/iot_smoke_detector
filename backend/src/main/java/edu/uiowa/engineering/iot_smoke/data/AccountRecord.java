package edu.uiowa.engineering.iot_smoke.data;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AccountRecord {

    @Id Long id;
    @Index private String email;
    @Index private String authToken;
    private List<DeviceRecord> deviceList;

    public AccountRecord() {
        this.deviceList = new ArrayList();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void addDevice(DeviceRecord device) {
        this.deviceList.add(device);
    }

    public List<DeviceRecord> getDeviceList() {
        return deviceList;
    }
}
