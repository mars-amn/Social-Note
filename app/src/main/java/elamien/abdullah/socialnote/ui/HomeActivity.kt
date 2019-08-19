package elamien.abdullah.socialnote.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.google.android.material.navigation.NavigationView
import com.miguelcatalan.materialsearchview.MaterialSearchView
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.adapter.PagedNoteListAdapter
import elamien.abdullah.socialnote.database.notes.Note
import elamien.abdullah.socialnote.databinding.ActivityHomeBinding
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity(), MaterialSearchView.OnQueryTextListener,
	NavigationView.OnNavigationItemSelectedListener {

	private lateinit var adapter : PagedNoteListAdapter
	private lateinit var mBinding : ActivityHomeBinding
	private val mViewModel : NoteViewModel by inject()
	private val mDisposables = CompositeDisposable()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		mBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
		mBinding.handlers = this
		setupToolbar()
		setupNavDrawer()
		adapter = PagedNoteListAdapter(this@HomeActivity)
		loadNotes()
		setupSearchView()
	}

	private fun setupNavDrawer() {
		val toggle = actionBarDrawerToggle
		mBinding.drawerLayout.addDrawerListener(toggle)
		mBinding.navigationView.setNavigationItemSelectedListener(this@HomeActivity)
		toggle.syncState()

	}

	private val actionBarDrawerToggle : ActionBarDrawerToggle
		get() {
			mBinding.navigationView.itemIconTintList = null
			setSupportActionBar(mBinding.toolbar)
			return object : ActionBarDrawerToggle(this,
					mBinding.drawerLayout,
					mBinding.toolbar,
					R.string.nav_drawer_open,
					R.string.nav_drawer_close) {
				override fun onDrawerSlide(drawerView : View, slideOffset : Float) {
					super.onDrawerSlide(drawerView, slideOffset)

					val moveFactor = mBinding.drawerLayout.width * slideOffset
					mBinding.homeContainer.translationX = moveFactor
					super.onDrawerSlide(drawerView, slideOffset)
				}
			}
		}

	private fun setupToolbar() {
		mBinding.toolbar.setNavigationIcon(R.drawable.ic_home_nav)
		setSupportActionBar(mBinding.toolbar)
		title = getString(R.string.app_name)
	}

	private fun loadNotes() {
		mViewModel.loadPagedNotes()
				.observe(this, Observer<PagedList<Note>> { list ->
					if (list.isNotEmpty()) {
						addNotesToRecyclerView(list)
					} else {
						hideRecyclerView()
					}
				})
	}

	private fun addNotesToRecyclerView(list : PagedList<Note>) {
		showRecyclerView()
		adapter.submitList(list)
		mBinding.notesRecyclerView.adapter = adapter
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
		mDisposables.add(searchSubject.debounce(700,
				TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe { query ->
			searchNotes(query)
		})
		return false
	}

	private fun searchNotes(query : String?) {
		mViewModel.searchForNote("%$query%")
				.observe(this@HomeActivity, Observer<PagedList<Note>> { list ->
					if (list.isNotEmpty()) {
						applySearchResults(list)
					}
				})
	}

	private fun applySearchResults(list : PagedList<Note>) {
		adapter.submitList(list)
	}

	override fun onStop() {
		mDisposables.dispose()
		super.onStop()
	}

	override fun onNavigationItemSelected(item : MenuItem) : Boolean {
		when (item.itemId) {
			R.id.settingsMenuItem -> openSettingsActivity()
		}
		return true
	}

	private fun openSettingsActivity() {
		startActivity(Intent(this@HomeActivity, SettingsActivity::class.java))
	}

	override fun onBackPressed() {
		when {
			mBinding.searchView.isSearchOpen -> mBinding.searchView.closeSearch()
			mBinding.drawerLayout.isDrawerOpen(GravityCompat.START) -> mBinding.drawerLayout.closeDrawer(GravityCompat.START)
			else -> super.onBackPressed()
		}
	}

}

