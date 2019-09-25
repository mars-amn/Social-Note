package playground.develop.socialnote.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import coil.api.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import org.jetbrains.anko.intentFor
import playground.develop.socialnote.BuildConfig
import playground.develop.socialnote.R
import playground.develop.socialnote.databinding.ActivityAboutBinding
import playground.develop.socialnote.utils.Constants.Companion.APP_FACEBOOK_URL
import playground.develop.socialnote.utils.Constants.Companion.APP_TWITTER_ID
import playground.develop.socialnote.utils.Constants.Companion.APP_TWITTER_URL
import playground.develop.socialnote.utils.Constants.Companion.TERMS_POLICY_KEY


class AboutActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about)
        mBinding.handlers = this
        setupLicensesText()
        setupAppVersionText()
        setupAppLogo()
    }

    fun onTermsClick(view: View) {
        startActivity(intentFor<TermsPolicyViewerActivity>(TERMS_POLICY_KEY to "terms_conditions.txt"))
    }

    fun onPrivacyPolicyClick(view: View) {
        startActivity(intentFor<TermsPolicyViewerActivity>(TERMS_POLICY_KEY to "privacy_policy.txt"))
    }

    private fun setupAppLogo() {
        mBinding.appLogoImage.load(R.drawable.logo) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }

    private fun setupAppVersionText() {
        val appVersion = BuildConfig.VERSION_NAME
        mBinding.appVersion.text = appVersion
    }

    private fun setupLicensesText() {
        mBinding.licensesText.paintFlags = mBinding.licensesText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun onLicensesClick(view: View) {
        startActivity(intentFor<OssLicensesMenuActivity>())
    }

    fun onFacebookButtonClick(view: View) {
        val intent = getFacebookIntent()
        startActivity(intent)
    }

    private fun getFacebookIntent(): Intent {
        try {
            if (getFacebookApplicationInfo().enabled) {
                val facebookUri = Uri.parse("fb://facewebmodal/f?href=$APP_FACEBOOK_URL")
                return Intent(Intent.ACTION_VIEW, facebookUri)
            }
        } catch (e: PackageManager.NameNotFoundException) {

        }

        return Intent(Intent.ACTION_VIEW, Uri.parse(APP_FACEBOOK_URL))
    }

    fun onTwitterButtonClick(view: View) {
        startActivity(getTwitterIntent())
    }

    private fun getTwitterIntent(): Intent {
        var twitterIntent: Intent? = null
        try {
            if (getTwitterApplicationInfo().enabled) {
                twitterIntent = Intent(Intent.ACTION_VIEW,
                                       Uri.parse("twitter://user?user_id=$APP_TWITTER_ID"))
                twitterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            twitterIntent = Intent(Intent.ACTION_VIEW, Uri.parse(APP_TWITTER_URL))
        }

        return twitterIntent!!
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getFacebookApplicationInfo(): ApplicationInfo {
        return packageManager.getApplicationInfo("com.facebook.katana", 0)
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun getTwitterApplicationInfo(): ApplicationInfo {
        return packageManager.getApplicationInfo("com.twitter.android", 0)
    }
}
