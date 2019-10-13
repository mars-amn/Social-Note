package playground.develop.socialnote.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import coil.api.load
import com.transitionseverywhere.extra.Scale
import playground.develop.socialnote.databinding.FragmentOnboardingBinding
import playground.develop.socialnote.ui.RegisterActivity
import playground.develop.socialnote.utils.Constants

/**
 * Created by AbdullahAtta on 22-Sep-19.
 */
class OnBoardingFragment : Fragment() {

    @DrawableRes
    var drawable: Int? = null
    var description: String? = null
    var index: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawable = arguments?.getInt(DRAWABLE_KEY)
        description = arguments?.getString(DESCRIPTION_KEY)
        index = arguments?.getInt(FRAGMENT_INDEX_KEY)
    }

    private lateinit var mBinding: FragmentOnboardingBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentOnboardingBinding.inflate(inflater, container, false)
        mBinding.handlers = this
        mBinding.onBoardingImage.load(drawable!!) {
            crossfade(true)
        }
        mBinding.onBoardingDescription.text = description

        if (index == 2) {
            showStartButton()
        } else {
            mBinding.onBoardingStartButton.visibility = View.GONE
        }
        return mBinding.root
    }

    private fun showStartButton() {
        applyAnimation()
        mBinding.onBoardingStartButton.visibility = View.VISIBLE
    }

    fun onStartButtonClick(view: View) {
        saveUserFirstLaunch()
        val intent = Intent(context, RegisterActivity::class.java)
        context?.startActivity(intent)
        activity?.finish()
    }

    private fun saveUserFirstLaunch() {
        val editor =
            context?.getSharedPreferences(Constants.APP_PREFERENCE_NAME, AppCompatActivity.MODE_PRIVATE)!!
                .edit()
        editor.putBoolean(Constants.FIRST_LAUNCH_KEY, true)
        editor.apply()
    }

    private fun applyAnimation() {
        val set = TransitionSet().addTransition(Scale(0.7f)).addTransition(Fade())
            .setInterpolator(FastOutLinearInInterpolator())
        TransitionManager.beginDelayedTransition(mBinding.fragmentParent, set)
    }

    companion object {
        fun newInstance(drawable: Int, description: String, index: Int): OnBoardingFragment {
            val fragment = OnBoardingFragment()
            val args = Bundle()
            args.putInt(DRAWABLE_KEY, drawable)
            args.putString(DESCRIPTION_KEY, description)
            args.putInt(FRAGMENT_INDEX_KEY, index)
            fragment.arguments = args
            return fragment
        }

        private const val DRAWABLE_KEY = "playground.develop.socialnote.fragments.drawable_key"
        private const val DESCRIPTION_KEY =
            "playground.develop.socialnote.fragments.description_key"
        private const val FRAGMENT_INDEX_KEY = "playground.develop.socialnote.fragments.index_key"
    }
}