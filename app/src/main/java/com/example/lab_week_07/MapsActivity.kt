package com.example.lab_week_07

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import android.location.Location
import com.google.android.gms.location.LocationServices

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.lab_week_07.databinding.ActivityMapsBinding



class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // This is the variable through which we will launch the permission request and track user responses
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    //A google play location service which helps us interact with Google's Fused  Location Provider API
    //The API intelligently provides us with the device location information
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // ==============================
        // Register for activity result (permission request handler)
        // ==============================
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // If granted by the user, execute the necessary function
                    getLastLocation()
                } else {
                    // If not granted, show a rationale dialog
                    // A rationale dialog is used to warn the user that the app
                    // may not work correctly without the required permission
                    showPermissionRationale {
                        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    }
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //OnMapReady is called when the map is ready to be used
        //The code below is used to check for the location permission for the map functionality to work
        //If it's not granted yet, then the rationale dialog will be brought up
        when {
            hasLocationPermission() -> getLastLocation()
            //shouldShowRequestPermissionRationale automatically checks if the user has denied the permission before
                    //If it has, then the rationale dialog will be brought up
                    shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher
                        .launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher
                .launch(ACCESS_FINE_LOCATION)
        }
    }

    //Executed when the location permission has been granted by the user
    private fun getLastLocation() {
        if (hasLocationPermission()) {
            try {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val userLocation = LatLng(location.latitude,
                                location.longitude)
                            updateMapLocation(userLocation)
                            addMarkerAtLocation(userLocation, "You")
                        }
                    }
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "SecurityException: ${e.message}")
            }
        } else {
            // If permission was rejected
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            location, 7f))
    }
    private fun addMarkerAtLocation(location: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().title(title)
            .position(location))
    }

    //This is used to bring up a rationale dialog which will be used to ask the user for permission again
    //A rationale dialog is used for a warning to the user that the app will now work without the required permission
    //Usually it's brought up when the user denies the needed permission in the previous permission request
    // ===================================
    // Helper function to show rationale dialog (stub)
    // ===================================

    //This is used to check if the user already has the permission granted
    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    private fun showPermissionRationale(positiveAction: () -> Unit) {
        //Create a pop up alert dialog that's used to ask for the required permission  again to the user
        AlertDialog.Builder(this)
            .setTitle("Location permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss()
            }
            .create().show()
    }
}