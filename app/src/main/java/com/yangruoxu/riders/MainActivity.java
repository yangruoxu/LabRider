package com.yangruoxu.riders;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private String provider;
    private TextView positionTextView;
    private String filename;
    private Runnable runnable;
    private Handler handler;
    private String StartTime;
    private String data;
    //private String imei = UUID.randomUUID().toString().replace("-","");
    //private String imei = getPhoneIMEI();
    private Timer timer;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StartTime = getCurrTime();
        runnable = new Runnable() {
            @Override
            public void run() {
                geolocation();
                handler.postDelayed(this, 1000 * 5);
            }
        };
        handler = new Handler();
        handler.postDelayed(runnable, 0);
//        while(true){
//            geolocation();
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void geolocation() {

        //int hasGeolocationPermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION);
//        if(hasGeolocationPermission != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this, );
//        }

        positionTextView = (TextView) findViewById(R.id.postion_text);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Criteria criteria = new Criteria();

        //provider = locationManager.getBestProvider(criteria, true);
        //Toast.makeText(this, provider, Toast.LENGTH_SHORT).show();

        List<String> providerList = locationManager.getProviders(true);

        if (providerList.contains(LocationManager.GPS_PROVIDER) & providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            //Toast.makeText(this,"GPS location service to use",Toast.LENGTH_SHORT).show();
            //Toast.makeText(this,"we have two method",Toast.LENGTH_SHORT).show();

            provider = locationManager.GPS_PROVIDER;
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            Location location_g = locationManager.getLastKnownLocation(provider);
            //Location location_g = locationManager.getLastKnownLocation(provider);
            double gps_acc = 10000000000.0;
            double net_acc = 10000000000.0;
            try {
                gps_acc = location_g.getAccuracy();
                //Toast.makeText(this, Float.toString(gps_acc),Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            provider = locationManager.NETWORK_PROVIDER;
            Location location_n = locationManager.getLastKnownLocation(provider);
            try {
                net_acc = location_n.getAccuracy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Toast.makeText(this, Float.toString(net_acc),Toast.LENGTH_SHORT).show();

            if (net_acc < gps_acc) {
                provider = locationManager.NETWORK_PROVIDER;
                //Toast.makeText(this,"Network is better", Toast.LENGTH_SHORT).show();
                Log.d("gps", "net is better");
            } else {
                provider = locationManager.GPS_PROVIDER;
                //Toast.makeText(this,"GPS is better", Toast.LENGTH_SHORT).show();
                Log.d("gps", "GPS is better");
            }
        } else if (providerList.contains(locationManager.GPS_PROVIDER) & !providerList.contains(locationManager.NETWORK_PROVIDER)) {
            //Toast.makeText(this,"GPS location service to use", Toast.LENGTH_SHORT).show();
            Log.d("gps", "geolocation: gps is used!");
            provider = locationManager.GPS_PROVIDER;
        } else if (providerList.contains(locationManager.NETWORK_PROVIDER) & !providerList.contains(locationManager.GPS_PROVIDER)) {
            //Toast.makeText(this,"Network location service to use", Toast.LENGTH_SHORT).show();
            Log.d("gps", "geolocation: net is used");
            provider = locationManager.GPS_PROVIDER;
        } else {
            //Toast.makeText(this,"no location service to use", Toast.LENGTH_SHORT).show();
            Log.d("gps", "geolocation: failed to read gps data");
            return;
        }


        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            save(location, getCurrTime());
            showLocation(location);
        }
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 1000, 0, locationListener);
        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onLocationChanged(Location location) {
            //save(location, getCurrTime());
            //showLocation(location);
        }
    };

    private void showLocation(Location location) {
        String Time = getCurrTime();
        String currentPostion = "time is: " + Time + "\n" + "latitude is: " + location.getLatitude() + "\n" +
                "longitude is: " + location.getLongitude() + "\n" + "speed is: " + location.getSpeed() + "\n" + provider + "\n" + "accuracy: " + location.getAccuracy() + "\n" + location.getTime();
        positionTextView.setText(currentPostion);
        return;
    }

    ;


    public void save(Location location, String CurrentTime) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        File file = null;

        String imei = UUID.randomUUID().toString().replace("-","");
        //String imei = getPhoneIMEI();
        data = imei + "|" + provider + ":" + CurrentTime + ";" + location.getLongitude() + "," + location.getLatitude() + ";" + location.getSpeed() + ";" + location.getAccuracy() + "\n";
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("socket", "run: 22222");
                try {
                    Socket socket = new Socket("202.120.38.188", 18777);

                    OutputStream os = socket.getOutputStream();
                    os.write(data.getBytes());
                    //OutputStreamWriter w = new OutputStreamWriter(os, "UTF-8");
                    Log.d("socket", "11111");
                    //w.flush();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bw.write(data);
                    bw.flush();
                    bw.close();
                    socket.shutdownOutput();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        try {
            String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
            file = new File(sd + "/Download/" + StartTime + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            Log.d("write", e.toString());
            Log.d("write", "write Error! ");
        }

        try {
            String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
            //Toast.makeText(this, sd, Toast.LENGTH_SHORT).show();
            //String pwd = getFilesDir().getPath();
            //Toast.makeText(this, pwd, Toast.LENGTH_SHORT).show();
            out = new FileOutputStream(sd + "/Download/" + StartTime + ".txt", true);
            //String Time = getCurrTime();

            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
            //Toast.makeText(this, "done!", Toast.LENGTH_SHORT).show();
            Log.d("write", "save complete! ");

        } catch (IOException e) {
            //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }


    private String getPhoneIMEI() {
        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Service.TELEPHONY_SERVICE);
        //TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELECOM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                String b =  tm.getImei();
                return b;

            }
        }
        //String b = tm.getLine1Number();
        return tm.getImei();
        //return "";
    };

    public String getCurrTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }


}
