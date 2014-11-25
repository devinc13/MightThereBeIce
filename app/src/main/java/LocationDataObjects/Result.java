package LocationDataObjects;

import java.util.ArrayList;
import java.util.List;

public class Result {

    private List<AreaName> areaName = new ArrayList<AreaName>();
    private String latitude;
    private String longitude;
    private String population;

    public List<AreaName> getAreaName() {
        return areaName;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getPopulation() {
        return population;
    }
}
