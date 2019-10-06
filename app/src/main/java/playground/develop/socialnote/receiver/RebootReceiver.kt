package playground.develop.socialnote.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import playground.develop.socialnote.services.GeofenceService
import playground.develop.socialnote.services.TimeReminderService
import playground.develop.socialnote.utils.Constants
import pub.devrel.easypermissions.EasyPermissions


class RebootReceiver : BroadcastReceiver() {
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val action = intent.action
            if (action == Intent.ACTION_BOOT_COMPLETED && EasyPermissions.hasPermissions(
                    context,
                    *locationPermissions
                )
            ) {
                startAddingGeofencesService(context)
            }
            if (action == Intent.ACTION_BOOT_COMPLETED) {
                startAddingTimeReminderNotes(context)
            }
        }
    }

    private fun startAddingTimeReminderNotes(context: Context) {
        val timeReminderService = Intent(
            context.applicationContext,
            TimeReminderService::class.java
        )
        timeReminderService.action = Constants.RE_ADD_TIME_REMINDER_INTENT_ACTION
        TimeReminderService.getTimeReminderService()
            .enqueueReminderNotes(context.applicationContext, timeReminderService)
    }

    private fun startAddingGeofencesService(context: Context) {
        val geofenceService = Intent(context.applicationContext, GeofenceService::class.java)
        geofenceService.action = Constants.RE_ADD_GEOFNECES_INTENT_ACTION
        GeofenceService.getGeofenceService()
            .enqueueQueryingAndAddingGeofencesJob(context.applicationContext, geofenceService)
    }

}