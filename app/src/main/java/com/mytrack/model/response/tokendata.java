package com.mytrack.model.response;

public class tokendata {

    private String name,email,userId,model,imageEncoded,phoneno, Encryptpwd, token;
    private Double gps_Lat,gps_Lng;

    public tokendata() {
    }

    public tokendata(String name,String email,String phoneno,Double gps_Lat,Double gps_Lng,String userId,String model,String imageEncoded,String Encryptpwd,String token) {
        this.name = name;
        this.email = email;
        this.gps_Lat = gps_Lat;
        this.gps_Lng = gps_Lng;
        this.userId = userId;
        this.model = model;
        this.imageEncoded = imageEncoded;
        this.phoneno = phoneno;
        this.Encryptpwd = Encryptpwd;
        this.token = token;
    }

    public Double getGps_Lat() {
        return gps_Lat;
    }

    public void setGps_Lat(Double gps_Lat) {
        this.gps_Lat = gps_Lat;
    }

    public Double getGps_Lng() {
        return gps_Lng;
    }

    public void setGps_Lng(Double gps_Lng) {
        this.gps_Lng = gps_Lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getImageEncoded() {
        return imageEncoded;
    }

    public void setImageEncoded(String imageEncoded) {
        this.imageEncoded = imageEncoded;
    }

    public String getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

    public String getEncryptpwd() {
        return Encryptpwd;
    }

    public void setEncryptpwd(String encryptpwd) {
        Encryptpwd = encryptpwd;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
