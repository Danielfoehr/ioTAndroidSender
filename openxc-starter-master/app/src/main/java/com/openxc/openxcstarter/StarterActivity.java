package com.openxc.openxcstarter;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.VehicleSpeed;
import com.openxcplatform.openxcstarter.R;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
/*import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
*/

import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;  //  here is the http client
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity which is the starting point of the Android application
 */

public class StarterActivity extends Activity {

    private VehicleManager mVehicleManager;
    private TextView mSuccessRequests;
    private TextView mFailedRequests;
    private static TextView mConnected;
    private static EditText mTripId;
    private int successCounter = 0;
    private int failureCounter = 0;
    private int MessagesRecieved;
    private boolean serviceIsBound = false;
    private static StarterActivity starterActivcity;
    private static boolean enabled = false;
    private static int uniqueIdentificationId;
    private static NotificationManager mNotificationManager ;
    private static NotificationCompat.Builder notificationBuilder;

    public static int getUniqueIdentificationId() {
        return uniqueIdentificationId;
    }

    public static NotificationCompat.Builder getNotificationBuilder() {
        return notificationBuilder;
    }

    public static NotificationManager getmNotificationManager() {

        return mNotificationManager;
    }

    public static EditText getmTripId() {
        return mTripId;
    }


    public static void setEnabled(boolean enabled) {
        StarterActivity.enabled = enabled;
    }

    public static boolean getEnabled() {

        return enabled;
    }

    public TextView getmFailedRequests() {
        return mFailedRequests;
    }

    public static StarterActivity getStarterActivcity() {
        return starterActivcity;
    }

    public TextView getmSuccessRequests() {
        return mSuccessRequests;
    }

    /**
     * Android specific lifecycle method- Method gets created during initialization of activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_starter);

        //store main activity reference in variable
        starterActivcity = this;

        //get UI views from activity_starter.xml
        mSuccessRequests = (TextView) findViewById(R.id.CountSuccessRequests);
        mFailedRequests = (TextView) findViewById(R.id.CountFailedRequests);
        mConnected = (TextView) findViewById(R.id.ServiceStatus);
        mTripId = (EditText) findViewById(R.id.TripId);


        ImageView imageViewStart = (ImageView) findViewById(R.id.Start);
        imageViewStart.setOnClickListener(new ListenerClassStart());

        //initialize threath pool
      new SimpleThreathPool();

    }

    /**
     * Android specific activity lifecycle method which gets called from Android system
     * when the activity starts up or returns from the background.
     * Re-connect to the VehicleManager so we can receive updates.
     */
    @Override
    public void onResume() {
        super.onResume();
    if (mVehicleManager == null) {
        Intent intent = new Intent(this, VehicleManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }
}

    /**
     * This listener object receives updates through the VehicleManager service when there is a new measurement available in the OBD2 device.
     * If there is a new speed measurement available, there are new measurements available for every other measuremnt type( gear, acceleration,...)
     */
    VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {


            MessagesRecieved++;

            //Propably TCP slowstart causes delay in first http post
            //using workaround -> please suggest a better one or how to solve the issue
            if (successCounter == 0 && failureCounter == 0) {
                //workaround: make first post request to localhost -> reduce delay
                Thread thread = new Thread() {

                    @Override
                    public void run() {

                        try

                        {
                            HttpClient Apacheclient = new DefaultHttpClient();
                            String url = "http://localhost/";
                            HttpPost httpPost = new HttpPost(url);
                            org.apache.http.HttpResponse response = Apacheclient.execute(httpPost);

                        } catch (
                                Exception e
                                )

                        {
                            //excpected Error
                            failureCounter++;
                        }
                    }

                };

                thread.run();

            }

            if (failureCounter >= 1 && enabled == true) {

                try {


                    Measurement vehicleSpeed = mVehicleManager.get(VehicleSpeed.class);
                    Measurement fuelConsumed = mVehicleManager.get(FuelConsumed.class);
                    Measurement acceleratorPedalPosition = mVehicleManager.get(AcceleratorPedalPosition.class);
                    Measurement odometer = mVehicleManager.get(Odometer.class);
                    Measurement transmissionGearPosition = mVehicleManager.get(com.openxc.measurements.TransmissionGearPosition.class);

                    //Get GPS Data
                    Measurement latitude = mVehicleManager.get(Latitude.class);
                    Measurement longitude = mVehicleManager.get(Longitude.class);

                    SimpleThreathPool.startMultiThreathing(vehicleSpeed, fuelConsumed, odometer, acceleratorPedalPosition, transmissionGearPosition, longitude,latitude,  mTripId.getText().toString(), MessagesRecieved);


                } catch (Exception e) {
                    Log.w("Info", "The vehicle may not have made the measurement yet");
                }

            }
        }
    };


    /**
     * Called when the connection with the VehicleManager service is
     * established, i.e. bound.
     */

    private ServiceConnection mConnection = new ServiceConnection() {

        /**
         * When app is connected to a running vehicle service.
         * If this method does not get called make sure to install the Open XC enabler application from Google Play.
         * The enabler application starts the vehicle service during startup
         * @param className
         * @param service
         */
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i("Info", "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();
            mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
            serviceIsBound = true;
            mConnected.setText("Connected to Service");
            mConnected.setTextColor(Color.GREEN);
        }

        /**
         * @param className
         * Called when the connection with the service disconnects unexpectedly
         */
        public void onServiceDisconnected(ComponentName className) {
            Log.e("Info", "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
            serviceIsBound = false;
            mConnected.setText("Disconnected from Service");
            mConnected.setTextColor(Color.RED);

            mNotificationManager.cancel(uniqueIdentificationId);
        }
    };

    /**
     * creates a Android notification which can be updated later on through a unique id.
     * displays status information
     */
    public static void showNotification() {


        notificationBuilder = new NotificationCompat.Builder(
                starterActivcity).setSmallIcon(R.drawable.ic_action_send)
                .setContentTitle("Sending Device Messages")
                .setContentText("Sent: 100 , Failed: 10");
        notificationBuilder.setProgress(0, 0, true);

        Intent resultIntent = new Intent();


        TaskStackBuilder stackBuilder = TaskStackBuilder
                .create(starterActivcity);

        stackBuilder.addParentStack(starterActivcity);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) starterActivcity.getSystemService(starterActivcity.getBaseContext().NOTIFICATION_SERVICE);
        uniqueIdentificationId = Math.round(System.currentTimeMillis());
        mNotificationManager.notify(uniqueIdentificationId, notificationBuilder.build());
    }

    public static void cancelNotification(){

        mNotificationManager.cancel(uniqueIdentificationId);
    }
}