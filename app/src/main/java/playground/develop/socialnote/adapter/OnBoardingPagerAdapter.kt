package playground.develop.socialnote.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import playground.develop.socialnote.R
import playground.develop.socialnote.fragments.OnBoardingFragment

/**
 * Created by AbdullahAtta on 22-Sep-19.
 */
class OnBoardingPagerAdapter(private val context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragmentsNumber = 3

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> OnBoardingFragment.newInstance(R.drawable.ic_cloud_sync, context.getString(R.string.on_boarding_first_card_description), position)
            1 -> OnBoardingFragment.newInstance(R.drawable.ic_notification_location, context.getString(R.string.on_boarding_second_card_description), position)
            2 -> OnBoardingFragment.newInstance(R.drawable.ic_discussion, context.getString(R.string.on_boarding_third_card_description), position)
            else -> {
                OnBoardingFragment
                    .newInstance(R.drawable.ic_cloud_sync, context.getString(R.string.on_boarding_first_card_description), position)
            }
        }
    }

    override fun getCount(): Int = fragmentsNumber
}