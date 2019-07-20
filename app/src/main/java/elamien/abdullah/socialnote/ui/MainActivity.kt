package elamien.abdullah.socialnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.handlers = this
        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar)
        title = getString(R.string.app_name)
    }

    fun onNewNoteFabClick(view: View) {
        val intent = Intent(this@MainActivity, AddNoteActivity::class.java)
        startActivity(intent)
    }
}
