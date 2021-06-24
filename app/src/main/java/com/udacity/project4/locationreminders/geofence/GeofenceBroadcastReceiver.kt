package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.errorMessage
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

private const val TAG = "GeofenceBroadcastReceiv"

class GeofenceBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val remindersRepository: ReminderDataSource by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent.hasError()) {
                val errorMessage = errorMessage(context, geofencingEvent.errorCode)
                Log.e(TAG, errorMessage)
                return
            }

            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, context.getString(R.string.geofence_entered))
                val fenceId = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences[0].requestId
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                //Get the location from the saved data source
                CoroutineScope(Dispatchers.IO).launch {
                    var reminderDTO: ReminderDTO? = null

                    val result = remindersRepository.getReminders()
                    when (result) {
                        is Result.Error -> {
                        }
                        is Result.Success -> {
                            result.data.let { it ->
                                if (it.isNotEmpty()) {
                                    val foundIndex = it.indexOfFirst { it.id == fenceId }

                                    val reminderResult =
                                        remindersRepository.getReminder(foundIndex.toString())
                                    when (reminderResult) {
                                        is Result.Error -> {
                                        }
                                        is Result.Success -> {
                                            reminderResult.data.let {
                                                reminderDTO = it
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        reminderDTO.let {
                            val reminderDataItem = ReminderDataItem(
                                reminderDTO!!.title,
                                reminderDTO!!.description,
                                reminderDTO!!.location,
                                reminderDTO!!.latitude,
                                reminderDTO!!.longitude,
                                reminderDTO!!.id
                            )
                            sendNotification(context, reminderDataItem)
                        }
                    }
                }
            }
        }
    }
}