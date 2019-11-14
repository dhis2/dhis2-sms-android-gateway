package com.jaime.smsdhis2.network;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class AuthGenerator {

    public static String getAuthToken(String user, String password) {
        byte[] data = new byte[0];
        try {
            data = (user + ":" + password).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Basic " + Base64.encodeToString(data, Base64.NO_WRAP);
    }
}
