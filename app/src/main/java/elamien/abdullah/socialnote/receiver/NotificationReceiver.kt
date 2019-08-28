package elamien.abdullah.socialnote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import elamien.abdullah.socialnote.utils.Constants.Companion.DISMISS_POST_COMMENT_NOTIFICATION_ACTION
import elamien.abdullah.socialnote.utils.NotificationsUtils

/**
 * Created by AbdullahAtta on 28-Aug-19.
 */
class NotificationReceiver : BroadcastReceiver() {

	override fun onReceive(context : Context?, intent : Intent?) {
		if (intent?.action == DISMISS_POST_COMMENT_NOTIFICATION_ACTION) {
			Log.d("isNotification", "in receiver. action = dismissing")
			dismissCommentNotification(context!!,
					intent.getIntExtra(DISMISS_POST_COMMENT_NOTIFICATION_ACTION, -1))
		}
	}

	private fun dismissCommentNotification(context : Context, notificationId : Int) {
		Log.d("isNotification", "in receiver. id = $notificationId")
		NotificationsUtils.getNotificationUtils()
				.dismissCommentNotification(context, notificationId)
	}
}