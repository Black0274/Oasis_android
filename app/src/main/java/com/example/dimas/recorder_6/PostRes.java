package com.example.dimas.recorder_6;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostRes {
    @SerializedName("login")
    private String Login;

    @SerializedName("password")
    private String Password;

    @SerializedName("lang")
    private String Lang;

    public String getLogin() { return Login; }

    public void setLogin(String Log) { this.Login = Log; }

    public String getPassword() { return Password; }

    public void setPassword(String Pas) { this.Password = Pas; }

    public String getLang() { return Lang; }

    public void setLang(String Lan) { this.Lang = Lan; }
}
