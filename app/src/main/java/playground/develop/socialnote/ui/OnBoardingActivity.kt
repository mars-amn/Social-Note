package playground.develop.socialnote.ui

import android.content.Intent
import android.os.Bundle
import com.codemybrainsout.onboarder.AhoyOnboarderActivity
import com.codemybrainsout.onboarder.AhoyOnboarderCard
import playground.develop.socialnote.R
import playground.develop.socialnote.utils.Constants.Companion.APP_PREFERENCE_NAME
import playground.develop.socialnote.utils.Constants.Companion.FIRST_LAUNCH_KEY


class OnBoardingActivity : AhoyOnboarderActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startOnBoardingCards()
        setupOnBoardingCards()
    }

    private fun setupOnBoardingCards() {
        setGradientBackground()
        setFinishButtonTitle(getString(R.string.on_boarding_finish_button_title))
        setInactiveIndicatorColor(R.color.primaryColor)
        setActiveIndicatorColor(R.color.colorPrimary)
    }

    private fun startOnBoardingCards() {
        val welcomeCards = ArrayList<AhoyOnboarderCard>()
        welcomeCards.add(firstCard())
        welcomeCards.add(secondCard())
        welcomeCards.add(thirdCard())
        setOnboardPages(welcomeCards)
    }

    private fun firstCard(): AhoyOnboarderCard {
        val card = AhoyOnboarderCard(getString(R.string.on_boarding_first_card_title),
                                     getString(R.string.on_boarding_first_card_description),
                                     R.drawable.ic_cloud_sync)
        card.setTitleColor(R.color.reader_title_color)
        card.setDescriptionColor(R.color.network_state_text_color)
        return card
    }

    private fun secondCard(): AhoyOnboarderCard {
        val card = AhoyOnboarderCard(getString(R.string.on_boarding_second_card_title),
                                     getString(R.string.on_boarding_second_card_description),
                                     R.drawable.ic_notification_location)
        card.setTitleColor(R.color.reader_title_color)
        card.setDescriptionColor(R.color.network_state_text_color)
        return card
    }

    private fun thirdCard(): AhoyOnboarderCard {
        val card = AhoyOnboarderCard(getString(R.string.on_boarding_third_card_title),
                                     getString(R.string.on_boarding_third_card_description),
                                     R.drawable.ic_discussion)
        card.setTitleColor(R.color.reader_title_color)
        card.setDescriptionColor(R.color.network_state_text_color)
        return card
    }

    override fun onFinishButtonPressed() {
        saveUserFirstLaunch()
        startRegisterActivity()
    }

    private fun startRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveUserFirstLaunch() {
        val editor = getSharedPreferences(APP_PREFERENCE_NAME, MODE_PRIVATE).edit()
        editor.putBoolean(FIRST_LAUNCH_KEY, true)
        editor.apply()
    }
}
