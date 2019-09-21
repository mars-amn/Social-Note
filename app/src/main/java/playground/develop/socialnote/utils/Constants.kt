package playground.develop.socialnote.utils

/**
 * Created by AbdullahAtta on 7/24/2019.
 */
class Constants {

    companion object {

        const val APP_PREFERENCE_NAME = "playground.develop.socialnote"
        const val FIRST_LAUNCH_KEY = "playground.develop.socialnote.utils.first_launch_key"
        const val SKIP_REGISTER_KEY = "playground.develop.socialnote.utils.skip_register_key"
        const val CONSIDER_REGISTER_KEY = "playground.develop.socialnote.utils.consider_register_key"
        const val USER_COUNTRY_ISO_KEY = "playground.develop.socialnote.utils.user_country_iso_key"
        const val USER_COUNTRY_ISO_ERROR_KEY = "not-available"
        /**
         * Intent & Notification Keys
         */
        const val NOTE_NOTIFICATION_TEXT_INTENT_KEY = "playground.develop.socialnote.note_notification_body_intent_key"
        const val ACTIVITY_NOTE_TIMER_NOTIFICATION_OPEN = "playground.develop.socialnote.notification_open_activity"
        const val NOTE_TIME_REMINDER_ACTION = "playground.develop.socialnote.open_note_notification_action"
        const val DISMISS_NOTE_TIME_REMINDER_NOTIFICATION = "playground.develop.socialnote.dimiss_notification"
        const val NOTE_INTENT_KEY = "playground.develop.socialnote.note_intent_key"
        const val NOTE_INTENT_ID = "playground.develop.socialnote.note_intent_id_key"


        /**
         * EventBust
         */
        const val AUTH_EVENT_FAIL = "playground.develop.socialnote.auth_fail"
        const val AUTH_EVENT_SUCCESS = "playground.develop.socialnote.auth_success"

        /**
         * Geofence related constants
         */
        const val ACTIVITY_NOTE_GEOFENCE_NOTIFICATION_OPEN = "playground.develop.socialnote.geofence_notification_open_activity"
        const val DISMISS_NOTE_GEOFENCE_NOTIFICATION = "playground.develop.socialnote.dimiss_geofence_notification"
        const val NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY = "playground.develop.socialnote.note_geofence_latlng_intent_key"
        const val NOTE_GEOFENCE_REMINDER_ACTION = "playground.develop.socialnote.note_geofence_action"
        const val NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY = "playground.develop.socialnote.note_geofence_id"
        const val GEOFENCE_REMINDER_RADIUS = 300.0f
        const val GEOFENCE_REMINDER_MAP_RADIUS = GEOFENCE_REMINDER_RADIUS.toDouble()
        const val GEOFENCE_EXPIRE_DATE = 2160000000L // 25 days

        /**
         * Geofence & Time reminder JobIntentService related constants
         */
        const val RE_ADD_GEOFNECES_INTENT_ACTION = "playground.develop.socialnote.re_add_geofences"
        const val GEOFENCE_RETRIEVER_INTENT_JOB_ID = 3
        const val RE_ADD_TIME_REMINDER_INTENT_ACTION = "playground.develop.socialnote.re_add_time_reminders"
        const val TIME_REMINDER_INTENT_JOB_ID = 10

        /**
         * Sync related constants
         */
        const val SYNC_UPDATE_NOTE_INTENT_ACTION = "playground.develop.socialnote.sync_update_note"
        const val SYNC_ALL_NOTES_INTENT_ACTION = "playground.develop.socialnote.sync_all_notes"
        const val SYNC_NEEDED_UPDATES_NOTES_INTENT_ACTION = "playground.develop.socialnote.sync_notes_need_update"
        const val SYNC_CALL_NOTES_POPULATE_ROOM_INTENT_ACTION = "playground.develop.socialnote.get_notes_remote_populate_room"
        const val SYNC_NEW_NOTE_INTENT_ACTION = "playground.develop.socialnote.sync_new_note"
        const val SYNC_DELETE_NOTE_INTENT_ACTION = "playground.develop.socialnote.sync_delete_note"
        const val SYNC_NOTE_ID_INTENT_KEY = "playground.develop.socialnote.sync_note_id_intent_key"
        const val SYNC_NOTE_SERVICE_JOB_ID = 8
        /**
         * Firebase syncing related constants
         */
        const val FIRESTORE_SYNCED_NOTES_COLLECTION_NAME = "SocialNote"
        const val FIRESTORE_USER_SYNCED_NOTES_COLLECTION_NAME = "SyncedNotes"
        const val FIRESTORE_SYNCED_NOTE_ID = "noteId"
        const val FIRESTORE_SYNCED_NOTE_BODY = "noteBody"
        const val FIRESTORE_SYNCED_NOTE_TITLE = "noteTitle"
        const val FIRESTORE_SYNCED_NOTE_DATE_CREATED = "dateCreated"
        const val FIRESTORE_SYNCED_NOTE_DATE_MODIFIED = "dateModified"
        const val FIRESTORE_SYNCED_NOTE_LOCATION_REMINDER = "geofenceLocation"
        const val FIRESTORE_SYNCED_NOTE_TIME_REMINDER = "timeReminder"
        const val FIRESTORE_SYNCED_NOTE_IS_SYNCED = "isSynced"
        /**
         * Firebase post related constants
         */
        const val FIRESTORE_POST_DOC_INTENT_KEY = "playground.develop.socialnote.post_document_name_key"
        const val FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY = "playground.develop.socialnote.post_author_register_key"
        const val OPEN_FROM_NOTIFICATION_COMMENT = "playground.develop.socialnote.open_from_notification"
        const val DISMISS_POST_COMMENT_NOTIFICATION_ACTION = "playground.develop.socialnote.dismiss_post_comment_notification"
        const val USER_LIKES_INTENT_KEY = "playground.develop.socialnote.user_likes_intent_key"
        const val FIRESTORE_POSTS_COLLECTION_NAME = "Posts"
        const val FIRESTORE_POSTS_POST_BODY = "post"
        const val FIRESTORE_POSTS_POST_AUTHOR_NAME = "authorName"
        const val FIRESTORE_POSTS_POST_CATEGORY_NAME = "categoryName"
        const val FIRESTORE_POSTS_POST_AUTHOR_ID = "authorUID"
        const val FIRESTORE_POSTS_POST_AUTHOR_IMAGE = "authorImage"
        const val FIRESTORE_POSTS_POST_DATE_CREATED = "dateCreated"
        const val FIRESTORE_POSTS_POST_LIKES = "likes"
        const val FIRESTORE_POSTS_POST_COMMENTS = "comments"
        const val FIRESTORE_POSTS_POST_REGISTER_TOKEN = "registerToken"
        const val FIRESTORE_POSTS_POST_DOC_NAME = "documentName"
        const val FIRESTORE_POSTS_POST_IMAGE_URL = "imageUrl"
        const val FIRESTORE_USER_COUNTRY_CODE = "countryCode"

        const val FIRESTORE_COMMENTS_NOTIFICATION_COLLECTION_NAME = "CommentNotification"
        const val FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_REGISTER_TOKEN = "authorRegisterToken"
        const val FIRESTORE_COMMENTS_NOTIFICATION_COMMENT = "comment"
        const val FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_IMAGE = "authorImage"
        const val FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_NAME = "authorName"
        const val FIRESTORE_COMMENTS_NOTIFICATION_AUTHOR_UID = "authorUID"
        const val FIRESTORE_COMMENTS_NOTIFICATION_DATE_CREATED = "dateCreated"
        const val FIRESTORE_COMMENTS_POST_COMMENT_DOC_ID = "documentId"
        const val FIRESTORE_COMMENTS_POST_COMMENT_AUTHOR_COUNTRY_CODE = "countryCode"
        const val FIRESTORE_COMMENTS_NOTIFICATION_COMMENTER_AUTHOR_TOKEN = "commentAuthorToken"

        const val FIRESTORE_LIKES_NOTIFICATION_COLLECTION_NAME = "LikesNotification"
        const val FIRESTORE_LIKES_NOTIFICATION_AUTHOR_REGISTER_TOKEN = "authorRegisterToken"
        const val FIRESTORE_LIKES_NOTIFICATION_USER_TOKEN = "userRegisterToken"
        const val FIRESTORE_LIKES_NOTIFICATION_USER_NAME = "userName"
        const val FIRESTORE_LIKES_NOTIFICATION_DOCUMENT_ID = "documentId"
        const val FIRESTORE_LIKES_NOTIFICATION_COUNTRY_CODE = "countryCode"
        const val FIRESTORE_LIKES_NOTIFICATION_USER_ID = "userLikerUId"

        const val FIRESTORE_USERS_COLLECTION_NAME = "Users"
        const val FIRESTORE_USER_UID = "userUid"
        const val FIRESTORE_USER_IMAGE_URL = "userImage"
        const val FIRESTORE_USER_NAME = "userName"
        const val FIRESTORE_USER_POSTS_COUNT = "userPostsCount"
        const val FIRESTORE_USER_TITLE = "userTitle"
        const val FIRESTORE_USER_COVER_IMAGE = "coverImage"

        const val USER_UID_INTENT_KEY = "playground.develop.socialnote.user_uid_intent_key"
        /**
         * Title related constants
         */
        const val AUTHOR_TITLE = "Author"
        const val READER_TITLE = "Reader"

        /**
         * Firestorage related constants
         */
        const val FIRESTORE_COVER_IMAGES = "cover_images"
        const val FIRESTORE_PROFILE_IMAGES = "profile_images"
        const val FIRESTORE_NOTES_IMAGES = "notes_images"
        const val FIRESTORE_POST_IMAGES = "post_images"
    }

}