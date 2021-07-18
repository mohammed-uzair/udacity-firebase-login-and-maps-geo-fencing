package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeoFence
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.checkBackgroundLocationPermissionGranted
import com.udacity.project4.utils.checkForegroundLocationPermissionGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class SaveReminderFragment : BaseFragment() {
    companion object {
        private const val TAG = "SaveReminderFragment"
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var _reminderDataItem: ReminderDataItem

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private val requestGpsPermission = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            addReminderGeoFence(_reminderDataItem)
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            _viewModel.showToast.value = getString(R.string.location_required_error)
        }
    }

    private val requestForegroundAndBackgroundLocationPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        var isAnyPermissionDenied = false

        it.forEach { permission ->
            if (permission.key == Manifest.permission.ACCESS_FINE_LOCATION) {
                if (permission.value == false) {
                    _viewModel.showToast.value =
                        (getString(R.string.permission_foreground_denied_explanation))

                    isAnyPermissionDenied = true
                }
            }
            if (permission.key == Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
                if (permission.value == false) {
                    _viewModel.showToast.value =
                        (getString(R.string.permission_background_denied_explanation))

                    isAnyPermissionDenied = true
                }
            }
        }

        if (!isAnyPermissionDenied) {
            checkGps()
        }
    }

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
    }

    @SuppressLint("MissingPermission")
    private fun addReminderGeoFence(reminderDataItem: ReminderDataItem) {
        geofencingClient.addGeofences(
            GeoFence.getGeofencingRequest(reminderDataItem),
            geofencePendingIntent
        ).addOnSuccessListener {
            _viewModel.showSnackBar.value = (getString(R.string.reminder_added))

            // Save this reminder to the data source
            _viewModel.saveReminder(reminderDataItem)
            _viewModel.showToast.value = getString(R.string.reminder_saved)
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }.addOnFailureListener {
            _viewModel.showSnackBar.value = (getString(R.string.geofences_not_added))
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    private fun checkGps() {
        checkGpsEnabled().addOnCompleteListener {
            if (it.isSuccessful) {
                _reminderDataItem.let(this::addReminderGeoFence)
            } else {
                _viewModel.showToast.value = getString(R.string.location_required_save_error)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude
            val longitude = _viewModel.longitude.value

            val reminderDataItem = ReminderDataItem(
                title = title,
                description = description.value,
                location = location,
                latitude = latitude.value,
                longitude = longitude,
            )

            if (_viewModel.validateEnteredData(reminderDataItem)) {
                var permissionsArray = arrayOf<String>()
                if (!checkForegroundLocationPermissionGranted()) {
                    permissionsArray += Manifest.permission.ACCESS_FINE_LOCATION
                }
                if (!checkBackgroundLocationPermissionGranted()) {
                    permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                }

                this._reminderDataItem = reminderDataItem

                if (permissionsArray.isNotEmpty()) {
                    requestForegroundAndBackgroundLocationPermissions.launch(permissionsArray)
                } else {
                    checkGps()
                }
            }
        }
    }

    private fun checkGpsEnabled(): Task<LocationSettingsResponse> {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireContext())

        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    requestGpsPermission.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.location_required_error,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }

        return locationSettingsResponseTask
    }

    override fun onDestroy() {
        super.onDestroy()

        //Clear everything
        _viewModel.reminderTitle.value = ""
        _viewModel.reminderDescription.value = ""
        _viewModel.reminderSelectedLocationStr.value = ""
    }
}
