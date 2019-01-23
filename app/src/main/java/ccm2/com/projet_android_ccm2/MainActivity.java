package ccm2.com.projet_android_ccm2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import android.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener{

    LocationManager monLocationManager;

    private static final int ID_DEMANDE_PERMISSION = 123;

    private GoogleMap maGoogleMap;

    private MapFragment monMapFragment;

    private String cleanDate;

    private Double getLat;

    private Double getLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager monFragmentManager = getFragmentManager();

        monMapFragment = (MapFragment) monFragmentManager.findFragmentById(R.id.mamap);

        verifierPermission();

    }

    private void verifierPermission() {
        if (!(
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                        &&

                        ActivityCompat.checkSelfPermission(
                                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}
                    , ID_DEMANDE_PERMISSION);
            return;

        }

        monLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        monLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);

        monLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);

        chargerMap();
    }


    @Override
    protected void onResume() {
        super.onResume();
        verifierPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (monLocationManager != null)
            monLocationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ID_DEMANDE_PERMISSION) {

        }
    }

    private double latitude;
    private double longitude;

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        //GET DATE
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date rowDate = new Date();
        String sendDate = timeStampFormat.format(rowDate);

        Toast.makeText(this, "latitude=" + latitude + " - longitude=" + longitude, Toast.LENGTH_LONG).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();

        //PUSH DATA
        data.put("Latitude", latitude);
        data.put("Longitude", longitude);
        data.put("Date", sendDate);

        db.collection("GPSLocation")
        .add(data)
        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("PUSH_DATA", "Location added with ID: " + documentReference.getId());
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("PUSH_DTA", "Error adding location", e);
            }
        });


        //GET DATA
        db.collection("GPSLocation")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d("GET_DATA", document.getId() + " => " + document.getData());

                            JSONObject jObject = new JSONObject(document.getData());
                            try {
                                getLat = jObject.getDouble("Latitude");
                                getLong = jObject.getDouble("Longitude");
                                cleanDate = jObject.getString("Date");

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("GET_DATA", "Error parsing JSON");
                            }


                            Marker marker = maGoogleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(getLat, getLong))
                                    .title("Test")
                                    .snippet(cleanDate));
                        }

                    } else {
                        Log.w("GET_DATA", "Error getting location data", task.getException());
                    }
                }
            });

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void chargerMap() {
        monMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                maGoogleMap = googleMap;

                maGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));

                if (ActivityCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
                maGoogleMap.setMyLocationEnabled(true);
            }
        });
    }
}
