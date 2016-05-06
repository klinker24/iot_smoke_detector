package edu.uiowa.engineering.iot_smoke.data;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class AirQualityRecord {

    @Id Long id;
    @Index private Long account;

    private float temperature;
    private float relativeHumidity;
    private float particleDensity;
    @Index private long time;

    public Long getAccount() {
        return account;
    }

    public void setAccount(Long accountId) {
        this.account = accountId;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getParticleDensity() {
        return particleDensity;
    }

    public void setParticleDensity(float particleDensity) {
        this.particleDensity = particleDensity;
    }

    public float getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(float relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
