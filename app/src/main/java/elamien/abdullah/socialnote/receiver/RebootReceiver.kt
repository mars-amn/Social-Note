package elamien.abdullah.socialnote.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import elamien.abdullah.socialnote.services.GeofenceService
import elamien.abdullah.socialnote.utils.Constants
import pub.devrel.easypermissions.EasyPermissions


class RebootReceiver : BroadcastReceiver() {
	private val locationPermissions =
		arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

	override fun onReceive(context : Context?, intent : Intent?) {
		if (context != null && intent != null) {
			val action = intent.action
			if (action == Intent.ACTION_BOOT_COMPLETED && EasyPermissions.hasPermissions(context,
						*locationPermissions)) {
				startAddingGeofencesService(context)
			}
		}
	}

	private fun startAddingGeofencesService(context : Context) {
		val geofenceService = Intent(context.applicationContext, GeofenceService::class.java)
		geofenceService.action = Constants.RE_ADD_GEOFNECES_INTENT_ACTION
		GeofenceService.getGeofenceService()
				.enqueueQueryingAndAddingGeofencesJob(context.applicationContext, geofenceService)
	}

}