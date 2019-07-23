package elamien.abdullah.socialnote.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.database.Note
import elamien.abdullah.socialnote.databinding.ActivityAddNoteBinding
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import org.koin.android.ext.android.inject
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.xml.sax.Attributes
import java.util.*


class AddNoteActivity : AppCompatActivity(), IAztecToolbarClickListener {
    private val mViewModel: NoteViewModel by inject()

    override fun onToolbarCollapseButtonClicked() {
    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
    }

    override fun onToolbarHeadingButtonClicked() {
    }

    override fun onToolbarHtmlButtonClicked() {
        val uploadingPredicate = object : AztecText.AttributePredicate {
            override fun matches(attrs: Attributes): Boolean {
                return attrs.getIndex("uploading") > -1
            }
        }

        val mediaPending = mBinding.aztec.getAllElementAttributes(uploadingPredicate).isNotEmpty()

        if (mediaPending) {
        } else {
            mBinding.formattingToolbar.toggleEditorMode()
        }
    }

    override fun onToolbarListButtonClicked() {
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }

    private lateinit var mBinding: ActivityAddNoteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_note)
        Aztec.with(mBinding.aztec, mBinding.source, mBinding.formattingToolbar, this)
        setupToolbar()
    }

    private fun setupToolbar() {
        supportActionBar?.title = getString(R.string.add_note_label)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveNoteMenuItem -> onSaveMenuItemClick()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSaveMenuItemClick() {
        val currentDate = Date()
        val note = Note("", mBinding.aztec.toFormattedHtml(), currentDate, currentDate)
        mViewModel.insertNewNote(note).observe(
            this, Observer<Long> {
                Toast.makeText(this@AddNoteActivity, "" + it, Toast.LENGTH_LONG).show()
            })
    }

}
