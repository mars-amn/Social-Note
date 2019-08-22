package elamien.abdullah.socialnote.utils

/**
 * Created by AbdullahAtta on 7/24/2019.
 */
class Constants {

	companion object {


		/**
		 * Intent & Notification Keys
		 */
		const val NOTE_NOTIFICATION_TEXT_INTENT_KEY =
			"elamien.abdullah.socialnote.note_notification_body_intent_key"
		const val ACTIVITY_NOTE_TIMER_NOTIFICATION_OPEN =
			"elamien.abdullah.socialnote.notification_open_activity"
		const val NOTE_TIME_REMINDER_ACTION =
			"elamien.abdullah.socialnote.open_note_notification_action"
		const val DISMISS_NOTE_TIME_REMINDER_NOTIFICATION =
			"elamien.abdullah.socialnote.dimiss_notification"
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
		const val DISMISS_NOTE_GEOFENCE_NOTIFICATION =
			"elamien.abdullah.socialnote.dimiss_geofence_notification"
		const val NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY =
			"elamien.abdullah.socialnote.note_geofence_latlng_intent_key"
		const val NOTE_GEOFENCE_REMINDER_ACTION = "elamien.abdullah.socialnote.note_geofence_action"
		const val NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY =
			"elamien.abdullah.socialnote.note_geofence_id"
		const val GEOFENCE_REMINDER_RADIUS = 300.0f
		const val GEOFENCE_REMINDER_MAP_RADIUS = GEOFENCE_REMINDER_RADIUS.toDouble()
		const val GEOFENCE_EXPIRE_DATE = 2160000000L // 25 days

		/**
		 * Geofence & Time reminder JobIntentService related constants
		 */
		const val RE_ADD_GEOFNECES_INTENT_ACTION = "elamien.abdullah.socialnote.re_add_geofences"
		const val GEOFENCE_RETRIEVER_INTENT_JOB_ID = 3
		const val RE_ADD_TIME_REMINDER_INTENT_ACTION =
			"elamien.abdullah.socialnote.re_add_time_reminders"
		const val TIME_REMINDER_INTENT_JOB_ID = 10

		/**
		 * Sync related constants
		 */
		const val SYNC_NEW_NOTE_INTENT_ACTION = "elamien.abdullah.socialnote.sync_new_note"
		const val SYNC_ALL_NOTES_INTENT_ACTION = "elamien.abdullah.socialnote.sync_all_notes"
		const val SYNC_NEEDED_UPDATES_NOTES_INTENT_ACTION =
			"elamien.abdullah.socialnote.sync_notes_need_update"
		const val SYNC_NOTE_ID_INTENT_KEY = "elamien.abdullah.socialnote.sync_note_id_intent_key"
		const val SYNC_NOTE_SERVICE_JOB_ID = 8

		/**
		 * Firebase related constants
		 */
		const val FIRESTORE_SYNCED_NOTES_COLLECTION_NAME = "SocialNote"
		const val FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME = "SyncedNotes"
		const val FIRESTORE_SYNCED_NOTE_BODY = "NoteBody"
		const val FIRESTORE_SYNCED_NOTE_TITLE = "NoteTitle"
		const val FIRESTORE_SYNCED_NOTE_DATE_CREATED = "DateCreated"
		const val FIRESTORE_SYNCED_NOTE_DATE_MODIFIED = "DateModified"
		const val FIRESTORE_SYNCED_NOTE_LOCATION_REMINDER = "GeofenceLocation"
		const val FIRESTORE_SYNCED_NOTE_TIME_REMINDER = "TimeReminder"
	}

}