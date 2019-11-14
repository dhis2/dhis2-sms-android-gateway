package com.jaime.smsdhis2;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.jaime.smsdhis2.network.AuthGenerator;
import com.jaime.smsdhis2.network.IncomingSMS;
import com.jaime.smsdhis2.network.RetrofitController;
import com.jaime.smsdhis2.network.SMSResponse;
import com.jaime.smsdhis2.network.SmSAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    public static final String PREFS_NAME = "DHIS2PrefsFile";
    String urlString;
    String username;
    String password;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {

            this.context = context;

            SharedPreferences settings = context.getSharedPreferences(
                    PREFS_NAME, 0);
            boolean forward = settings.getBoolean("dhis2.forward", false);
            String commands = settings.getString("dhis2.commands", "");


            if (!forward || commands == null) {
                return;
            }

            // ---get the SMS message passed in---
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            if (bundle != null) {
                // ---retrieve the SMS message received---
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                for (int i = 0; i < msgs.length; i++) {

                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

                    String command = msgs[i].getMessageBody()
                            ;
                    Log.d(TAG, "message before parsing=(" + command + ")");

                    urlString = settings
                            .getString("dhis2.url",
                                    "http://yourdhis2url/api/");
                    username = settings.getString("dhis2.username",
                            "admin");
                    password = settings.getString("dhis2.password",
                            "district");

                    sendSMSToDhis2Server(username,password,urlString,
                            msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
                }
            }

        } catch (Exception e) {
            Toast.makeText(context, "Failed to handle SMS forwarding",
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception:" + e, e);

        }
    }

    public void sendSMSToDhis2Server(String user, String password, String url,
                                     String originator, String body){

        RetrofitController retrofitController = new RetrofitController(url);
        SmSAPI smsAPI = retrofitController.start();

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        IncomingSMS incomingSMS = new IncomingSMS(body, originator, "unknown", date, date, "1");

        smsAPI.sendSMS(AuthGenerator.getAuthToken(user,password), incomingSMS).enqueue(new Callback<SMSResponse>() {
            @Override
            public void onResponse(Call<SMSResponse> call, Response<SMSResponse> response) {
                    if (response.code() > 304){
                        Toast.makeText(context, "There was a error, please check URL" + response.code(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(context, "SMS Sent to the server " + response.code(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<SMSResponse> call, Throwable t) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}