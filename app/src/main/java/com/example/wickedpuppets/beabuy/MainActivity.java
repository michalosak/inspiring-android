package com.example.wickedpuppets.beabuy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;


interface APIInterface {

    @POST("/test")
    Call<String> getStringScalar();

    @POST("/receive")
    Call<String> postBeaconInfo(@Body BeaconInfo info);

}

//public interface ScalarService {
//    @POST("path")
//    Call<String> getStringScalar(@Body String body);
//}




public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ProximityManager proximityManager;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize("HrIntLkGdnKSpkrrSJZpesIMXcOkUTht");

        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.setIBeaconListener(createIBeaconListener());
        setFilters();
        imageView = (ImageView) findViewById(R.id.photo);


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


    static void getClient(String beaconUUID) {

        Log.d("RETROFIT", "getClient: called");

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())

                .baseUrl("http://192.170.20.98:5000")
                .build();

//        retrofit = new Retrofit.Builder()
//                .baseUrl("http://192.170.20.98:5000")
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
//                .build();

        BeaconInfo info = new BeaconInfo();
        info.beacon = beaconUUID;
        info.mail = "test2@mail.com";
        Call<String> postBeaconInfo = retrofit.create(APIInterface.class).postBeaconInfo(info);

        postBeaconInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
//                Toast.makeText(, "API Response: " + response.body(), Toast.LENGTH_LONG).show();
                Log.d("RETROFIT", "onResponse: body = " + response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
//                Toast.makeText(this, "API FAILED!!! ", Toast.LENGTH_LONG).show();
                Log.e("RETROFIT", "onFailure: FAILED TO SEND REQUEST");
            }
        });

//        return service;
    }

    void loadPhoto() {
        Picasso.with(this)

                .load("http://x3.wykop.pl/cdn/c3201142/comment_cIqwDFW1DRwypsz6B2k4sfs4VQ06FbJp.jpg")

                .into(imageView);
    }

    @Override
    protected void onStart() {
        super.onStart();
      //  loadPhoto();



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
            }

            @Override
            public void onIBeaconsUpdated (List< IBeaconDevice > ibeacons, IBeaconRegion beaconRegions){
                // when discovered beacon was not in the set proximity but it can be in the future
                // monitor the proximity for it
                for (IBeaconDevice ibeacon : ibeacons) {
                        getClient(ibeacon.getUniqueId());
                        Log.i(TAG, "Sample " +ibeacon.toString() );
                        showToast(ibeacon.getUniqueId());

//                    getClient();


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
}
