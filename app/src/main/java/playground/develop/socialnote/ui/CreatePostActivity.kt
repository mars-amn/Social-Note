package playground.develop.socialnote.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import coil.api.load
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import playground.develop.socialnote.R
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.database.remote.firestore.models.User
import playground.develop.socialnote.databinding.ActivityCreatePostBinding
import playground.develop.socialnote.eventbus.SocialPostMessage
import playground.develop.socialnote.utils.Constants.Companion.BLOCKED_EVENT
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POST_IMAGES
import playground.develop.socialnote.utils.Constants.Companion.POST_SUCCESS_EVENT
import playground.develop.socialnote.utils.PreferenceUtils
import playground.develop.socialnote.viewmodel.PostViewModel
import java.io.ByteArrayOutputStream
import java.util.*

class CreatePostActivity : AppCompatActivity(), IAztecToolbarClickListener {
    override fun onToolbarHtmlButtonClicked() {

    }

    override fun onToolbarListButtonClicked() {
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }

    override fun onToolbarCollapseButtonClicked() {
    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
    }

    override fun onToolbarHeadingButtonClicked() {
    }

    private lateinit var mBinding: ActivityCreatePostBinding
    private val mFirebaseAuth: FirebaseAuth by inject()
    private val mFirebaseStorage: FirebaseStorage by inject()
    private lateinit var mUser: User
    private val mPostViewModel: PostViewModel by viewModel()
    var mRegisterToken: String? = null
    private var mSelectedImage: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this@CreatePostActivity,
            R.layout.activity_create_post
        )
        mBinding.handlers = this
        registerEventBus()
        initEditor()
        getRegisterToken()
        loadUser()
    }

    private fun loadUser() {
        mPostViewModel.getUser()
            .observe(this, Observer { user ->
                if (user != null) {
                    mUser = user
                }
            })
    }

    private fun registerEventBus() {
        EventBus.getDefault()
            .register(this)
    }

    override fun onStop() {
        super.onStop()
        unregisterEventBus()
    }

    private fun unregisterEventBus() {
        EventBus.getDefault()
            .unregister(this)
    }

    @Subscribe
    fun onEvent(event: SocialPostMessage) {
        if (event.postEventMessage == POST_SUCCESS_EVENT) {
            finish()
        } else if (event.postEventMessage == BLOCKED_EVENT) {
            showBlockDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) {
            registerEventBus()
        }
    }

    private fun showBlockDialog() {
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.blocked_dialog_title))
            .setMessage(getString(R.string.blocked_message))
            .setPositiveButton(getString(R.string.blocked_dialog_button_label)) { dialog, id ->
                dialog.dismiss()
            }
            .show()
    }

    private fun initEditor() {
        Aztec.with(mBinding.aztec, mBinding.source, mBinding.toolbarEditor, this)
        mBinding.aztec.setCalypsoMode(false)
        mBinding.source.setCalypsoMode(false)
        mBinding.aztec.setTextColor(Color.BLACK)
    }

    private fun startImagePicker() {
        val imageIntent = Intent(Intent.ACTION_GET_CONTENT)
        imageIntent.type = "image/*"
        val chooser = Intent.createChooser(
            imageIntent,
            getString(R.string.image_picker_chooser_title)
        )
        startActivityForResult(chooser, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.post_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.postMenuItem -> createNewPost()
            R.id.addImage -> startImagePicker()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createNewPost() {
        if (mBinding.aztec.toFormattedHtml() == "") {
            toast(getString(R.string.empty_post_message))
            return
        }
        toast(getString(R.string.posting_proccess_msg))
        if (mSelectedImage != null) {
            postWithImage()
        } else {
            uploadPostWithoutImage()
        }
    }


    private fun uploadPostWithoutImage() {
        val post = getPost()
        post.imageUrl = ""
        mPostViewModel.createPost(post, mUserCountryCode!!)
    }

    private val mUserCountryCode: String?
        get() {
            return PreferenceUtils.getPreferenceUtils()
                .getUserCountryCode(this)
        }

    private fun postWithImage() {
        val baos = ByteArrayOutputStream()
        mSelectedImage!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        val postImageRef = mFirebaseStorage.getReference(FIRESTORE_POST_IMAGES)
            .child(Date().time.toString())
        val uploadTask = postImageRef.putBytes(bytes)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                toast(getString(R.string.failed_upolad_message))
            }
            return@Continuation postImageRef.downloadUrl
        })
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val body = mBinding.aztec.toFormattedHtml()
                    val authorId = mFirebaseAuth.currentUser?.uid
                    val authorImage = mUser.userImage
                    val categoryName = ""
                    val authorName = mFirebaseAuth.currentUser?.displayName
                    val post = Post(
                        mRegisterToken,
                        body,
                        authorName,
                        categoryName,
                        authorId,
                        authorImage,
                        Timestamp(Date()),
                        imageUrl = task.result.toString(),
                        countryCode = mUserCountryCode
                    )
                    mPostViewModel.createPost(post, mUserCountryCode!!)
                }
            }
    }

    private fun getPost(): Post {
        val body = mBinding.aztec.toFormattedHtml()
        val authorId = mFirebaseAuth.currentUser?.uid
        val authorImage = mUser.userImage
        val categoryName = ""
        val authorName = mFirebaseAuth.currentUser?.displayName
        return Post(
            mRegisterToken,
            body,
            authorName,
            categoryName,
            authorId,
            authorImage,
            Timestamp(Date()),
            countryCode = mUserCountryCode
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri = data.data!!
            val imageStream = contentResolver.openInputStream(uri)!!
            val imageBitmap = BitmapFactory.decodeStream(imageStream)
            showImage(imageBitmap)
            mSelectedImage = imageBitmap
        }
    }

    private fun showImage(imageBitmap: Bitmap?) {
        mBinding.postImage.visibility = View.VISIBLE
        mBinding.removeSelectedImageButton.visibility = View.VISIBLE
        mBinding.postImage.load(imageBitmap) {
            crossfade(true)
        }
    }

    fun onRemoveImageClick(view: View) {
        mBinding.postImage.setImageResource(android.R.color.transparent)
        mBinding.postImage.visibility = View.GONE
        mBinding.removeSelectedImageButton.visibility = View.GONE
        mSelectedImage = null
    }

    private fun getRegisterToken() {
        FirebaseInstanceId.getInstance()
            .instanceId.addOnSuccessListener { instanceIdResult ->
            mRegisterToken = instanceIdResult.token
        }
    }

    override fun onBackPressed() {
        if (mBinding.aztec.toFormattedHtml() == "") {
            super.onBackPressed()
        } else {
            showPostAlertDialog()
        }
    }

    private fun showPostAlertDialog() {
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.post_alert_dialog_title))
            .setMessage(getString(R.string.post_alert_dialog_message))
            .setPositiveButton(getString(R.string.back_button_dialog_positive_button_label)) { dialog, id ->
                dialog.dismiss()
                navigateUp()
            }
            .setNegativeButton(getString(R.string.back_button_dialog_negative_button_label)) { dialog, id ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateUp() {
        NavUtils.navigateUpFromSameTask(this)
        finish()
    }

    companion object {
        const val PICK_IMAGE_REQUEST_CODE = 72
    }
}
