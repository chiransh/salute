package com.codebusters.user;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;

public class Main2Activity extends AppCompatActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks {
    DatabaseReference dref, dref1;
    Double jsonlat, jsonlong;
    FirebaseAuth mauth;
    GoogleMap map;
    Double ab;
    int i = 0, index = 0, flag = 0;
    Double a[];
    Double lat[], lan[];
    Marker mPositionMarker;
    String key[];
    Button req;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dref = FirebaseDatabase.getInstance().getReference().child("Ambulance");
        dref1 = FirebaseDatabase.getInstance().getReference().child("User");
        jsonlat=new Double(0);
        jsonlong=new Double(0);
        mauth = FirebaseAuth.getInstance();
        lat = new Double[10];
        lan = new Double[10];
        mPositionMarker=null;
        key = new String[10];
        a = new Double[10];
        ab = new Double(0);
        setContentView(R.layout.activity_main2);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        req = (Button) findViewById(R.id.requestbtn);
        req.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag = 1;
                ab = a[0];
                index = 0;
                for (int i = 0; a[i] != null && i < a.length; i++) {
                    if (a[i] < ab) {
                        ab = a[i];
                        index = i;
                    }
                }
                dref.child(key[index]).child("usercalled").setValue(mauth.getCurrentUser().getUid());
                dref1.child(mauth.getCurrentUser().getUid()).setValue(key[index]);
                new AlertDialog.Builder(getApplicationContext()).setTitle("Ambulance Successfully Assigned").create().show();



            }
        });
//    dref= FirebaseDatabase.getInstance().getReference().child("Ambulance");
//        dref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot childsnapshot:dataSnapshot.getChildren()){
//                    String key=childsnapshot.getKey();
//
//                     lat[i] =(Double)(dataSnapshot.child(key).child("Latitude").getValue());
//                    lan[i] =(Double)(dataSnapshot.child(key).child("Longitude").getValue());
//
//
//
//
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
//        LatLng sydney = new LatLng(-33.852, 151.211);
//        googleMap.addMarker(new MarkerOptions().position(sydney)
//                .title("Marker in Sydney"));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        LatLng abc = new LatLng(-33.333, 151.245);
//        googleMap.addMarker(new MarkerOptions().position(abc)
//                .title("Marker in Sydney"));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(abc));
        final LatLng abc = new LatLng(28.589589, 77.314461);
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (flag == 0) {
                    for (DataSnapshot childsnapshot : dataSnapshot.getChildren()) {
                        key[i] = childsnapshot.getKey();

                        lat[i] = (Double) (dataSnapshot.child(key[i]).child("Latitude").getValue());
                        lan[i] = (Double) (dataSnapshot.child(key[i]).child("Longitude").getValue());
                        LatLng sydney = new LatLng(lat[i], lan[i]);
                        googleMap.addMarker(new MarkerOptions().position(sydney)
                                .title(""));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


                        a[i] = (Double) CalculationByDistance(abc, sydney);
                        Log.d("ABC", String.valueOf(a[i]));
                        i++;


                    }
                }
                else
                {
                    new abcd().execute("https://login-df695.firebaseio.com/Ambulance/" + key[index] + "/.json");
                    LatLng latlng = new LatLng(jsonlat, jsonlong);
                    Location location = new Location(String.valueOf(latlng));
                    if (mPositionMarker == null) {

                        mPositionMarker = googleMap.addMarker(new MarkerOptions()
                                .flat(true)
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.amb))
                                .anchor(0.5f, 0.5f)
                                .position(latlng));
                    }
                    animateMarker(mPositionMarker, location); // Helper method for smooth
                    // animation

                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location
                            .getLatitude(), location.getLongitude())));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    public void animateMarker(final Marker marker, final Location location) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final double startRotation = marker.getRotation();
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                double lng = t * location.getLongitude() + (1 - t)
                        * startLatLng.longitude;
                double lat = t * location.getLatitude() + (1 - t)
                        * startLatLng.latitude;

                float rotation = (float) (t * location.getBearing() + (1 - t)
                        * startRotation);

                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation(rotation);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }


    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    class abcd extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection con = null;
            jsonlat = new Double(0);
            jsonlong = new Double(0);
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.connect();
                InputStream stram = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stram));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String jsonStr = buffer.toString();
                JSONObject jsonObj = new JSONObject(jsonStr);
                jsonlat = Double.valueOf((jsonObj.getString("Latitude")));
                jsonlong = Double.valueOf((jsonObj.getString("Longitude")));
                StringBuffer readData = new StringBuffer();
                return null;
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}