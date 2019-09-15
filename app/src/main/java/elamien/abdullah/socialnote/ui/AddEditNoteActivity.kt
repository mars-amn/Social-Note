package elamien.abdullah.socialnote.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.database.local.geofence.NoteGeofence
import elamien.abdullah.socialnote.database.local.notes.Note
import elamien.abdullah.socialnote.database.local.reminder.NoteReminder
import elamien.abdullah.socialnote.databinding.ActivityAddNoteBinding
import elamien.abdullah.socialnote.receiver.GeofenceReminderReceiver
import elamien.abdullah.socialnote.receiver.NoteReminderReceiver
import elamien.abdullah.socialnote.services.SyncingService
import elamien.abdullah.socialnote.utils.Constants
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import org.koin.android.ext.android.inject
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class AddEditNoteActivity : AppCompatActivity(), IAztecToolbarClickListener,
                            EasyPermissions.PermissionCallbacks {

    private val mViewModel: NoteViewModel by inject()
    private lateinit var mBinding: ActivityAddNoteBinding
    private lateinit var mExistedNote: Note
    private var mGeofenceLocation: LatLng? = null
    private var mReminderDate: Date? = null
    private val locationsPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                                               Manifest.permission.ACCESS_COARSE_LOCATION)

    private var isGeofence = false
    private var isReminder = false
    private var isSyncingEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_note)
        mBinding.handlers = this
        initEditor()
        if (intent != null && intent.hasExtra(Constants.NOTE_INTENT_KEY)) {
            setupToolbar(label = getString(R.string.edit_note_toolbar_label))
            initEditorWithNote(intent.getLongExtra(Constants.NOTE_INTENT_KEY, -1))
        } else {
            setupToolbar(label = getString(R.string.add_note_label))
        }

        setupSyncing()

        if (isFromGeofenceReceiver()) {
            dismissGeofenceNotification()
        } else if (isOpenFromNotification()) {
            dismissNoteTimeReminderNotification()
        }
    }

    private fun initEditor() {
        Aztec.with(mBinding.aztec, mBinding.source, mBinding.formattingToolbar, this)
        mBinding.aztec.setCalypsoMode(false)
        mBinding.source.setCalypsoMode(false)
    }

    private fun setupToolbar(label: String) {
        supportActionBar?.title = label
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initEditorWithNote(noteId: Long) {
        mViewModel.getNote(noteId).observe(this, Observer<Note> { note ->
            if (note != null) {
                mExistedNote = note
                mBinding.aztec.fromHtml(mExistedNote.note.toString(), true)
                mBinding.noteTitleInputText.setText(note.noteTitle!!)
                if (note.geofence != null) {
                    isGeofence = true
                    mGeofenceLocation = LatLng(note.geofence?.noteGeofenceLatitude!!,
                                               note.geofence?.noteGeofenceLongitude!!)
                }
            }

        })
    }

    private fun setupSyncing() {
        val settings = PreferenceManager.getDefaultSharedPreferences(this@AddEditNoteActivity)
        isSyncingEnabled = settings.getBoolean(getString(R.string.note_sync_key), false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveNoteMenuItem -> onSaveMenuItemClick()
            android.R.id.home -> {
                if (mBinding.aztec.toFormattedHtml() == "") {
                    navigateUp()
                } else {
                    showUnsavedNoteDialog()
                }
                return true
            }
            R.id.noteTimeReminderMenuItem -> {
                onTimeReminderClick()
            }
            R.id.noteGeofenceReminderMenuItem -> {
                onLocationReminderClick()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSaveMenuItemClick() {
        if (mBinding.aztec.toFormattedHtml() == "") {
            Toast.makeText(this@AddEditNoteActivity,
                           getString(R.string.empty_editor_msg),
                           Toast.LENGTH_LONG).show()
            return
        }
        val currentDate = Date()
        if (intent != null && intent.hasExtra(Constants.NOTE_INTENT_KEY)) {
            updateNote(currentDate)
        } else {
            insertNewNote(currentDate)
        }
    }

    private fun updateNote(currentDate: Date) {
        mExistedNote.dateModified = currentDate
        mExistedNote.note = mBinding.aztec.toFormattedHtml()
        mExistedNote.noteTitle = noteTitle()
        if (isReminder) {
            mExistedNote.timeReminder = getTimeReminder()
            setupReminder(mExistedNote.note!!, mExistedNote.id!!)
        }
        if (isGeofence) {
            mExistedNote.geofence = getGeofenceLocation()
            createNoteGeofence(mExistedNote.id!!)
        }
        if (isSyncingEnabled) {
            startSyncService(mExistedNote.id!!, Constants.SYNC_UPDATE_NOTE_INTENT_ACTION)
        } else {
            mExistedNote.isNeedUpdate = true
        }
        mViewModel.updateNote(mExistedNote)
        navigateUp()
    }

    private fun noteTitle() = mBinding.noteTitleInputText.text.toString()

    private fun setupReminder(body: String, id: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this@AddEditNoteActivity, NoteReminderReceiver::class.java)
                .let { intent ->
                    intent.action = Constants.NOTE_TIME_REMINDER_ACTION
                    intent.putExtra(Constants.NOTE_INTENT_KEY, id)
                    intent.putExtra(Constants.NOTE_NOTIFICATION_TEXT_INTENT_KEY, body)
                    PendingIntent.getBroadcast(this@AddEditNoteActivity,
                                               id.toInt(),
                                               intent,
                                               PendingIntent.FLAG_UPDATE_CURRENT)
                }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, mReminderDate?.time!!, alarmIntent)
    }

    private fun getTimeReminder() = NoteReminder(mReminderDate?.time)

    private fun getGeofenceLocation() = NoteGeofence(mGeofenceLocation?.latitude,
                                                     mGeofenceLocation?.longitude)

    private fun createNoteGeofence(id: Long) {
        addGeofence(createGeofenceRequest(getGeofenceBuilder(id)), id)
    }

    private fun startSyncService(noteId: Long, action: String) {
        val syncIntent = Intent(this@AddEditNoteActivity, SyncingService::class.java)
        syncIntent.action = action
        syncIntent.putExtra(Constants.SYNC_NOTE_ID_INTENT_KEY, noteId)
        SyncingService.getSyncingService().enqueueSyncNewNoteService(this, syncIntent)
    }


    private fun insertNewNote(currentDate: Date) {
        val note = Note(noteTitle(), mBinding.aztec.toFormattedHtml(), currentDate, currentDate)
        note.id = Date().time
        if (isGeofence) {
            note.geofence = getGeofenceLocation()
        }
        if (isReminder) {
            note.timeReminder = getTimeReminder()
        }
        mViewModel.insertNewNote(note).observe(this, Observer<Long> { noteId ->
            if (noteId != null) {
                if (isReminder) {
                    setupReminder(mBinding.aztec.toFormattedHtml(), noteId)
                }
                if (isGeofence) {
                    createNoteGeofence(noteId)
                }
                if (isSyncingEnabled) {
                    startSyncService(noteId, Constants.SYNC_NEW_NOTE_INTENT_ACTION)
                }
            }
            navigateUp()
        })
    }

    private fun showUnsavedNoteDialog() {
        MaterialAlertDialogBuilder(this@AddEditNoteActivity)
                .setTitle(getString(R.string.back_button_dialog_title))
                .setMessage(getString(R.string.back_button_dialog_msg_ptI) + "\n" + getString(R.string.back_button_dialog_msg_ptII))
                .setPositiveButton(getString(R.string.back_button_dialog_positive_button_label)) { dialog, id ->
                    dialog.dismiss()
                    navigateUp()
                }
                .setNegativeButton(getString(R.string.back_button_dialog_negative_button_label)) { dialog, id ->
                    dialog.dismiss()
                }.show()
    }

    private fun navigateUp() {
        NavUtils.navigateUpFromSameTask(this@AddEditNoteActivity)
        finish()
    }


    @SuppressLint("MissingPermission")
    fun addGeofence(geofencingRequest: GeofencingRequest, id: Long) {
        val client = LocationServices.getGeofencingClient(this@AddEditNoteActivity)
        client.addGeofences(geofencingRequest, createGeofencePendingIntent(id))
                .addOnSuccessListener { }.addOnFailureListener { }
    }

    private fun createGeofencePendingIntent(id: Long): PendingIntent {
        val intent = Intent(this@AddEditNoteActivity, GeofenceReminderReceiver::class.java)
        intent.action = Constants.NOTE_GEOFENCE_REMINDER_ACTION
        intent.putExtra(Constants.NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY, id)
        return PendingIntent.getBroadcast(this@AddEditNoteActivity,
                                          id.toInt(),
                                          intent,
                                          PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).addGeofence(geofence)
                .build()
    }

    private fun getGeofenceBuilder(id: Long): Geofence {
        return Geofence.Builder().setRequestId("geo_fence_reminder_$id").setCircularRegion(
            mGeofenceLocation?.latitude!!,
            mGeofenceLocation?.longitude!!,
            Constants.GEOFENCE_REMINDER_RADIUS)
                .setExpirationDuration(Constants.GEOFENCE_EXPIRE_DATE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }

    private fun onTimeReminderClick() {
        SingleDateAndTimePickerDialog.Builder(this@AddEditNoteActivity).title("Pick Date")
                .displayYears(false).displayDays(true).displayHours(true).displayMinutes(true)
                .minutesStep(1)
                .mainColor(ContextCompat.getColor(this@AddEditNoteActivity, R.color.secondaryColor))
                .mustBeOnFuture().listener { pickedDate ->
                    if (pickedDate != null) {
                        isReminder = true
                        mReminderDate = pickedDate
                    }
                }.display()
    }

    private fun onLocationReminderClick() {
        if (!EasyPermissions.hasPermissions(this@AddEditNoteActivity, *locationsPermissions)) {
            EasyPermissions.requestPermissions(this@AddEditNoteActivity,
                                               getString(R.string.request_location_rational_dialog),
                                               LOCATION_PERMISSION_REQUEST_CODE,
                                               *locationsPermissions)
        } else {
            startMapActivity()
        }
    }

    private fun startMapActivity() {
        val intent = Intent(this@AddEditNoteActivity, GeofencePickerActivity::class.java)
        if (mGeofenceLocation != null) {
            intent.putExtra(Constants.NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY, mGeofenceLocation)
        }
        startActivityForResult(intent, GEOFENCE_NOTE_REMINDER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GEOFENCE_NOTE_REMINDER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            isGeofence = true
            mGeofenceLocation = data
                    .getParcelableExtra<LatLng>(Constants.NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            startMapActivity()
        }
    }

    private fun isFromGeofenceReceiver(): Boolean {
        return intent.getBooleanExtra(Constants.ACTIVITY_NOTE_GEOFENCE_NOTIFICATION_OPEN, false)
    }

    private fun dismissGeofenceNotification() {
        val intent = Intent(this@AddEditNoteActivity, GeofenceReminderReceiver::class.java)
        intent.action = Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION
        intent.putExtra(Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION,
                        getIntent().getLongExtra(Constants.NOTE_INTENT_KEY, -1))
        PendingIntent.getBroadcast(this@AddEditNoteActivity,
                                   0,
                                   intent,
                                   PendingIntent.FLAG_UPDATE_CURRENT)
        sendBroadcast(intent)
    }

    private fun isOpenFromNotification() = intent.getBooleanExtra(Constants.ACTIVITY_NOTE_TIMER_NOTIFICATION_OPEN,
                                                                  false)

    private fun dismissNoteTimeReminderNotification() {
        val intent = Intent(this@AddEditNoteActivity, NoteReminderReceiver::class.java)
        intent.action = Constants.DISMISS_NOTE_TIME_REMINDER_NOTIFICATION
        intent.putExtra(Constants.NOTE_INTENT_KEY,
                        getIntent().getLongExtra(Constants.NOTE_INTENT_KEY, -1))
        PendingIntent.getBroadcast(this@AddEditNoteActivity,
                                   0,
                                   intent,
                                   PendingIntent.FLAG_UPDATE_CURRENT)
        sendBroadcast(intent)
    }

    override fun onBackPressed() {
        if (mBinding.aztec.toFormattedHtml() == "") {
            super.onBackPressed()
        } else {
            showUnsavedNoteDialog()
        }
    }

    override fun onToolbarHtmlButtonClicked() {

    }

    override fun onToolbarListButtonClicked() {
    }

    override fun onToolbarMediaButtonClicked(): Boolean {
        return false
    }

    override fun onToolbarCollapseButtonClicked() {
    }

    override fun onToolbarExpandButtonClicked() {
    }

    override fun onToolbarFormatButtonClicked(format: ITextFormat, isKeyboardShortcut: Boolean) {
    }

    override fun onToolbarHeadingButtonClicked() {
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 252
        private const val GEOFENCE_NOTE_REMINDER_REQUEST_CODE = 7
    }
}
