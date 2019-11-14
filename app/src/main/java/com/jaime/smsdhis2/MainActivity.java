package com.jaime.smsdhis2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class MainActivity extends Activity {

    private static String TAG = "MainActivity";
    public static final String PREFS_NAME = "DHIS2PrefsFile";


    Button btnSave;
    EditText txtPassword;
    EditText txtUsername;
    EditText txtURL;
    ToggleButton toggleForward;
    TextView textIPAddress;
    private static final int PERMISSION_SEND_SMS = 123;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent(this, WebService.class);
        // potentially add data to the intent
        // i.putExtra("KEY1", "Value to be used by the service");
        this.startService(i);
        // bindService(i, connection, this.BIND_AUTO_CREATE);

        Log.i(TAG, "Created thread for server socket.");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        setContentView(R.layout.activity_main);

        btnSave = findViewById(R.id.btnSave);
        txtPassword = findViewById(R.id.txtPassword);
        txtUsername = findViewById(R.id.txtUsername);
        txtURL = findViewById(R.id.txtURL);
        toggleForward = findViewById(R.id.toggleForward);
        textIPAddress = findViewById(R.id.textIPAddress);

        txtURL.setText(settings.getString("dhis2.url",
                "http://android2.dhis2.org:8080/"));
        txtUsername.setText(settings.getString("dhis2.username", "admin"));
        txtPassword.setText(settings.getString("dhis2.password", "district"));
        toggleForward.setChecked(settings.getBoolean("dhis2.forward", true));

        // Show IP address
        textIPAddress.setText("Listening at: http://" + getLocalIpAddress()+ ":8000/send?recipient={recipient}&content={content}");

        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String password = txtPassword.getText().toString();
                String username = txtUsername.getText().toString();
                String url = txtURL.getText().toString();

                // Save it in preferences
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("dhis2.username", username);
                editor.putString("dhis2.password", password);
                editor.putString("dhis2.url", url);
                editor.putBoolean("dhis2.forward", toggleForward.isChecked());
                editor.commit();
                Toast.makeText(getApplicationContext(), "Settings saved",
                        Toast.LENGTH_SHORT).show();

            }
        });

        requestSmsPermission();
    }

    private void requestSmsPermission() {
        // check permission is given
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
        }
    }

    // ---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message) {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, pi, null);
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
    /*
     * public String getLocalIpAddress() {
     *
     * try { for (Enumeration < NetworkInterface > en =
     * NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
     * NetworkInterface intf = en.nextElement(); for (Enumeration < InetAddress
     * > enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
     * InetAddress inetAddress = enumIpAddr.nextElement(); if
     * (!inetAddress.isLoopbackAddress()) { return
     * inetAddress.getHostAddress().toString(); } } } } catch (SocketException
     * ex) { Log.e(TAG, ex.toString()); } return null; }
     */
}