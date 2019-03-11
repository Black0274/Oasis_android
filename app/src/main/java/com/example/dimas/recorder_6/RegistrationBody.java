package com.example.dimas.recorder_6;

import com.google.gson.annotations.SerializedName;

public class RegistrationBody {

    public String login;

    public String password;

    public String lang;

    RegistrationBody(String log, String pas, String lan) {
        this.login = log;
        this.password = pas;
        this.lang = lan;
    }

}
