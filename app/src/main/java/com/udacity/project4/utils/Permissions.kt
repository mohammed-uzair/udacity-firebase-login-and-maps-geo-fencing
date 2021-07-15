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
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.udacity.project4.R

@TargetApi(29)
fun Fragment.checkLocationPermissions() {
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
                        R.string.permission_foreground_denied_explanation,
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

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "Permissions"

private val runningQOrLater =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

fun Fragment.checkGpsEnabled(resolve: Boolean = true): Task<LocationSettingsResponse> {
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

    return locationSettingsResponseTask
}

fun Fragment.checkForegroundLocationPermissionGranted() = (
        PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ))

fun Fragment.requestForegroundLocationPermission(): Boolean {
    registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (!it) {
            Toast.makeText(
                requireContext(),
                R.string.permission_foreground_denied_explanation,
                Toast.LENGTH_LONG
            ).show()
        } else {
            //Start all the geofencing
            _viewModel.allReminders.observe(this) { reminders ->
                if (reminders.isNotEmpty())
                    startGeoFencing(reminders)
            }
        }
    }.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
}

fun Fragment.checkBackgroundLocationPermissionGranted(): Boolean {
    if (runningQOrLater) {
        return PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
    }

    return true
}

fun Fragment.requestBackgroundLocationPermission() {

}