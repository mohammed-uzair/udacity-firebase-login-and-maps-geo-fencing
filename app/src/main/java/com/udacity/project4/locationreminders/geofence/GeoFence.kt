package com.udacity.project4.locationreminders.geofence

import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

const val GEOFENCE_RADIUS_IN_METERS = 100f

object GeoFence {
    private fun generateGeoFence(reminderDataItem: ReminderDataItem): Geofence {
        return Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId(reminderDataItem.id)

            // Set the circular region of this geofence.
            .setCircularRegion(
                reminderDataItem.latitude ?: 0.0,
                reminderDataItem.longitude ?: 0.0,
                GEOFENCE_RADIUS_IN_METERS
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(Geofence.NEVER_EXPIRE)

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

            // Create the geofence.
            .build()
    }

    fun getGeofencingRequest(reminderDataItem: ReminderDataItem): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(generateGeoFence(reminderDataItem))
        }.build()
    }

    fun getGeofencingRequest(reminderDataItem: List<ReminderDataItem>): GeofencingRequest {
        val geoFences = mutableListOf<Geofence>()

        reminderDataItem.forEach { geoFences.add(generateGeoFence(it)) }

        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geoFences)
        }.build()
    }
}