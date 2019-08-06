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
import elamien.abdullah.socialnote.databinding.ActivityHomeBinding
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import org.koin.android.ext.android.inject

class HomeActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityHomeBinding
    private val mViewModel : NoteViewModel by inject()

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        mBinding.handlers = this
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

    private fun addNotesToRecyclerView(t : PagedList<Note>?) {
        showRecyclerView()
        val adapter = PagedNoteListAdapter(this@HomeActivity)
        adapter.setHasStableIds(true)
        adapter.submitList(t)
        mBinding.notesRecyclerView.adapter = AlphaInAnimationAdapter(adapter)
    }

    private fun hideRecyclerView() {
        mBinding.notesRecyclerView.visibility = View.GONE
        mBinding.noteEmptyStateLayout.visibility = View.VISIBLE
    }


    private fun showRecyclerView() {
        mBinding.lottieAnimationView.pauseAnimation()
        mBinding.noteEmptyStateLayout.visibility = View.GONE
        mBinding.notesRecyclerView.visibility = View.VISIBLE
    }

    fun onNewNoteFabClick(view : View) {
        val intent = Intent(this@HomeActivity, AddEditNoteActivity::class.java)
        startActivity(intent)
    }

    fun deleteNote(note : Note?) {
        mViewModel.deleteNote(note)
    }

}
