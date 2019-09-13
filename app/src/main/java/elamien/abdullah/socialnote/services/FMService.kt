package elamien.abdullah.socialnote.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import elamien.abdullah.socialnote.utils.NotificationsUtils
import elamien.abdullah.socialnote.utils.PreferenceUtils

/**
 * Created by AbdullahAtta on 27-Aug-19.
 */
class FMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        if (!PreferenceUtils.getPreferenceUtils().isPushNotificationsEnabled(baseContext)) return

        when (message.data["type"]) {
            "COMMENT" -> notifyUserWithPostComment(message)
            "LIKE" -> notifyUserWithPostLike(message)
            else -> {
                return
            }
        }
    }

    private fun notifyUserWithPostLike(message: RemoteMessage) {
        val authorToken = message.data["authorToken"]
        val userLikerToken = message.data["userLikerToken"]
        if (authorToken == userLikerToken) {
            return
        }
        val title = message.data["title"]
        val documentId = message.data["documentId"]
        notifyAuthor("", title!!, documentId!!, authorToken!!)
    }

    private fun notifyUserWithPostComment(message: RemoteMessage) {
        val token = message.data["token"]
        val commentAuthToken = message.data["commentAuthToken"]
        if (commentAuthToken == token) {
            return
        }
        val comment = message.data["comment"]
        val title = message.data["title"]
        val documentId = message.data["documentId"]
        notifyAuthor(comment!!, title!!, documentId!!, token!!)
    }

    private fun notifyAuthor(text: String, title: String, documentId: String, token: String) {
        NotificationsUtils.getNotificationUtils()
                .sendPostInteractNotification(applicationContext, text, title, documentId, token)
    }
}