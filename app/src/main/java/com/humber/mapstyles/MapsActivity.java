package com.humber.mapstyles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM_LEVEL = 12;
    private static final String TAG = "Rankit";
    private GoogleMap mMap;
    private Toolbar myToolbar;
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        client = LocationServices.getFusedLocationProviderClient(this);

        displayActionBar();
        permissionCheck();
    }

    // Map Ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Default Style
        normal(mMap);
    }

    // Permissions
    private void permissionCheck()
    {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        getMyLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    // Current Location
    private void getMyLocation() {

        // Permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // Current Locations
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                // Current Lat,lon
                LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());

                // Add circle radius
                CircleOptions circleOptions = new CircleOptions().center(new LatLng(location.getLatitude(),location.getLongitude()))
                                                                 .radius(1000)
                                                                 .fillColor(Color.argb(100, 66, 156, 245))
                                                                 .strokeColor(Color.GRAY)
                                                                 .strokeWidth(2);
                mMap.addCircle(circleOptions);

                // Add a marker and move the camera
                mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_baseline_adjust_24))
                                    .position(myLocation)
                                    .title("Current Location"));

                // Zoom Level 12
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, DEFAULT_ZOOM_LEVEL));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity.this, "Current Location not found" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Custom Marker
    private BitmapDescriptor BitmapDescriptorFromVector(Context applicationContext, int vectorId) {
        Drawable drawable  = ContextCompat.getDrawable(applicationContext,vectorId);
                drawable.setBounds(0,
                                   0,
                                   drawable.getIntrinsicWidth(),
                                   drawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                                            drawable.getIntrinsicHeight(),
                                            Bitmap.Config.ARGB_8888);

            Canvas c  = new Canvas(bitmap);
                          drawable.draw(c);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // Toolbar and Menus
    public void displayActionBar()
    {
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    // Menu Options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.themes, menu);
        return true;
    }

    // Option selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.normal:
                try {
                    boolean success =   mMap.setMapStyle
                            (MapStyleOptions.loadRawResourceStyle
                                    (this,R.raw.map_style));
                    if(!success)
                    {  Log.d(TAG,"Styles parsing failed"); }
                }catch (Resources.NotFoundException e)
                {
                    Log.d(TAG,"Styles not found",e);
                }
                return true;

            case R.id.dark:
                try {
                    boolean success =   mMap.setMapStyle
                            (MapStyleOptions.loadRawResourceStyle
                                    (this,R.raw.night_style));
                    if(!success)
                    {  Log.d(TAG,"Styles parsing failed"); }
                }catch (Resources.NotFoundException e)
                {
                    Log.d(TAG,"Styles not found",e);
                }
                return true;
            case R.id.light:
                try {
                    boolean success =   mMap.setMapStyle
                            (MapStyleOptions.loadRawResourceStyle
                                    (this,R.raw.light_style));
                    if(!success)
                    {  Log.d(TAG,"Styles parsing failed"); }
                }catch (Resources.NotFoundException e)
                {
                    Log.d(TAG,"Styles not found",e);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Default Theme
    public void normal(GoogleMap mMap){
        try {
            boolean success =   mMap.setMapStyle
                    (MapStyleOptions.loadRawResourceStyle
                            (this,R.raw.map_style));
            if(!success)
            {  Log.d(TAG,"Styles parsing failed"); }
        }catch (Resources.NotFoundException e)
        {
            Log.d(TAG,"Styles not found",e);
        }

    }

}