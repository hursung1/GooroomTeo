package edu.skku.GooroomTeo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
    Button addbtn;
    Button findbtn;
    GoogleMap map;
    double lon;
    double lat;
    double comlon;
    double comlat;
    double shortlon;
    double shortlat;
    String [] splitstring;
    String getFromFire;
    double shortest;
    String shortestPlace;
    private DatabaseReference mPostReference;
    ArrayList<String> data;
    double [] testarray;
    int a;
    String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getFragmentManager();
        data = new ArrayList<String>();
        MapFragment mapFragment = (MapFragment) fragmentManager

                .findFragmentById(R.id.map);
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

        Log.d("before mark", "tag" );
        //지도에 마커표시하기
        getFirebaseDatabase();
        for (int i=0; i<data.size(); i++){
            getFromFire=data.get(i);
            splitstring=getFromFire.split(":");
            comlon=Double.valueOf(splitstring[0]).doubleValue();
            comlat=Double.valueOf(splitstring[1]).doubleValue();
            title=splitstring[2];
            LatLng marker=new LatLng(comlat,comlon);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(marker);
            markerOptions.title(title);
            map.addMarker(markerOptions);


        }

        Log.d("after mark", "tag" );

        addbtn = findViewById(R.id.addcontent);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }

        });
        findbtn = findViewById(R.id.findnear);
        findbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng MyPos = new LatLng(lat, lon);
                Location current=new Location("myCurrent");
                current.setLatitude(lat);
                current.setLongitude(lon);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(MyPos);
                markerOptions.title("현위치");
                map.addMarker(markerOptions);
                map.moveCamera(CameraUpdateFactory.newLatLng(MyPos));
                map.animateCamera(CameraUpdateFactory.zoomTo(16));

                //데이터 받아와서 거리계산하기
                getFirebaseDatabase();
                shortest=100000000;
                for (int i=0; i<data.size(); i++){
                    getFromFire=data.get(i);
                    splitstring=getFromFire.split(":");
                    comlon=Double.valueOf(splitstring[0]).doubleValue();
                    comlat=Double.valueOf(splitstring[1]).doubleValue();
                    Location comPos=new Location("comPos");
                    comPos.setLatitude(comlat);
                    comPos.setLongitude(comlon);

                    double distance=current.distanceTo(comPos);
                    if (shortest>distance){
                        double temp=distance;
                        shortest=temp;
                        a=i;
                    }
                }
                shortestPlace=data.get(a);
                splitstring=shortestPlace.split(":");
                shortlat=Double.valueOf(splitstring[1]).doubleValue();
                shortlon=Double.valueOf(splitstring[0]).doubleValue();
                LatLng Shortplace = new LatLng(lat, lon);
                map.moveCamera(CameraUpdateFactory.newLatLng(Shortplace));
                map.animateCamera(CameraUpdateFactory.zoomTo(16));

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
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        map.addMarker(markerOptions);
        map.setOnMarkerClickListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    public boolean onMarkerClick(Marker marker) {

        Intent intent = new Intent(MainActivity.this, RateActivity.class);
        String title = marker.getTitle();
        intent.putExtra("information", title);
        startActivity(intent);

        return true;

    }


    public void getFirebaseDatabase() {
        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("onDataChange", "Data is Updated");
                data.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    FirebasePost get = postSnapshot.getValue(FirebasePost.class);
                    String[] info = {String.valueOf(get.longitude), String.valueOf(get.latitude), get.name};
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
}
