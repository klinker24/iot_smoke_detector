package edu.uiowa.engineering.iot_smoke.endpoint;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;

import java.util.Collections;
import java.util.List;

import edu.uiowa.engineering.iot_smoke.TimeCompare;
import edu.uiowa.engineering.iot_smoke.data.AirQualityRecord;

import static edu.uiowa.engineering.iot_smoke.OfyService.ofy;

@Api(
        name = "analytics",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "iot_smoke.engineering.uiowa.edu",
                ownerName = "iot_smoke.engineering.uiowa.edu",
                packagePath=""
        )
)
public class AnalyticsEndpoint {

    @ApiMethod(name = "listReadings")
    public CollectionResponse<AirQualityRecord> listAllReadings() {
        List<AirQualityRecord> records = ofy().load()
                .type(AirQualityRecord.class)
                .list();

        Collections.sort(records, new TimeCompare());

        return CollectionResponse.<AirQualityRecord>builder().setItems(records).build();
    }
}
