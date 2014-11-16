package DataObjects;

import java.util.ArrayList;
import java.util.List;

public class Weather {

    private String date;
    private List<Hourly> hourly = new ArrayList<Hourly>();
    private String maxtempC;
    private String maxtempF;
    private String mintempC;
    private String mintempF;

    public String getDate() {
        return date;
    }

    public List<Hourly> getHourly() {
        return hourly;
    }

    public String getMaxtempC() {
        return maxtempC;
    }

    public String getMaxtempF() {
        return maxtempF;
    }

    public String getMintempC() {
        return mintempC;
    }

    public String getMintempF() {
        return mintempF;
    }
}