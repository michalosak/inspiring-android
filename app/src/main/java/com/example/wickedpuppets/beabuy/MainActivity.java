package com.example.wickedpuppets.beabuy;

import android.Manifest;
import android.content.pm.PackageManager;
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

    void loadPhoto() {
        Picasso.with(this)

                .load("http://x3.wykop.pl/cdn/c3201142/comment_cIqwDFW1DRwypsz6B2k4sfs4VQ06FbJp.jpg")

                .into(imageView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPhoto();

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
