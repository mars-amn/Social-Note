package playground.develop.socialnote.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.databinding.DataBindingUtil
import coil.api.load
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.github.irshulx.models.EditorTextStyle
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.jetbrains.anko.toast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import playground.develop.socialnote.R
import playground.develop.socialnote.database.remote.firestore.models.Post
import playground.develop.socialnote.databinding.ActivityCreatePostBinding
import playground.develop.socialnote.utils.Constants.Companion.FIRESTORE_POST_IMAGES
import playground.develop.socialnote.viewmodel.PostViewModel
import java.io.ByteArrayOutputStream
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCreatePostBinding
    private val mFirebaseAuth: FirebaseAuth by inject()
    private val mFirebaseStorage: FirebaseStorage by inject()

    private val mPostViewModel: PostViewModel by viewModel()
    var mRegisterToken: String? = null
    private var mSelectedImage: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil
                .setContentView(this@CreatePostActivity, R.layout.activity_create_post)
        mBinding.handlers = this
        initEditor()
        getRegisterToken()

        if (savedInstanceState != null) {
            mBinding.editor.render(savedInstanceState.getString(EDITOR_SAVE_STATE_KEY))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EDITOR_SAVE_STATE_KEY, mBinding.editor.contentAsHTML)
    }

    private fun initEditor() {
        findViewById<View>(R.id.action_h1)
                .setOnClickListener { mBinding.editor.updateTextStyle(EditorTextStyle.H1) }

        findViewById<View>(R.id.action_h2)
                .setOnClickListener { mBinding.editor.updateTextStyle(EditorTextStyle.H2) }

        findViewById<View>(R.id.action_h3)
                .setOnClickListener { mBinding.editor.updateTextStyle(EditorTextStyle.H3) }

        findViewById<View>(R.id.action_bold)
                .setOnClickListener { mBinding.editor.updateTextStyle(EditorTextStyle.BOLD) }

        findViewById<View>(R.id.action_Italic)
                .setOnClickListener { mBinding.editor.updateTextStyle(EditorTextStyle.ITALIC) }

        findViewById<View>(R.id.action_indent)
                .setOnClickListener { mBinding.editor.updateTextStyle(EditorTextStyle.INDENT) }

        findViewById<View>(R.id.action_blockquote)
                .setOnClickListener { mBinding.editor.updateTextStyle(EditorTextStyle.BLOCKQUOTE) }

        findViewById<View>(R.id.action_outdent)
                .setOnClickListener { mBinding.editor.updateTextStyle(EditorTextStyle.OUTDENT) }

        findViewById<View>(R.id.action_bulleted)
                .setOnClickListener { mBinding.editor.insertList(false) }

        findViewById<View>(R.id.action_unordered_numbered)
                .setOnClickListener { mBinding.editor.insertList(true) }
        findViewById<View>(R.id.action_hr).setOnClickListener { mBinding.editor.insertDivider() }


        findViewById<View>(R.id.action_color).setOnClickListener {
            ColorPickerDialogBuilder.with(this)
                    .setTitle(getString(R.string.color_pick_choose_title)).initialColor(Color.RED)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .setOnColorSelectedListener { color ->
                        mBinding.editor.updateTextColor(colorHex(color))
                    }
                    .setPositiveButton(getString(R.string.color_picker_positive_button)) { dialog, color, colors ->
                        mBinding.editor.updateTextColor(colorHex(color))
                    }
                    .setNegativeButton(getString(R.string.color_picker_negative_button)) { dialog, which -> }
                    .build().show()
        }

        findViewById<View>(R.id.action_insert_image).setOnClickListener { startImagePicker() }

        findViewById<View>(R.id.action_insert_link)
                .setOnClickListener { mBinding.editor.insertLink() }


        findViewById<View>(R.id.action_erase)
                .setOnClickListener { mBinding.editor.clearAllContents() }
    }

    private fun startImagePicker() {
        val imageIntent = Intent(Intent.ACTION_GET_CONTENT)
        imageIntent.type = "image/*"
        val chooser = Intent.createChooser(imageIntent, getString(R.string.image_picker_chooser_title))
        startActivityForResult(chooser, PICK_IMAGE_REQUEST_CODE)
    }

    private fun colorHex(color: Int): String {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.post_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.postMenuItem -> createNewPost()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createNewPost() {
        if (stripHtml() == "" || stripHtml().isEmpty()) {
            toast(getString(R.string.empty_post_message))
            return
        }

        if (mSelectedImage != null) {
            postWithImage()
        } else {
            uploadPostWithoutImage()
        }
    }


    private fun uploadPostWithoutImage() {
        val post = getPost()
        post.imageUrl = ""
        mPostViewModel.createPost(post)
        finish()
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
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val body = mBinding.editor.contentAsHTML
                val authorId = mFirebaseAuth.currentUser?.uid
                val authorImage = mFirebaseAuth.currentUser?.photoUrl.toString()
                val categoryName = ""
                val authorName = mFirebaseAuth.currentUser?.displayName
                val post = Post(mRegisterToken,
                                body,
                                authorName,
                                categoryName,
                                authorId,
                                authorImage,
                                Timestamp(Date()),
                                imageUrl = task.result.toString())
                mPostViewModel.createPost(post)
                finish()
            }
        }
    }

    private fun getPost(): Post {
        val body = mBinding.editor.contentAsHTML
        val authorId = mFirebaseAuth.currentUser?.uid
        val authorImage = mFirebaseAuth.currentUser?.photoUrl.toString()
        val categoryName = ""
        val authorName = mFirebaseAuth.currentUser?.displayName
        return Post(mRegisterToken,
                    body,
                    authorName,
                    categoryName,
                    authorId,
                    authorImage,
                    Timestamp(Date()))

    }

    private fun stripHtml(): String {
        val text = mBinding.editor.contentAsHTML
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(text).toString()
        }
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
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            mRegisterToken = instanceIdResult.token
        }
    }

    override fun onBackPressed() {
        if (stripHtml() == "" || stripHtml().isEmpty()) {
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
                }.show()
    }

    private fun navigateUp() {
        NavUtils.navigateUpFromSameTask(this)
        finish()
    }

    companion object {
        const val EDITOR_SAVE_STATE_KEY = "EDITOR-SAVE-STATE-KEY"
        const val PICK_IMAGE_REQUEST_CODE = 7
    }
}
