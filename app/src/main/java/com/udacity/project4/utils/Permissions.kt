package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.udacity.project4.R

@TargetApi(29)
fun Fragment.checkLocationPermission() {
    if (!checkForegroundAndBackgroundLocationPermissionApproved()) {
        requestForegroundAndBackgroundLocationPermissions()
    }
}

@TargetApi(29)
fun Fragment.checkForegroundAndBackgroundLocationPermissionApproved(): Boolean {
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
    Log.d(TAG, "Request foreground only location permission")

    val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (!it.value) {
                    Toast.makeText(
                        requireContext(),
                        R.string.permission_denied_explanation,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    if (runningQOrLater) {
        permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
    }

    requestMultiplePermissions.launch(permissionsArray)
}

/**
 * This method will check if the GPS is enabled, if not request for it, else prompt at error
 */
fun Fragment.checkIfGpsIsEnabled(resolve: Boolean = true) {
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
}

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "Permissions"

private val runningQOrLater =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q