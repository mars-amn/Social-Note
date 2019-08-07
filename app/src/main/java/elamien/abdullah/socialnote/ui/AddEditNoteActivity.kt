package elamien.abdullah.socialnote.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.database.Note
import elamien.abdullah.socialnote.databinding.ActivityAddNoteBinding
import elamien.abdullah.socialnote.utils.Constants
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import org.koin.android.ext.android.inject
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.xml.sax.Attributes
import java.util.*


class AddEditNoteActivity : AppCompatActivity(), IAztecToolbarClickListener {
    private val mViewModel : NoteViewModel by inject()
    private lateinit var mBinding : ActivityAddNoteBinding
    private lateinit var editedNote : Note

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_note)
        Aztec.with(mBinding.aztec, mBinding.source, mBinding.formattingToolbar, this)
        if (intent != null && intent.hasExtra(Constants.NOTE_INTENT_KEY)) {
            setupToolbar(label = getString(R.string.edit_note_toolbar_label))
            initEditorWithNote(intent.getLongExtra(Constants.NOTE_INTENT_KEY, -1))
        } else {
            setupToolbar(label = getString(R.string.add_note_label))
        }
    }


    private fun initEditorWithNote(noteId : Long) {
        mViewModel.getNote(noteId).observe(this,
            Observer<Note> {
                editedNote = it
                mBinding.aztec.fromHtml(editedNote.note.toString(), true)
            })
    }

    private fun setupToolbar(label : String) {
        supportActionBar?.title = label
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onCreateOptionsMenu(menu : Menu?) : Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        when (item.itemId) {
            R.id.saveNoteMenuItem -> onSaveMenuItemClick()
            android.R.id.home -> {
                showUnsavedNoteDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showUnsavedNoteDialog() {
        MaterialAlertDialogBuilder(this@AddEditNoteActivity)
            .setTitle(getString(R.string.back_button_dialog_title))
            .setMessage(
                getString(R.string.back_button_dialog_msg_ptI) + "\n" +
                        getString(R.string.back_button_dialog_msg_ptII)
            )
            .setPositiveButton(getString(R.string.back_button_dialog_positive_button_label)) { dialog, id ->
                dialog.dismiss()
                navigateUp()
            }
            .setNegativeButton(getString(R.string.back_button_dialog_negative_button_label)) { dialog, id
                ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onBackPressed() {
        showUnsavedNoteDialog()
    }

    private fun onSaveMenuItemClick() {
        val currentDate = Date()
        if (intent != null && intent.hasExtra(Constants.NOTE_INTENT_KEY)) {
            editedNote.dateModified = currentDate
            editedNote.note = mBinding.aztec.toFormattedHtml()
            editedNote.noteTitle = noteTitle()
            mViewModel.updateNote(editedNote)
            navigateUp()
        } else {
            val note = Note(noteTitle(), mBinding.aztec.toFormattedHtml(), currentDate, currentDate)
            mViewModel.insertNewNote(note).observe(
                this, Observer<Long> {
                    navigateUp()
                })
        }
    }

    private fun noteTitle() = mBinding.noteTitleInputText.text.toString()

    private fun navigateUp() {
        NavUtils.navigateUpFromSameTask(this@AddEditNoteActivity)
        finish()
    }

    override fun onToolbarHtmlButtonClicked() {
        val uploadingPredicate = object : AztecText.AttributePredicate {
            override fun matches(attrs : Attributes) : Boolean {
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

    override fun onToolbarMediaButtonClicked() : Boolean {
        return false
    }

    override fun onToolbarCollapseButtonClicked() {
    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarFormatButtonClicked(format : ITextFormat, isKeyboardShortcut : Boolean) {
    }

    override fun onToolbarHeadingButtonClicked() {
    }
}
