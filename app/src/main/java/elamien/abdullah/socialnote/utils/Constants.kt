package elamien.abdullah.socialnote.utils

/**
 * Created by AbdullahAtta on 7/24/2019.
 */
class Constants {

	companion object {
		/**
		 * Intent & Notification Keys
		 */
		const val NOTE_NOTIFICATION_TEXT_INTENT_KEY = "elamien.abdullah.socialnote.note_notification_body_intent_key"
		const val ACTIVITY_NOTE_TIMER_NOTIFICATION_OPEN = "elamien.abdullah.socialnote.notification_open_activity"
		const val NOTE_TIME_REMINDER_ACTION = "elamien.abdullah.socialnote.open_note_notification_action"
		const val DISMISS_NOTE_TIME_REMINDER_NOTIFICATION = "elamien.abdullah.socialnote.dimiss_notification"
		const val NOTE_INTENT_KEY = "elamien.abdullah.socialnote.note_intent_key"
		const val NOTE_INTENT_ID = "elamien.abdullah.socialnote.note_intent_id_key"


		/**
		 * EventBust
		 */
		const val AUTH_EVENT_FAIL = "elamien.abdullah.socialnote.auth_fail"
		const val AUTH_EVENT_SUCCESS = "elamien.abdullah.socialnote.auth_success"

		/**
		 * Geofence related constants
		 */
		const val ACTIVITY_NOTE_GEOFENCE_NOTIFICATION_OPEN =
			"elamien.abdullah.socialnote.geofence_notification_open_activity"
		const val DISMISS_NOTE_GEOFENCE_NOTIFICATION = "elamien.abdullah.socialnote.dimiss_geofence_notification"
		const val NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY =
			"elamien.abdullah.socialnote.note_geofence_latlng_intent_key"
		const val NOTE_GEOFENCE_REMINDER_ACTION = "elamien.abdullah.socialnote.note_geofence_action"
		const val NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY = "elamien.abdullah.socialnote.note_geofence_id"
		const val GEOFENCE_REMINDER_RADIUS = 300.0f
		const val GEOFENCE_REMINDER_MAP_RADIUS = GEOFENCE_REMINDER_RADIUS.toDouble()
		const val GEOFENCE_EXPIRE_DATE = 2160000000L // 25 days
	}

}