package elamien.abdullah.socialnote.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import elamien.abdullah.socialnote.utils.NotificationsUtils

/**
 * Created by AbdullahAtta on 27-Aug-19.
 */
class FMService : FirebaseMessagingService() {

	override fun onMessageReceived(message : RemoteMessage) {
		val comment = message.data["comment"]
		val title = message.data["title"]
		val documentId = message.data["documentId"]
		val token = message.data["token"]
		val commentAuthToken = message.data["commentAuthToken"]

		if (commentAuthToken == token) return // wouldn't be nice if the author of the post commented on his post, yet to receive a notification

		notifyUserWithComment(comment!!, title!!, documentId!!, token!!)
	}

	private fun notifyUserWithComment(comment : String,
									  title : String,
									  documentId : String,
									  token : String) {
		NotificationsUtils.getNotificationUtils()
				.sendCommentNotification(applicationContext, comment, title, documentId, token)
	}
}