package elamien.abdullah.socialnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.PagedNoteListAdapter
import elamien.abdullah.socialnote.database.Note
import elamien.abdullah.socialnote.databinding.ActivityMainBinding
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityMainBinding
    private val mViewModel : NoteViewModel by inject()
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.handlers = this
        setupToolbar()
        loadNotes()
    }

    private fun loadNotes() {
        mViewModel.loadPagedNotes().observe(this,
            Observer<PagedList<Note>> { t ->
                if (t.isNotEmpty()) {
                    addNotesToRecyclerView(t)
                } else {
                    hideRecyclerView()
                }
            })
    }

    private fun hideRecyclerView() {
        mBinding.notesRecyclerView.visibility = View.GONE
        mBinding.noteEmptyStateLayout.visibility = View.VISIBLE
    }

    private fun addNotesToRecyclerView(t : PagedList<Note>?) {
        showRecyclerView()
        val adapter = PagedNoteListAdapter(this@MainActivity)
        adapter.submitList(t)
        mBinding.notesRecyclerView.adapter = AlphaInAnimationAdapter(adapter)
    }

    private fun showRecyclerView() {
        mBinding.lottieAnimationView.pauseAnimation()
        mBinding.noteEmptyStateLayout.visibility = View.GONE
        mBinding.notesRecyclerView.visibility = View.VISIBLE
    }

    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar)
        title = getString(R.string.app_name)
    }

    fun onNewNoteFabClick(view : View) {
        val intent = Intent(this@MainActivity, AddNoteActivity::class.java)
        startActivity(intent)
    }
}
