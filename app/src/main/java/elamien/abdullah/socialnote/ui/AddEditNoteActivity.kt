package elamien.abdullah.socialnote.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.database.Note
import elamien.abdullah.socialnote.databinding.ActivityAddNoteBinding
import elamien.abdullah.socialnote.receiver.NoteReminderReceiver
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
        mBinding.handlers = this
        Aztec.with(mBinding.aztec, mBinding.source, mBinding.formattingToolbar, this)
        if (intent != null && intent.hasExtra(Constants.NOTE_INTENT_KEY)) {
            setupToolbar(label = getString(R.string.edit_note_toolbar_label))
            initEditorWithNote(intent.getLongExtra(Constants.NOTE_INTENT_KEY, -1))
        } else {
            setupToolbar(label = getString(R.string.add_note_label))
        }

        if (isOpenFromNotification()) {
            dismissTheNotification()
        }
    }

    private fun isOpenFromNotification() = intent.getBooleanExtra(Constants.ACTIVITY_NOTIFICATION_OPEN, false)

    private fun dismissTheNotification() {
        val intent = Intent(this@AddEditNoteActivity, NoteReminderReceiver::class.java)
        intent.action = Constants.DISMISS_NOTIFICATION_ACTION
        PendingIntent.getBroadcast(this@AddEditNoteActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        sendBroadcast(intent)
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
            if (mReminderDate != null) {
                setupReminder(editedNote.id, editedNote.note!!)
            }
            navigateUp()
        } else {
            val note = Note(noteTitle(), mBinding.aztec.toFormattedHtml(), currentDate, currentDate)
            mViewModel.insertNewNote(note).observe(
                this, Observer<Long> {
                    if (mReminderDate != null) {
                        setupReminder(it, mBinding.aztec.toFormattedHtml())
                    }
                    navigateUp()
                })
        }
    }

    private fun setupReminder(id : Long?, body : String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this@AddEditNoteActivity, NoteReminderReceiver::class.java)
            .let { intent ->
                intent.action = Constants.OPEN_NOTE_INTENT_ACTION
                intent.putExtra(Constants.NOTE_INTENT_ID, id)
                intent.putExtra(Constants.NOTE_NOTIFICATION_TEXT_INTENT_KEY, body)
                PendingIntent.getBroadcast(this@AddEditNoteActivity, 0, intent, 0)
            }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, mReminderDate?.time!!, alarmIntent)
    }

    private fun noteTitle() = mBinding.noteTitleInputText.text.toString()

    private fun navigateUp() {
        NavUtils.navigateUpFromSameTask(this@AddEditNoteActivity)
        finish()
    }

    private var mReminderDate : Date? = null

    fun onSetReminderClick(view : View) {
        SingleDateAndTimePickerDialog.Builder(this@AddEditNoteActivity)
            .title("Pick Date")
            .displayYears(false)
            .displayDays(true)
            .displayHours(true)
            .displayMinutes(true)
            .minutesStep(1)
            .mainColor(ContextCompat.getColor(this@AddEditNoteActivity, R.color.secondaryColor))
            .mustBeOnFuture()
            .listener {
                mReminderDate = it
            }.display()
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
