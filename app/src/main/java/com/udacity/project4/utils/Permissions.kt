package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.udacity.project4.R

@TargetApi(29)
fun Fragment.checkLocationPermission() {
    if (!foregroundAndBackgroundLocationPermissionApproved()) {
        requestForegroundAndBackgroundLocationPermissions()
    }
}

@TargetApi(29)
fun Fragment.foregroundAndBackgroundLocationPermissionApproved(): Boolean {
    val foregroundLocationApproved = (
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
    val backgroundPermissionApproved =
        if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }
    return foregroundLocationApproved && backgroundPermissionApproved
}

@TargetApi(29)
fun Fragment.requestForegroundAndBackgroundLocationPermissions() {
    var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = when {
        runningQOrLater -> {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
        }
        else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
    }

    Log.d(TAG, "Request foreground only location permission")

    ActivityCompat.requestPermissions(
        requireActivity(),
        permissionsArray,
        resultCode
    )
}

fun Fragment.checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_LOW_POWER
    }
    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val settingsClient = LocationServices.getSettingsClient(requireContext())
    val locationSettingsResponseTask =
        settingsClient.checkLocationSettings(builder.build())
    locationSettingsResponseTask.addOnFailureListener { exception ->
        if (exception is ResolvableApiException && resolve) {
            try {
                exception.startResolutionForResult(
                    requireActivity(),
                    REQUEST_TURN_DEVICE_LOCATION_ON
                )
            } catch (sendEx: IntentSender.SendIntentException) {
                Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
            }
        } else {
            Toast.makeText(requireContext(), R.string.location_required_error, Toast.LENGTH_LONG)
                .show()
        }
    }
//    locationSettingsResponseTask.addOnCompleteListener {
//        //Exit the app
//        requireActivity().finish()
//    }
}

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

private val runningQOrLater =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q