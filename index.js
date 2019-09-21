const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendPostCommentNotification = functions.firestore
      .document('CommentNotification/{doc_id}')
      .onCreate((snap, context) => {
        const newValue = snap.data();

        const token = newValue.authorRegisterToken;
        const commentToken = newValue.commentAuthorToken
        const name = newValue.authorName
        const comment = newValue.comment
        const documentId = newValue.documentId
        const commenterImage = newValue.authorImage
         const countryCode = newValue.countryCode
        const type = 'COMMENT'


        const payload = {
                data: {
                    title: name + " commented on your post on Social Note",
                    token: token,
                    commentAuthToken: commentToken,
                    comment: comment,
                    documentId: documentId,
                    countryCode:countryCode,
             	      type : type
                }
            };

            return admin.messaging().sendToDevice(token,payload).then(result => {
                console.log("Notification sent!");
            });


});
exports.sendPostLikeNotification = functions.firestore
      .document('LikesNotification/{doc_id}')
      .onCreate((snap, context) => {
        const newValue = snap.data();

        const authorToken = newValue.authorRegisterToken;
        const userLikerToken = newValue.userRegisterToken
        const name = newValue.userName
        const documentId = newValue.documentId
        const countryCode = newValue.countryCode
 		const type = 'LIKE'


        const payload = {
                data: {
                    title: name + " liked your post on Social Note",
                    authorToken: authorToken,
                    userLikerToken: userLikerToken,
                    documentId: documentId,
                    countryCode: countryCode,
                 type : type
                }
            };

            return admin.messaging().sendToDevice(authorToken,payload).then(result => {
                console.log("Notification sent!");
            });


});