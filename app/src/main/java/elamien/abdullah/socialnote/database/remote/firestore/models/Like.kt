package elamien.abdullah.socialnote.database.remote.firestore.models

/**
 * Created by AbdullahAtta on 26-Aug-19.
 */
data class Like(var userLikerUId : String? = null,
				var authorRegisterToken : String? = null,
				var userRegisterToken : String? = null,
				var userName : String? = null,
				var documentId : String? = null,
				var userImage : String? = null) {}