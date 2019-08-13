package elamien.abdullah.socialnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.miguelcatalan.materialsearchview.MaterialSearchView
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.PagedNoteListAdapter
import elamien.abdullah.socialnote.database.Note
import elamien.abdullah.socialnote.databinding.ActivityHomeBinding
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity(), MaterialSearchView.OnQueryTextListener {

    private lateinit var adapter : PagedNoteListAdapter
    private lateinit var mBinding : ActivityHomeBinding
    private val mViewModel : NoteViewModel by inject()
    private val mDisposables = CompositeDisposable()
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        mBinding.handlers = this
        adapter = PagedNoteListAdapter(this@HomeActivity)
        setupToolbar()
        loadNotes()
        setupSearchView()
    }


    private fun setupToolbar() {
        setSupportActionBar(mBinding.toolbar)
        title = getString(R.string.app_name)
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
        adapter.setHasStableIds(true)
        adapter.submitList(t)
        adapter.notifyDataSetChanged()
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

    override fun onCreateOptionsMenu(menu : Menu?) : Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        val searchItem = menu!!.findItem(R.id.searchMenuItem)
        mBinding.searchView.setMenuItem(searchItem)
        return super.onCreateOptionsMenu(menu)
    }

    fun onNewNoteFabClick(view : View) {
        val intent = Intent(this@HomeActivity, AddEditNoteActivity::class.java)
        startActivity(intent)
    }

    fun deleteNote(note : Note?) {
        mViewModel.deleteNote(note)
    }

    private fun setupSearchView() {
        mBinding.searchView.setOnQueryTextListener(this@HomeActivity)
    }

    override fun onQueryTextSubmit(query : String?) : Boolean {
        return false
    }

    override fun onQueryTextChange(newText : String?) : Boolean {
        val searchSubject = BehaviorSubject.create<String>()
        searchSubject.onNext(newText!!)
        mDisposables.add(searchSubject
            .debounce(700, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query ->
                searchNotes(query)
            })
        return false
    }

    private fun searchNotes(query : String?) {
        mViewModel.searchForNote("%$query%").observe(this@HomeActivity, Observer<PagedList<Note>> {
            if (it.isNotEmpty()) {
                applySearchResults(it)
            }
        })
    }

    private fun applySearchResults(it : PagedList<Note>) {
        adapter.submitList(it)
    }

    override fun onStop() {
        mDisposables.dispose()
        super.onStop()
    }

    override fun onBackPressed() {
        if (mBinding.searchView.isSearchOpen) {
            mBinding.searchView.closeSearch()
        } else {
            super.onBackPressed()
        }
    }


}

