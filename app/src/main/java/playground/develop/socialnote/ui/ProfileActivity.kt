package playground.develop.socialnote.ui

import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import coil.api.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import playground.develop.socialnote.R
import playground.develop.socialnote.adapter.PostsFeedAdapter
import playground.develop.socialnote.database.remote.firestore.models.Like
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.database.remote.firestore.models.User
import playground.develop.socialnote.databinding.ActivityProfileBinding
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.utils.Constants.Companion.AUTHOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_COVER_IMAGES
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_PROFILE_IMAGES
import playground.develop.socialnote.utils.Constants.Companion.ORIGINATOR_TITLE
import playground.develop.socialnote.utils.Constants.Companion.READER_TITLE
import playground.develop.socialnote.utils.Constants.Companion.USER_UID_INTENT_KEY
import playground.develop.socialnote.utils.DeviceUtils
import playground.develop.socialnote.utils.PreferenceUtils
import playground.develop.socialnote.viewmodel.PostViewModel
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity(), PostsFeedAdapter.PostInteractListener {

    private val mFirebaseStorage: FirebaseStorage by inject()
    private val mPostViewModel: PostViewModel by viewModel()
    private val mAuth by inject<FirebaseAuth>()

    private lateinit var mAdapter: PostsFeedAdapter

    private lateinit var mBinding: ActivityProfileBinding
    private lateinit var mUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this@ProfileActivity, R.layout.activity_profile)
        mBinding.handlers = this
        mAdapter = PostsFeedAdapter(this@ProfileActivity, this@ProfileActivity, ArrayList<Post>())

        if (intent != null && intent.hasExtra(USER_UID_INTENT_KEY)) {
            val userUid = intent.getStringExtra(USER_UID_INTENT_KEY)
            if (userUid == mAuth.currentUser?.uid) {
                loadCurrentUser()
            } else {
                loadUser()
            }
        } else {
            loadCurrentUser()
        }
    }

    private val mUserCountryCode: String?
        get() {
            return PreferenceUtils.getPreferenceUtils()
                    .getUserCountryCode(this)
        }

    private fun loadUser() {
        hideProfileUtilsButtons()
        val userUid = intent.getStringExtra(USER_UID_INTENT_KEY)
        mPostViewModel.getUser(userUid)
                .observe(this@ProfileActivity, Observer { user ->
                    showUserDetails(user)
                })
        loadUserPosts(userUid)
    }

    private fun loadUserPosts(userUid: String?) {
        mPostViewModel.getUserPosts(userUid, mUserCountryCode!!)
                .observe(this@ProfileActivity, Observer { posts ->
                    if (posts.isNotEmpty()) {
                        mAdapter.addPosts(posts)
                        mBinding.userPostsRecyclerView.adapter = mAdapter
                    }
                })
    }

    private fun showUserDetails(user: User) {
        mUser = user
        mBinding.userProfileCoverImage.load(user.coverImage)
        mBinding.userProfileImage.load(user.userImage) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
        mBinding.userProfileName.text = user.userName
        when (user.userTitle) {
            READER_TITLE -> showReaderTitle()
            AUTHOR_TITLE -> showAuthorTitle()
            ORIGINATOR_TITLE -> showOriginatorTitle()
        }
    }

    private fun showOriginatorTitle() {
        mBinding.userProfileTitle.text = getString(R.string.originator_title)
        mBinding.userProfileTitle.setTextColor(ContextCompat.getColor(this,
                                                                      R.color.originator_title_color))
    }

    private fun showAuthorTitle() {
        mBinding.userProfileTitle.text = getString(R.string.author_title)
        mBinding.userProfileTitle.setTextColor(ContextCompat.getColor(this,
                                                                      R.color.author_title_color))
    }

    private fun showReaderTitle() {
        mBinding.userProfileTitle.text = getString(R.string.reader_title)
        mBinding.userProfileTitle.setTextColor(ContextCompat.getColor(this,
                                                                      R.color.reader_title_color))
    }

    private fun hideProfileUtilsButtons() {
        mBinding.userProfileChangeCoverImageButton.visibility = View.GONE
        mBinding.changeProfileImageButton.visibility = View.GONE
    }

    private fun loadCurrentUser() {
        mPostViewModel.getUser()
                .observe(this@ProfileActivity, Observer { user ->
                    showUserDetails(user)
                })
        loadUserPosts()
    }

    private fun loadUserPosts() {
        mPostViewModel.getUserPosts(mAuth.currentUser?.uid, mUserCountryCode!!)
                .observe(this@ProfileActivity, Observer { posts ->
                    if (posts.isNotEmpty()) {
                        mAdapter.addPosts(posts)
                        mBinding.userPostsRecyclerView.adapter = mAdapter
                    }
                })
    }

    fun onChangeCoverImageClick(view: View) {
        startImagePickChooser(COVER_IMAGE_PICK_REQUEST_CODE)
    }

    private fun startImagePickChooser(requestCode: Int) {
        val imageIntent = Intent(ACTION_GET_CONTENT)
        imageIntent.type = "image/*"
        val chooser = Intent.createChooser(imageIntent,
                                           getString(R.string.image_picker_chooser_title))
        startActivityForResult(chooser, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && requestCode == COVER_IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data.data!!
            val imageStream = contentResolver.openInputStream(uri)!!
            val imageBitmap = BitmapFactory.decodeStream(imageStream)
            uploadImageToFirestore(imageBitmap!!, FIRESTORE_COVER_IMAGES)
        } else if (data != null && requestCode == PROFILE_IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data.data!!
            val imageStream = contentResolver.openInputStream(uri)!!
            val imageBitmap = BitmapFactory.decodeStream(imageStream)
            uploadImageToFirestore(imageBitmap!!, FIRESTORE_PROFILE_IMAGES)
        }
    }

    private fun uploadImageToFirestore(imageBitmap: Bitmap, where: String) {
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        val coverImageRef = mFirebaseStorage.getReference(where)
                .child(mUser.userUid!! + where)
        val uploadTask = coverImageRef.putBytes(bytes)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                toast("Failed uploading image")
            }
            return@Continuation coverImageRef.downloadUrl
        })
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        when (where) {
                            FIRESTORE_COVER_IMAGES -> {
                                updateUserCoverImage(task.result.toString())
                            }
                            FIRESTORE_PROFILE_IMAGES -> {
                                updateUserProfileImage(task.result.toString())
                            }

                        }
                    }
                }
    }

    private fun updateUserCoverImage(imageUrl: String) {
        mUser.coverImage = imageUrl
        mBinding.userProfileCoverImage.load(imageUrl) {
            crossfade(true)
        }
        updateUserInformation()
    }

    private fun updateUserProfileImage(imageUrl: String) {
        mUser.userImage = imageUrl
        mBinding.userProfileImage.load(imageUrl) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
        updateUserInformation()
    }

    fun onChangeProfileImageClick(view: View) {
        startImagePickChooser(PROFILE_IMAGE_PICK_REQUEST_CODE)
    }

    private fun updateUserInformation() {
        mPostViewModel.updateUser(mUser)
    }

    override fun onCommentButtonClick(post: Post) {
        startActivity(intentFor<PostDetailsActivity>(Constants.FIRESTORE_POST_DOC_INTENT_KEY to post.documentName,
                                                     Constants.FIRESTORE_POST_AUTHOR_REGISTER_TOKEN_KEY to post.registerToken,
                                                     Constants.USER_COUNTRY_ISO_KEY to post.countryCode))
    }

    override fun onLikeButtonClick(like: Like, postCountryCode: String) {
        like.userTitle = mUser.userTitle!!
        mPostViewModel.createLikeOnPost(like, postCountryCode)
    }

    override fun onUnLikeButtonClick(like: Like, postCountryCode: String) {
        like.userTitle = mUser.userTitle!!
        mPostViewModel.removeLikePost(like, postCountryCode)
    }

    override fun onPostLongClickListener(post: Post) {
        if (DeviceUtils.getDeviceUtils(this).dsd(mAuth.currentUser?.uid!!)) {
            MaterialAlertDialogBuilder(this@ProfileActivity).setTitle(getString(R.string.delete_post_dialog_title))
                    .setMessage(getString(R.string.delete_post_dialog_message))
                    .setNegativeButton(getString(R.string.delete_post_dialog_negative_button)) { dialog, id ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.delete_post_dialog_positive_button)) { dialog, id ->
                        mPostViewModel.deletePost(post)
                        dialog.dismiss()
                    }
                    .setNeutralButton(R.string.b) { dialog, id ->
                        mPostViewModel.b(post.authorUID, post.post!!)
                        mPostViewModel.deletePost(post)
                        dialog.dismiss()
                    }
                    .show()
        } else if (mUser.userUid == post.authorUID) {
            MaterialAlertDialogBuilder(this@ProfileActivity).setTitle(getString(R.string.delete_post_dialog_title))
                    .setMessage(getString(R.string.delete_post_dialog_message))
                    .setNegativeButton(getString(R.string.delete_post_dialog_negative_button)) { dialog, id ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(getString(R.string.delete_post_dialog_positive_button)) { dialog, id ->
                        mPostViewModel.deletePost(post)
                        loadUserPosts()
                        dialog.dismiss()
                    }
                    .show()
        }
    }

    companion object {
        const val COVER_IMAGE_PICK_REQUEST_CODE = 8
        const val PROFILE_IMAGE_PICK_REQUEST_CODE = 33
    }
}
