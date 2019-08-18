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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.database.geofence.NoteGeofence
import elamien.abdullah.socialnote.database.notes.Note
import elamien.abdullah.socialnote.databinding.ActivityAddNoteBinding
import elamien.abdullah.socialnote.receiver.GeofenceReminderReceiver
import elamien.abdullah.socialnote.receiver.NoteReminderReceiver
import elamien.abdullah.socialnote.utils.Constants
import elamien.abdullah.socialnote.viewmodel.NoteViewModel
import org.koin.android.ext.android.inject
import org.wordpress.aztec.Aztec
import org.wordpress.aztec.AztecText
import org.wordpress.aztec.ITextFormat
import org.wordpress.aztec.toolbar.IAztecToolbarClickListener
import org.xml.sax.Attributes
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class AddEditNoteActivity : AppCompatActivity(), IAztecToolbarClickListener, EasyPermissions.PermissionCallbacks {

	private val mViewModel : NoteViewModel by inject()
	private lateinit var mBinding : ActivityAddNoteBinding
	private lateinit var editedNote : Note
	private var mGeofenceLocation : LatLng? = null
	private val locationsPermissions =
		arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

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

		if (isFromGeofenceReceiver()) {
			dismissGeofenceNotification()
		} else if (isOpenFromNotification()) {
			dismissNoteTimeReminderNotification()
		}

	}

	private fun setupToolbar(label : String) {
		supportActionBar?.title = label
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.setDisplayShowHomeEnabled(true)
	}

	private fun initEditorWithNote(noteId : Long) {
		mViewModel.getNote(noteId).observe(this, Observer<Note> { note ->
			if (note != null) {
				editedNote = note
				mBinding.aztec.fromHtml(editedNote.note.toString(), true)
				mBinding.noteTitleInputText.setText(note.noteTitle!!)
				if (note.geofence != null) {
					mGeofenceLocation =
						LatLng(note.geofence?.noteGeofenceLatitude!!, note.geofence?.noteGeofenceLongitude!!)
				}
			}

		})
	}

	private fun isFromGeofenceReceiver() : Boolean {
		return intent.getBooleanExtra(Constants.ACTIVITY_NOTE_GEOFENCE_NOTIFICATION_OPEN, false)
	}

	private fun dismissGeofenceNotification() {
		val intent = Intent(this@AddEditNoteActivity, GeofenceReminderReceiver::class.java)
		intent.action = Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION
		intent.putExtra(Constants.DISMISS_NOTE_GEOFENCE_NOTIFICATION,
				getIntent().getLongExtra(Constants.NOTE_INTENT_KEY, -1))
		PendingIntent.getBroadcast(this@AddEditNoteActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
		sendBroadcast(intent)
	}

	private fun isOpenFromNotification() =
		intent.getBooleanExtra(Constants.ACTIVITY_NOTE_TIMER_NOTIFICATION_OPEN, false)

	private fun dismissNoteTimeReminderNotification() {
		val intent = Intent(this@AddEditNoteActivity, NoteReminderReceiver::class.java)
		intent.action = Constants.DISMISS_NOTE_TIME_REMINDER_NOTIFICATION
		PendingIntent.getBroadcast(this@AddEditNoteActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
		sendBroadcast(intent)
	}


	override fun onCreateOptionsMenu(menu : Menu?) : Boolean {
		menuInflater.inflate(R.menu.add_note_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item : MenuItem) : Boolean {
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
		}
		return super.onOptionsItemSelected(item)
	}

	private fun onSaveMenuItemClick() {
		if (mBinding.aztec.toFormattedHtml() == "") {
			Toast.makeText(this@AddEditNoteActivity, getString(R.string.empty_editor_msg), Toast.LENGTH_LONG).show()
			return
		}

		val currentDate = Date()
		if (intent != null && intent.hasExtra(Constants.NOTE_INTENT_KEY)) {
			editedNote.dateModified = currentDate
			editedNote.note = mBinding.aztec.toFormattedHtml()
			editedNote.noteTitle = noteTitle()
			mViewModel.updateNote(editedNote)
			if (isTimeReminderNotNull()) {
				setupReminder(editedNote.note!!, editedNote.id!!)
			}
			if (isGeofenceLocationNotNull()) {
				val noteGeofence = NoteGeofence(mGeofenceLocation?.latitude, mGeofenceLocation?.longitude)
				editedNote.geofence = noteGeofence
				createNoteGeofence(editedNote.id!!)
			}
			navigateUp()
		} else {
			val note = Note(noteTitle(), mBinding.aztec.toFormattedHtml(), currentDate, currentDate)
			if (isGeofenceLocationNotNull()) {
				val noteGeofence = NoteGeofence(mGeofenceLocation?.latitude, mGeofenceLocation?.longitude)
				note.geofence = noteGeofence
			}
			mViewModel.insertNewNote(note).observe(this, Observer<Long> { noteId ->
				if (noteId != null) {
					if (isTimeReminderNotNull()) {
						setupReminder(mBinding.aztec.toFormattedHtml(), noteId)
					}
					if (isGeofenceLocationNotNull()) {
						createNoteGeofence(noteId)
					}
				}
				navigateUp()
			})
		}
	}

	private fun noteTitle() = mBinding.noteTitleInputText.text.toString()

	private fun navigateUp() {
		NavUtils.navigateUpFromSameTask(this@AddEditNoteActivity)
		finish()
	}

	private fun isTimeReminderNotNull() = mReminderDate != null

	private fun setupReminder(body : String, id : Long) {
		val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
		val alarmIntent = Intent(this@AddEditNoteActivity, NoteReminderReceiver::class.java).let { intent ->
			intent.action = Constants.NOTE_TIME_REMINDER_ACTION
			intent.putExtra(Constants.NOTE_INTENT_ID, id)
			intent.putExtra(Constants.NOTE_NOTIFICATION_TEXT_INTENT_KEY, body)
			PendingIntent.getBroadcast(this@AddEditNoteActivity, 0, intent, 0)
		}
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, mReminderDate?.time!!, alarmIntent)
	}

	private fun isGeofenceLocationNotNull() = mGeofenceLocation != null

	private fun createNoteGeofence(id : Long) {
		addGeofence(createGeofenceRequest(getGeofenceBuilder(id)), id)
	}

	@SuppressLint("MissingPermission")
	fun addGeofence(geofencingRequest : GeofencingRequest, id : Long) {
		val client = LocationServices.getGeofencingClient(this@AddEditNoteActivity)
		client.addGeofences(geofencingRequest, createGeofencePendingIntent(id)).addOnSuccessListener { }
				.addOnFailureListener { }
	}

	private fun createGeofencePendingIntent(id : Long) : PendingIntent {
		val intent = Intent(this@AddEditNoteActivity, GeofenceReminderReceiver::class.java)
		intent.action = Constants.NOTE_GEOFENCE_REMINDER_ACTION
		intent.putExtra(Constants.NOTE_GEOFENCE_REMINDER_ID_INTENT_KEY, id)
		return PendingIntent.getBroadcast(this@AddEditNoteActivity,
				id.toInt(),
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT)
	}

	private fun createGeofenceRequest(geofence : Geofence) : GeofencingRequest {
		return GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
				.addGeofence(geofence).build()
	}

	private fun getGeofenceBuilder(id : Long) : Geofence {
		return Geofence.Builder().setRequestId("geo_fence_reminder_$id")
				.setCircularRegion(mGeofenceLocation?.latitude!!,
						mGeofenceLocation?.longitude!!,
						Constants.GEOFENCE_REMINDER_RADIUS).setExpirationDuration(Constants.GEOFENCE_EXPIRE_DATE)
				.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT).build()
	}

	private fun showUnsavedNoteDialog() {
		MaterialAlertDialogBuilder(this@AddEditNoteActivity).setTitle(getString(R.string.back_button_dialog_title))
				.setMessage(getString(R.string.back_button_dialog_msg_ptI) + "\n" + getString(R.string.back_button_dialog_msg_ptII))
				.setPositiveButton(getString(R.string.back_button_dialog_positive_button_label)) { dialog, id ->
					dialog.dismiss()
					navigateUp()
				}.setNegativeButton(getString(R.string.back_button_dialog_negative_button_label)) { dialog, id ->
					dialog.dismiss()
				}.show()
	}

	override fun onBackPressed() {
		if (mBinding.aztec.toFormattedHtml() == "") {
			super.onBackPressed()
		} else {
			showUnsavedNoteDialog()
		}
	}

	private var mReminderDate : Date? = null

	fun onSetReminderClick(view : View) {
		SingleDateAndTimePickerDialog.Builder(this@AddEditNoteActivity).title("Pick Date").displayYears(false)
				.displayDays(true).displayHours(true).displayMinutes(true).minutesStep(1)
				.mainColor(ContextCompat.getColor(this@AddEditNoteActivity, R.color.secondaryColor)).mustBeOnFuture()
				.listener { pickedDate ->
					if (pickedDate != null) {
						mReminderDate = pickedDate
					}
				}.display()
	}

	fun onLocationReminderClick(view : View) {
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

	override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == GEOFENCE_NOTE_REMINDER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
			mGeofenceLocation = data.getParcelableExtra<LatLng>(Constants.NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY)
		}
	}


	override fun onRequestPermissionsResult(requestCode : Int,
											permissions : Array<out String>,
											grantResults : IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
	}

	override fun onPermissionsDenied(requestCode : Int, perms : MutableList<String>) {
	}

	override fun onPermissionsGranted(requestCode : Int, perms : MutableList<String>) {
		if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
			startMapActivity()
		}
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

	companion object {
		private const val LOCATION_PERMISSION_REQUEST_CODE = 252
		private const val GEOFENCE_NOTE_REMINDER_REQUEST_CODE = 7
	}
}
