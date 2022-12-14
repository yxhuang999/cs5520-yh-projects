package edu.northeastern.fall22_team34.carpool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.fall22_team34.R;
import edu.northeastern.fall22_team34.carpool.models.User;

public class LoginActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;

    private String username;
    private Boolean isLoggedIn = false;
    private List<String> currUsernames;
    private Location currLocation;

    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool_login);

        mDatabase = FirebaseDatabase.getInstance();

        currUsernames = new ArrayList<>();

        Button loginButton = findViewById(R.id.btn_login);
        EditText usernameEditText = findViewById(R.id.et_username);

        locationInit();

        newUserListener();

        loginListener(loginButton, usernameEditText);
    }

    private void locationInit() {
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                currLocation = location;
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);
            }
        }
    }

    // listen to new users added
    private void newUserListener() {
        mDatabase.getReference().child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {
                User user = snapshot.getValue(User.class);
                currUsernames.add(user.username);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,
                                       @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                currUsernames.remove(user.username);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loginListener(Button loginButton, EditText usernameEditText) {
        // create user profile with username and current location
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currLocation == null) {
                    Toast.makeText(getApplicationContext(), "Cannot Access Current Location, " +
                                    "Please Try Again",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                username = usernameEditText.getText().toString();
                if (username.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Username Cannot be Empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!currUsernames.contains(username)) {
                    createUser(username, currLocation);
                }

                Intent homeActivity = new Intent(getApplicationContext(), HomeActivity.class);
                homeActivity.putExtra("USERNAME", username);
                startActivity(homeActivity);
            }
        });
    }

    // add user to firebase
    private void createUser(String username, Location currLocation) {
        User user = new User(username, currLocation);
        mDatabase.getReference().child("users").child(username).setValue(user);
    }

    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }
}