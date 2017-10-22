package com.example.wickedpuppets.beabuy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wickedpuppets.beabuy.interfaces.RetroFitInterface;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;




public class MainActivity extends AppCompatActivity {

    private static final String host = "http://192.168.1.83:8080/";
//    private static final String host = "http://localhosst:8080/";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static User user = new User();
    private ProximityManager proximityManager;
    private Context context;
    private Button send;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        prepareRetrofit();
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize("HrIntLkGdnKSpkrrSJZpesIMXcOkUTht");
        send = (Button) findViewById(R.id.send);
        proximityManager = ProximityManagerFactory.create(context);
        proximityManager.setIBeaconListener(createIBeaconListener());
        setFilters();
        send.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.i("CLICK_SEND", "OK");
                EditText name = (EditText) findViewById(R.id.inputName);
                user.setName(name.getText().toString());
                sendUser(host + "users/add/");

            }

        });
    }


    private void checkPermissionAndStart() {
        int checkSelfPermissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);

        if (PackageManager.PERMISSION_GRANTED == checkSelfPermissionResult) {
            Log.d(TAG, "checkPermissionAndStart: zaakceptowane");
            //already granted
            startScanning();
        } else {
            //request permission
            Log.d(TAG, "checkPermissionAndStart: niezaakpcetowane");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

        }
    }

    // Toasts on device
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (100 == requestCode) {
                Log.d(TAG, "onRequestPermissionsResult: 100 code");
                //same request code as was in request permission
                startScanning();
                Toast.makeText(this, "AKCPETUJ TOO!!!!", Toast.LENGTH_SHORT).show();
            }

        } else {
            Log.d(TAG, "onRequestPermissionsResult: ");
            //not granted permission
            //show some explanation dialog that some features will not work
            Toast.makeText(this, "AKCPETUJ TOO!!!!", Toast.LENGTH_SHORT).show();
        }
    }


    private void setFilters() {
        IBeaconFilter customIBeaconFilter = new IBeaconFilter() {
            @Override
            public boolean apply(IBeaconDevice iBeaconDevice) {
                // So here we set the max distance from a beacon to 1m
                return iBeaconDevice.getDistance() < 1;
            }
        };

        proximityManager.filters().iBeaconFilter(customIBeaconFilter);
    }

    private static Retrofit retrofit = null;

    static Retrofit prepareRetrofit() {
        Log.d("RETROFIT", "sendBeacon: called");

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        return retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl(host)
                .build();
    }

    void setBaseUrl(String url) {
        retrofit.baseUrl().newBuilder(url).build();
    }

    void sendUser(String url) {

        setBaseUrl(url);
        Call<JSONObject> postUser = retrofit.create(RetroFitInterface.class).postUser(user);
        postUser.enqueue(new Callback<JSONObject>() {


            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                JSONObject bodyResponse = response.body();
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Log.e("RETROFIT", "onFailure: FAILED TO SEND REQUEST");
            }
        });
    }

    void sendBeacon(String beaconUUID, String url) {
        setBaseUrl(url);
        Beacon info = new Beacon();
        info.setBeaconId(beaconUUID);
        info.setUser(user.getName());
        Call<JSONObject> postBeaconInfo = MainActivity.retrofit.create(RetroFitInterface.class).postBeaconInfo(info);

        postBeaconInfo.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
//

                JSONObject bodyResponse = response.body();

            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
//                Toast.makeText(this, "API FAILED!!! ", Toast.LENGTH_LONG).show();
                Log.e("RETROFIT", "onFailure: FAILED TO SEND REQUEST");
            }
        });

//        return service;
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkPermissionAndStart();
        startScanning();
    }


    @Override
    protected void onStop() {
        proximityManager.stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;
        super.onDestroy();
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                Log.d(TAG, "onServiceReady: Scan in running");
                proximityManager.startScanning();
            }
        });
    }


    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                Log.i("Sample", "IBeacon discovered: " + ibeacon.getUniqueId());
                showToast(ibeacon.getUniqueId());
                Log.i("USERNAME", user.getName());
                if (ibeacon.getUniqueId() != null && !user.getName().isEmpty()) {
                    sendBeacon(ibeacon.getUniqueId(), host + " beacons/");
                    Log.i("ON UPDATED", " " + ibeacon.toString());
                    showToast(ibeacon.getUniqueId());
                }
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> ibeacons, IBeaconRegion beaconRegions) {
                // when discovered beacon was not in the set proximity but it can be in the future
                // monitor the proximity for it

                Integer i = 1;
                for (IBeaconDevice ibeacon : ibeacons) {
                    if (ibeacon.getUniqueId() != null && !user.getName().isEmpty()) {
                        i++;
                        sendBeacon(ibeacon.getUniqueId(), host + " beacons/");
                        Log.i("ON UPDATED", " " + ibeacon.toString());
                        showToast(ibeacon.getUniqueId());
                    }
                }

            }


        };
    }

    private EddystoneListener createEddystoneListener() {
        return new SimpleEddystoneListener() {
            @Override
            public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.i("Sample", "Eddystone discovered: " + eddystone.toString());
            }
        };
    }


    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }


}
