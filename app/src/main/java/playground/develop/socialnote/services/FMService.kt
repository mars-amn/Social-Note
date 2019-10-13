package playground.develop.socialnote.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import playground.develop.socialnote.R
import playground.develop.socialnote.utils.NotificationsUtils
import playground.develop.socialnote.utils.PreferenceUtils

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
        val postCountryCode = message.data["countryCode"]
        if (authorToken == userLikerToken) {
            return
        }
        var title = message.data["title"]
        val documentId = message.data["documentId"]
        title = title + " " + applicationContext.getString(R.string.like_notification_title)
        notifyAuthor("", title, documentId!!, authorToken!!, postCountryCode)
    }

    private fun notifyUserWithPostComment(message: RemoteMessage) {
        val token = message.data["token"]
        val commentAuthToken = message.data["commentAuthToken"]
        val postCountryCode = message.data["countryCode"]
        if (commentAuthToken == token) {
            return
        }
        val comment = message.data["comment"]
        var title = message.data["title"]
        val documentId = message.data["documentId"]
        title = title + " " + applicationContext.getString(R.string.comment_notification_title)
        notifyAuthor(comment!!, title, documentId!!, token!!, postCountryCode)
    }

    private fun notifyAuthor(text: String, title: String, documentId: String, token: String, countryCode: String?) {
        NotificationsUtils.getNotificationUtils()
            .sendPostInteractNotification(applicationContext, text, title, documentId, token, countryCode)
    }
}