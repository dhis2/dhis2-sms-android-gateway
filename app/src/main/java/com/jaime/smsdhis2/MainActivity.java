package com.jaime.smsdhis2;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.jaime.smsdhis2.network.AuthGenerator;
import com.jaime.smsdhis2.network.IncomingSMS;
import com.jaime.smsdhis2.network.RetrofitController;
import com.jaime.smsdhis2.network.SMSResponse;
import com.jaime.smsdhis2.network.SmSAPI;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import de.adorsys.android.securestoragelibrary.SecurePreferences;
import de.adorsys.android.securestoragelibrary.SecureStorageException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity{

    private static String TAG = "MainActivity";

    Button btnSave;
    EditText txtPassword;
    EditText txtUsername;
    EditText txtURL;
    ToggleButton toggleForward;
    TextView textIPAddress;
    TextView tvLogs;
    private static final int PERMISSION_CODE = 123;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, WebService.class);
        this.startService(i);

        Log.i(TAG, "Created thread for server socket.");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        btnSave = findViewById(R.id.btnSave);
        txtPassword = findViewById(R.id.txtPassword);
        txtUsername = findViewById(R.id.txtUsername);
        txtURL = findViewById(R.id.txtURL);
        toggleForward = findViewById(R.id.toggleForward);
        textIPAddress = findViewById(R.id.textIPAddress);
        tvLogs = findViewById(R.id.textLogs);

        txtURL.setText(SecurePreferences.getStringValue(getBaseContext(),"dhis2.url",
                "http://android2.dhis2.org:8080/"));
        txtUsername.setText(SecurePreferences.getStringValue(getBaseContext(),"dhis2.username", "admin"));
        txtPassword.setText(SecurePreferences.getStringValue(getBaseContext(),"dhis2.password", "district"));
        toggleForward.setChecked(SecurePreferences.getBooleanValue(getBaseContext(),"dhis2.forward", true));
        // Show IP address
        textIPAddress.setText("Listening at: http://" + getLocalIpAddress()+ ":8000/send?recipient={recipient}&content={content}");

        //registerReceiver(SmsReceiver, intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED"););
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String password = txtPassword.getText().toString();
                String username = txtUsername.getText().toString();
                String url = txtURL.getText().toString();

                // Save it in preferences
                try {
                    SecurePreferences.setValue(getBaseContext(),"dhis2.username", username);
                    SecurePreferences.setValue(getBaseContext(), "dhis2.password", password);
                    SecurePreferences.setValue(getBaseContext(),"dhis2.url", url);
                    SecurePreferences.setValue(getBaseContext(),"dhis2.forward", toggleForward.isChecked());
                } catch (SecureStorageException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "Settings saved",
                        Toast.LENGTH_SHORT).show();

            }
        });

        requestSmsPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void requestSmsPermission() {
        if (Build.VERSION.SDK_INT <= 23){
            registerReceiver(new SmsReceiver(), new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        } else if ((ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS}, PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    registerReceiver(new SmsReceiver(), new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
                }
            }
        }
    }

    public String getLocalIpAddress() {

        String ipv4 = "";

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    Log.i(TAG,"ip1--:" + inetAddress);
                    Log.i(TAG,"ip2--:" + inetAddress.getHostAddress());

                    // for getting IPV4 format
                    if (!inetAddress.isLoopbackAddress()
                            && isValidIp4Address(ipv4 = inetAddress
                                    .getHostAddress())) {

                        String ip = inetAddress.getHostAddress().toString();
                        //System.out.println("ip---::" + ip);
                        //EditText tv = (EditText) findViewById(R.id.ipadd);
                        //tv.setText(ip);
                        // return inetAddress.getHostAddress().toString();
                        return ipv4;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    public boolean isValidIp4Address(final String hostName) {
        try {
            return Inet4Address.getByName(hostName) != null;
        } catch (UnknownHostException ex) {
            return false;
        }
    }

     class SmsReceiver extends BroadcastReceiver {
        private static final String TAG = "SmsReceiver";

        String urlString;
        String username;
        String password;
        Context context;

        @Override
        public void onReceive(Context context, Intent intent) {

            try {

                this.context = context;
                logMessage("  ");
                logMessage("SMS Received");

                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs = null;
                if (bundle != null) {
                    // ---retrieve the SMS message received---
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {

                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

                        String command = msgs[i].getMessageBody();
                        Log.d(TAG, "message before parsing=(" + command + ")");
                        urlString = SecurePreferences.getStringValue(getBaseContext(), "dhis2.url",
                                "http://yourdhis2url/api/");
                        username = SecurePreferences.getStringValue(getBaseContext(), "dhis2.username",
                                "admin");
                        password = SecurePreferences.getStringValue(getBaseContext(), "dhis2.password",
                                "district");

                        sendSMSToDhis2Server(username,password,urlString,
                                msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
                    }
                }

            } catch (Exception e) {
                logMessage("Failed to handle SMS forwarding");
                Log.e(TAG, "Exception:" + e, e);
            }
        }

         public void sendSMSToDhis2Server(String user, String password, String url,
                                         final String originator, String body){

            RetrofitController retrofitController = new RetrofitController(url);
            SmSAPI smsAPI = retrofitController.start();

            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            IncomingSMS incomingSMS = new IncomingSMS(body, originator, "unknown", date, date, "1");

            String logUrl = url+ "api/sms/inbound";
            logMessage("Server Url: "+ logUrl);
            logMessage("Sending SMS To dhis2ServerUrl: {");
            logMessage("originator: " + originator);
            logMessage("receivedDate: " + date);
            logMessage("sentDate: " + date);
            logMessage("smsEnconding: 1");
            logMessage("body: " + body);
            logMessage("}");

            smsAPI.sendSMS(AuthGenerator.getAuthToken(user,password), incomingSMS).enqueue(new Callback<SMSResponse>() {
                @Override
                public void onResponse(Call<SMSResponse> call, Response<SMSResponse> response) {
                    if (response.code() > 304){
                        String errorMessage;
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e){
                            errorMessage = response.toString();
                        }
                        logMessage(errorMessage);
                        return;
                    }
                    logMessage("SMS Sent to the server. Response code: " + response.code());
                    logMessage("  ");


                    //change 1 with real submission ID
                    String smsConfirmationResponse = "1:0::Submission has been processed successfully";
                    sendSMS(originator, smsConfirmationResponse, "Confirmation SMS sent to android app");
                }

                @Override
                public void onFailure(Call<SMSResponse> call, Throwable t) {
                    logMessage("Something went wrong, please check out your internet connection device/server");
                    logMessage("Message was not delivered successfully to dhis2 server");
                    sendSMS(originator, "Error sync could not be performed", "Confirmation SMS sent to android app");
                    logMessage("  ");
                }
            });
        }

        private void logMessage(String message){
            if (context != null && tvLogs != null){
                String logs = tvLogs.getText().toString();
                String finalLogs = logs +"\n" + message;
                tvLogs.setText(finalLogs);
            }
        }

         private void sendSMS(String phoneNumber, String message, String logMessage) {
             if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                 logMessage("  ");
                 logMessage("Can't send confirmation sms to lack of permissions");
                 return;
             }

             String SENT = "SMS_SENT";
             PendingIntent pi = PendingIntent.getBroadcast(context, 0,new Intent(SENT), 0);
             SmsManager sms = SmsManager.getDefault();
             sms.sendTextMessage(phoneNumber, null, message, pi, null);
             logMessage("  ");
             logMessage(logMessage);
         }
    }
}