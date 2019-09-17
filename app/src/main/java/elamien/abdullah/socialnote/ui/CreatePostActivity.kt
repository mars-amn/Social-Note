package elamien.abdullah.socialnote.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.github.irshulx.EditorListener
import com.github.irshulx.models.EditorTextStyle
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.database.remote.firestore.models.Post
import elamien.abdullah.socialnote.databinding.ActivityCreatePostBinding
import elamien.abdullah.socialnote.utils.Constants.Companion.FIRESTORE_POST_IMAGES
import elamien.abdullah.socialnote.viewmodel.PostViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import top.defaults.colorpicker.ColorPickerPopup
import java.io.ByteArrayOutputStream
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityCreatePostBinding
    private val mFirebaseAuth: FirebaseAuth by inject()
    private val mFirebaseStorage: FirebaseStorage by inject()

    private val mPostViewModel: PostViewModel by viewModel()
    var mRegisterToken: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil
                .setContentView(this@CreatePostActivity, R.layout.activity_create_post)
        mBinding.handlers = this
        initEditor()
        getRegisterToken()
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
            ColorPickerPopup.Builder(this@CreatePostActivity).initialColor(Color.RED)
                    .enableAlpha(true).okTitle("Pick").cancelTitle("Cancel").showIndicator(true)
                    .showValue(true).build().show(findViewById<View>(android.R.id.content),
                                                  object : ColorPickerPopup.ColorPickerObserver() {
                                                      override fun onColorPicked(color: Int) {
                                                          mBinding.editor
                                                                  .updateTextColor(colorHex(color))
                                                      }
                                                  })
        }

        findViewById<View>(R.id.action_insert_image)
                .setOnClickListener { mBinding.editor.openImagePicker() }

        findViewById<View>(R.id.action_insert_link)
                .setOnClickListener { mBinding.editor.insertLink() }


        findViewById<View>(R.id.action_erase)
                .setOnClickListener { mBinding.editor.clearAllContents() }
        mBinding.editor.editorListener = object : EditorListener {
            override fun onRenderMacro(name: String?,
                                       props: MutableMap<String, Any>?,
                                       index: Int): View {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(editText: EditText, text: Editable) {
            }

            override fun onUpload(image: Bitmap, uuid: String) {
                val baos = ByteArrayOutputStream()
                Toast.makeText(this@CreatePostActivity, uuid, Toast.LENGTH_LONG).show()
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val bytes = baos.toByteArray()
                val coverImageRef = mFirebaseStorage.getReference(FIRESTORE_POST_IMAGES)
                        .child(Date().time.toString())
                val uploadTask = coverImageRef.putBytes(bytes)
                uploadTask
                        .continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(this@CreatePostActivity,
                                               "Failed uploading image",
                                               Toast.LENGTH_SHORT).show()
                            }
                            return@Continuation coverImageRef.downloadUrl
                        }).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                mBinding.editor.onImageUploadComplete(task.result.toString(), uuid)
                            }
                        }

            }


        }
    }

    private fun colorHex(color: Int): String {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b)
    }

    fun onCreatePostButtonClick(view: View) {
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
                        Timestamp(Date()))
        mPostViewModel.createPost(post)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mBinding.editor.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri = data.data!!
            val imageStream = contentResolver.openInputStream(uri)!!
            val imageBitmap = BitmapFactory.decodeStream(imageStream)
            mBinding.editor.insertImage(imageBitmap)
        } else if (resultCode == RESULT_CANCELED) {
        }
    }

    private fun getRegisterToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            mRegisterToken = instanceIdResult.token
            Toast.makeText(this@CreatePostActivity, instanceIdResult.token, Toast.LENGTH_LONG)
                    .show()
        }
    }


}
