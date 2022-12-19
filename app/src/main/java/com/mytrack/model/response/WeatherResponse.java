package com.mytrack.model.response;

import java.util.ArrayList;

public class WeatherResponse {
    public String id;
    public String name;
    public String visibility;
    public ArrayList<weather> weather;
    public main main;
    public wind wind;
    public sys sys;
    public String cod;
    public String dt;
    public coord coord;
    public String timezone;
    public String base;

    public class coord{
        public String lon;
        public String lat;
    }

    public class main {
        public String temp;
        public String pressure;
        public String humidity;
        public String temp_min;
        public String temp_max;
    }

    public class weather {
        public String id;
        public String main;
        public String description;
        public String icon;
    }

    public class wind {
        public String speed;
    }

    public class sys {
        public String type;
        public String id;
        public String message;
        public String country;
        public String sunrise;
        public String sunset;
    }
}
