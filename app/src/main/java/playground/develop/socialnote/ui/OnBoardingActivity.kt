package playground.develop.socialnote.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import playground.develop.socialnote.R
import playground.develop.socialnote.adapter.OnBoardingPagerAdapter
import playground.develop.socialnote.databinding.ActivityOnboardingBinding


class OnBoardingActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        val pagerAdapter = OnBoardingPagerAdapter(this, supportFragmentManager)
        mBinding.onBoardingViewPager.adapter = pagerAdapter
        mBinding.onBoardingWormDotsIndicator.setViewPager(mBinding.onBoardingViewPager)
    }

    private fun setupFullScreen() {
        window
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}
