package com.example.locationpractise

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.locationpractise.Base.BaseActivity
import java.util.jar.Manifest

class MainActivity : BaseActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!isGPSPermissionAllowed()){
            requestPermission()
        }

    }

    val requestGPSPermissionLauncher =
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

    fun getUserLocation(){
        Toast.makeText(this,"we can access user location",Toast.LENGTH_LONG).show()

    }
}