package LocationDataObjects;

import java.util.ArrayList;
import java.util.List;

public class Result {

    private List<AreaName> areaName = new ArrayList<AreaName>();
    private List<Country> country = new ArrayList<Country>();
    private List<Region> region = new ArrayList<Region>();
    private String latitude;
    private String longitude;
    private String population;

    public List<AreaName> getAreaName() {
        return areaName;
    }

    public List<Country> getCountry() {
        return country;
    }

    public List<Region> getRegion() {
        return region;
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
