package edu.uiowa.engineering.iot_smoke.data;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class AirQualityRecord {

    @Id Long id;
    @Index private Long account;
    private String data;

    public Long getAccount() {
        return account;
    }

    public void setAccount(Long accountId) {
        this.account = accountId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
