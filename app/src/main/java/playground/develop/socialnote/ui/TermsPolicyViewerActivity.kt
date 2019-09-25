package playground.develop.socialnote.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import playground.develop.socialnote.R
import playground.develop.socialnote.databinding.ActivityTermsPolicyViewerBinding
import playground.develop.socialnote.utils.Constants.Companion.TERMS_POLICY_KEY


class TermsPolicyViewerActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityTermsPolicyViewerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_terms_policy_viewer)

        if (intent != null && intent.hasExtra(TERMS_POLICY_KEY)) {
            loadFileAndView(intent.getStringExtra(TERMS_POLICY_KEY))
        }
    }

    private fun loadFileAndView(fileName: String?) {
        var fileId: Int = R.raw.terms_conditions
        when (fileName) {
            "terms_conditions.txt" -> {
                fileId = R.raw.terms_conditions
                title = "Terms and Conditions"
            }
            "privacy_policy.txt" -> {
                fileId = R.raw.privacy_policy
                title = "Privacy Policy"
            }
        }
        val inputStream = resources.openRawResource(fileId)

        val byteArray = ByteArray(inputStream.available())
        inputStream.read(byteArray)
        mBinding.termsPolicyViewer.text = String(byteArray)
    }
}
