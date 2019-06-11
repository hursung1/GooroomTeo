package edu.skku.GooroomTeo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private DatabaseReference mPostReference;
    private Button addbtn;
    private Button findbtn;
    private Button Firebtn;
    private Button currentbtn;
    private GoogleMap map;
    private Location current;

    private double lon;
    private double lat;
    private double comlon;
    private double comlat;
    private double shortlon;
    private double shortlat;
    private double shortest;
    private String currentLocation;
    private String title;
    private String getFromFire;
    private String[] splitstring;
    private String shortestPlace;

    private ArrayList<String> data;

    private double[] testarray;
    private int a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        data = new ArrayList<>();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //현재위치에서 시작

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {

            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    100,
                    0,
                    networkLocationListener);
        }
        mPostReference = FirebaseDatabase.getInstance().getReference();

        Log.d("before mark", "tag");
        //지도에 마커표시하기


        Log.d("after mark", "tag");

        addbtn = findViewById(R.id.addbtn);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }

        });

        //파이어베이스에서 데이터 가져오기
        Firebtn = findViewById(R.id.getFirebase);
        Firebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getFirebaseDatabase();
                for (int i = 0; i < data.size(); i++) {
                    getFromFire = data.get(i);
                    splitstring = getFromFire.split(":");
                    comlon = Double.valueOf(splitstring[0]).doubleValue();
                    comlat = Double.valueOf(splitstring[1]).doubleValue();
                    title = splitstring[2];
                    LatLng marker = new LatLng(comlat, comlon);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(marker);
                    markerOptions.title(title);
                    map.addMarker(markerOptions);
                    map.setOnMarkerClickListener(MainActivity.this);

                }
            }
        });
        currentbtn = findViewById(R.id.curbtn);
        currentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LatLng MyPos = new LatLng(lat, lon);
                Location current = new Location("myCurrent");
                current.setLatitude(lat);
                current.setLongitude(lon);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(MyPos);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                markerOptions.alpha(0.5f);
                markerOptions.title("현위치");
                map.addMarker(markerOptions);

                map.moveCamera(CameraUpdateFactory.newLatLng(MyPos));
                map.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        });

        findbtn = findViewById(R.id.findnear);
        findbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getFirebaseDatabase();
                Location current = new Location("myCurrent");
                current.setLatitude(lat);
                current.setLongitude(lon);
                //데이터 받아와서 거리계산하기
                shortest = 100000000;
                for (int i = 0; i < data.size(); i++) {
                    getFromFire = data.get(i);
                    splitstring = getFromFire.split(":");
                    comlon = Double.valueOf(splitstring[0]).doubleValue();
                    comlat = Double.valueOf(splitstring[1]).doubleValue();
                    Location comPos = new Location("comPos");
                    comPos.setLatitude(comlat);
                    comPos.setLongitude(comlon);

                    double distance = current.distanceTo(comPos);
                    if (shortest > distance) {
                        double temp = distance;
                        shortest = temp;
                        a = i;
                    }
                }
                shortestPlace = data.get(a);
                splitstring = shortestPlace.split(":");
                shortlat = Double.valueOf(splitstring[1]).doubleValue();
                shortlon = Double.valueOf(splitstring[0]).doubleValue();
                LatLng Shortplace = new LatLng(shortlat, shortlon);

                map.setOnMarkerClickListener(MainActivity.this);
                map.moveCamera(CameraUpdateFactory.newLatLng(Shortplace));
                map.animateCamera(CameraUpdateFactory.zoomTo(18));

            }
        });

        //Get User comment from firebase database
        mPostReference.child("locinfo").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String key = dataSnapshot.getKey();
                //System.out.println(key);
                FirebasePost get = dataSnapshot.getValue(FirebasePost.class);
                String[] info = {String.valueOf(get.longitude), String.valueOf(get.latitude), key};
                String result = info[0] + ":" + info[1] + ":" + info[2];
                data.add(result);
                System.out.println(info[0] + "\t" + info[1] + "\t" + key);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    final LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String provider = location.getProvider();
            lon = location.getLongitude();
            lat = location.getLatitude();
            Log.d("Update Network", "" + provider + " " + lon + " " + lat);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;

        LatLng SEOUL = new LatLng(37.56, 126.97);
        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));

    }

    public boolean onMarkerClick(Marker marker) {

        Intent intent = new Intent(MainActivity.this, RateActivity.class);
        String title = marker.getTitle();
        currentLocation="현위치";

        if (!title.equals(currentLocation)){
            intent.putExtra("information", title);
            startActivity(intent);

        }
        return true;

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }


/*
    public void getFirebaseDatabase() {
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("onDataChange", "Data is Updated");
                data.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
                    FirebasePost get = postSnapshot.getValue(FirebasePost.class);
                    String[] info = {String.valueOf(get.longitude), String.valueOf(get.latitude), key};
                    String result = info[0] + " : " + info[1] + " : " + info[2];
                    data.add(result);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mPostReference.child("locinfo").addValueEventListener(postListener);

    }
    */
}
