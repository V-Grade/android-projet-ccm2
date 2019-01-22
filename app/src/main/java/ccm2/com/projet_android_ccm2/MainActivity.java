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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener{

    LocationManager monLocationManager;

    private static final int ID_DEMANDE_PERMISSION = 123;

    private GoogleMap maGoogleMap;

    private MapFragment monMapFragment;

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

        Toast.makeText(this, "latitude=" + latitude + " - longitude=" + longitude, Toast.LENGTH_LONG).show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();

        data.put("Latitude", latitude);
        data.put("Longitude", longitude);

        db.collection("GPSLocation")
        .add(data)
        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("CUSTOM_LOG", "Location added with ID: " + documentReference.getId());
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("CUSTOM_LOG", "Error adding location", e);
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
