package com.jaime.smsdhis2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class WebService extends Service {

    private static String TAG = "MainActivity";
    NotificationManager manager;
    Notification myNotication;
    private String channel_ID = "ChannelID";
    private String channel_NAME = "ChannelName";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(serverConn).start();

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel mChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(channel_ID, channel_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(mChannel);
        }

        Intent notificationIntent = new Intent(this, WebService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(getApplicationContext(), channel_ID);
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            builder.setContentTitle("DHIS2");
            builder.setContentText("DHIS2 SMS Gateway");
            builder.setSmallIcon(R.drawable.ic_launcher_background);
            builder.setContentIntent(pendingIntent);
            Notification notification = builder.build();
            startForeground(11, notification);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO for communication return IBinder implementation
        return null;
    }

    private ServerSocket server;

    Runnable serverConn = new Runnable() {
        public void run() {
            try {

                //Log.i(TAG, "IP Address:" + getLocalIpAddress());
                server = new ServerSocket(8000);

                while (true) {

                    Socket socket = server.accept();
                    ConnectionHandler conn = new ConnectionHandler(socket);
                    conn.start();

                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };

    class ConnectionHandler extends Thread {

        Socket socket = null;

        public ConnectionHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {

                while (true) {

                    Log.i(TAG, "Accepted server socket "
                            + socket.getRemoteSocketAddress().toString());

                    PrintWriter out = new PrintWriter(socket.getOutputStream(),
                            true);
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    String inputLine, outputLine;

                    String content = "";
                    String recipient = "";

                    inputLine = in.readLine();
                    Log.i(TAG, "RX:" + inputLine);
                    if (inputLine.contains("GET ") && inputLine.contains("?")) {
                        String qs = inputLine.substring(inputLine.indexOf("?"));
                        Log.i(TAG, "QS:" + qs + "END");
                        if (qs.contains(" ")) {
                            qs = qs.substring(0, qs.indexOf(" "));
                        }
                        Log.i(TAG, "QS:" + qs + "END");
                        Uri uri = Uri.parse(qs);
                        content = uri.getQueryParameter("content");
                        recipient = uri.getQueryParameter("recipient");
                        Log.i(TAG, "content:" + content + ", recipient:"
                                + recipient);
                    }

                    while (inputLine != null && !"".equals(inputLine)) {
                        //out.println("RX:" + inputLine);
                        Log.i(TAG, "RX:" + inputLine);
                        inputLine = in.readLine();
                    }

                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/html; charset=utf-8\r\n");
                    out.println();
                    out.println("<html><form name=\"input\" action=\"send\" method=\"get\">" +
                            "Recipient: <input type=\"text\" name=\"recipient\" value=\"" + recipient + "\">" +
                            "Content: <input type=\"text\" name=\"content\"  value=\"" + content + "\">" +
                            "<input type=\"submit\" value=\"Submit\"></form></html>");

                    //out.println("Message forwarded to SMS\r\n");
                    Log.i(TAG, "Closing connection");

                    in.close();
                    out.close();
                    socket.close();

                    // Sending message

                    if (content == null || recipient == null ||
                            "".equals(content) || "".equals(recipient)) {
                        Log.i(TAG, "SMS not sent");
                    } else {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(recipient, null, content, null, null);
                        Log.i(TAG, "Sending SMS.");
                    }

                    Log.i(TAG, "Message sent");
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
