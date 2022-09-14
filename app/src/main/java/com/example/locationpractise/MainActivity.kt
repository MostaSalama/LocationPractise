package com.example.locationpractise


import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.locationpractise.Base.BaseActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

class MainActivity : BaseActivity() , OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
     var googleMap: GoogleMap? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContentView(R.layout.activity_main)

        if(isGPSPermissionAllowed()){
            getUserLocation()
        }else{
            requestPermission()
        }

    }

    var userLocation : Location?=null
    var userMarker : Marker?=null
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        drawUserMarkerOnMap()
    }

    fun drawUserMarkerOnMap(){
        val latLng = LatLng(userLocation?.latitude?:0.0,userLocation?.longitude?:0.0)

        if (userMarker==null){
            var markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("Current Location")
            userMarker = googleMap?.addMarker(markerOptions)

        }else
            userMarker?.position = latLng

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16.0f))
    }

    private val requestGPSPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
            } else {
                showDialog("we can't get the nearest drivers to you, " +
                "to use this feature please allow location service")
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermission(){
        if(shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)){
            showDialog(message = "Please enable location permission to get you the newest drivers",
                        "Yes",
                        posAction = { dialogInterface, i->
                            dialogInterface.dismiss()
                            requestGPSPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

                        },
                        negActionName = "No",
                        negAction = { dialogInterface, i->
                            dialogInterface.dismiss()
                        })


        }else{
            requestGPSPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)


        }
    }





    fun isGPSPermissionAllowed():Boolean{

        return ContextCompat.checkSelfPermission(this,
        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val locationCallback:LocationCallback = object :LocationCallback(){

        override fun onLocationResult(location: LocationResult) {

            for (location in location.locations){
                Log.e("location updated",""+location.latitude + " "+location.longitude)
                userLocation = location
                drawUserMarkerOnMap()
            }
        }
    }


    val locationRequest = create().apply {
            interval = 10000
            fastestInterval = 3000
            priority = LocationRequest.QUALITY_HIGH_ACCURACY
    }

    val REQUEST_CHECK_SETTINGS = 120
    @SuppressLint("MissingPermission")
    fun getUserLocation(){
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,
                Looper.getMainLooper())
            Toast.makeText(this,"Location is enabled !",Toast.LENGTH_LONG).show()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }






    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CHECK_SETTINGS){
                if(requestCode== RESULT_OK){
                    getUserLocation()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}