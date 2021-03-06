package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.checkForegroundLocationPermissionGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class SelectLocationFragment : BaseFragment(), KoinComponent, OnMapReadyCallback {
    companion object {
        private const val TAG = "SelectLocationFragment"
        private const val DEFAULT_LATITUDE = 37.422160
        private const val DEFAULT_LONGITUDE = -122.084270
        private const val DEFAULT_ZOOM_LEVEL = 15f
    }

    private lateinit var map: GoogleMap
    private val remindersRepository: ReminderDataSource by inject()
    private var marker: Marker? = null

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel : SaveReminderViewModel by sharedViewModel()
    private lateinit var mapView: MapView
    private lateinit var binding: FragmentSelectLocationBinding

    @SuppressLint("MissingPermission")
    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                Toast.makeText(
                    requireContext(),
                    R.string.permission_foreground_denied_explanation,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (this::map.isInitialized) {
                    map.isMyLocationEnabled = true
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        mapView = binding.root.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)

        return binding.root
    }

    private fun setMapStyle() {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )
        } catch (exception: NotFoundException) {
            Log.e(TAG, "setMapStyle: ${exception.message}", exception)
        }
    }

    private fun onLocationSelected(latLng: LatLng, name: String = "") {
        _viewModel.latitude.value = latLng.latitude
        _viewModel.longitude.value = latLng.longitude
        _viewModel.reminderSelectedLocationStr.value =
            if (name.isBlank()) "${latLng.latitude}, ${latLng.longitude}" else name
        findNavController().popBackStack()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        this.map = map

        setMapStyle()

        val homeLatLng = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, DEFAULT_ZOOM_LEVEL))

        if (!checkForegroundLocationPermissionGranted()) {
            permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            map.isMyLocationEnabled = true
        }

        //Get all the saved locations
        lifecycleScope.launch(Dispatchers.IO) {
            val reminders = remindersRepository.getReminders()
            reminders.let { it ->
                when (it) {
                    is Result.Error -> {
                    }
                    is Result.Success -> {
                        it.data.let { reminderDtos ->
                            reminderDtos.forEach { reminderDto ->
                                withContext(Dispatchers.Main) {
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(
                                                LatLng(
                                                    reminderDto.latitude ?: 0.0,
                                                    reminderDto.longitude ?: 0.0
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        map.setOnPoiClickListener {
            addMarkerOnCurrentSelectedLocation(it.latLng)

            //Ask the user for confirmation
            showConfirmationAlert(
                R.string.save_location_confirmation_title,
                R.string.save_location_confirmation_message
            ) { _, _ ->
                //Save the user marked location
                onLocationSelected(it.latLng, it.name)
            }
        }

        map.setOnMapClickListener {
            addMarkerOnCurrentSelectedLocation(it)

            //Ask the user for confirmation
            showConfirmationAlert(
                R.string.save_location_confirmation_title,
                R.string.save_location_confirmation_message
            ) { _, _ ->
                //Save the user marked location
                onLocationSelected(it)
            }
        }
    }

    private fun addMarkerOnCurrentSelectedLocation(latLng: LatLng) {
        if (this::map.isInitialized) {
            // Clear the old marker
            marker?.remove()

            // Add the marker
            marker =
                map.addMarker(MarkerOptions().position(LatLng(latLng.latitude, latLng.longitude)))
        }
    }
}