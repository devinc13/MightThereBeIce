package DataObjects;

import java.util.ArrayList;
import java.util.List;

public class Data {

    private List<Request> request = new ArrayList<Request>();
    private List<Weather> weather = new ArrayList<Weather>();

    public List<Request> getRequest() {
        return request;
    }

    public List<Weather> getWeather() {
        return weather;
    }
}