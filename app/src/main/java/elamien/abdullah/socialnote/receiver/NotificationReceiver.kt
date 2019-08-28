package elamien.abdullah.socialnote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import elamien.abdullah.socialnote.utils.Constants

/**
 * Created by AbdullahAtta on 28-Aug-19.
 */
class NotificationReceiver : BroadcastReceiver() {

	override fun onReceive(context : Context?, intent : Intent?) {
		if (intent?.action == Constants.DISMISS_POST_COMMENT_NOTIFICATION_ACTION) {

		}
	}
}