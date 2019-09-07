package elamien.abdullah.socialnote.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.services.SyncingService
import elamien.abdullah.socialnote.ui.RegisterActivity
import elamien.abdullah.socialnote.utils.Constants
import org.koin.android.ext.android.inject


class SettingsFragment : PreferenceFragmentCompat(),
	SharedPreferences.OnSharedPreferenceChangeListener {

	val mFirebaseAuth : FirebaseAuth by inject()
	override fun onCreatePreferences(savedInstanceState : Bundle?, rootKey : String?) {
		addPreferencesFromResource(R.xml.settings)
		registerPreferenceListener()
	}


	override fun onStop() {
		super.onStop()
		unregisterPreferenceChangeListener()
	}

	private fun unregisterPreferenceChangeListener() {
		PreferenceManager.getDefaultSharedPreferences(context)
				.unregisterOnSharedPreferenceChangeListener(this)
	}

	private fun registerPreferenceListener() {
		PreferenceManager.getDefaultSharedPreferences(context)
				.registerOnSharedPreferenceChangeListener(this)
	}

	override fun onSharedPreferenceChanged(sharedPreferences : SharedPreferences?, key : String?) {
		if (key == getString(R.string.note_sync_key)) {
			val isEnabled = sharedPreferences?.getBoolean(key, false)!!
			if (isEnabled && mFirebaseAuth.currentUser == null) {
				val switchPreference = findPreference(key) as SwitchPreferenceCompat
				switchPreference.isChecked = false
				showRegisterRequestDialog()
				return
			} else if (isEnabled && mFirebaseAuth.currentUser != null) {
				startSyncService()
			}
		}
	}

	private fun startSyncService() {
		setupNeededSyncUpdatesNotes()
		setupSyncingNotes()
		getSyncedNotes()
	}

	private fun getSyncedNotes() {
		val syncService = Intent(context, SyncingService::class.java)
		syncService.action = Constants.SYNC_CALL_NOTES_POPULATE_ROOM_INTENT_ACTION
		SyncingService.getSyncingService()
				.enqueueCallSyncedNotes(context!!, syncService)
	}

	private fun setupNeededSyncUpdatesNotes() {
		val syncService = Intent(context, SyncingService::class.java)
		syncService.action = Constants.SYNC_NEEDED_UPDATES_NOTES_INTENT_ACTION
		SyncingService.getSyncingService()
				.enqueueSyncNeededUpdateNotes(context!!, syncService)

	}

	private fun setupSyncingNotes() {
		val syncService = Intent(context, SyncingService::class.java)
		syncService.action = Constants.SYNC_ALL_NOTES_INTENT_ACTION
		SyncingService.getSyncingService()
				.enqueueSyncAllNotes(context!!, syncService)
	}

	private fun showRegisterRequestDialog() {
		MaterialAlertDialogBuilder(context).setTitle(getString(R.string.sync_notes_dialog_title))
				.setMessage(getString(R.string.sync_notes_dialog_message))
				.setNegativeButton(getString(R.string.sync_notes_dialog_neg_button_label)) { dialog, id ->
					dialog.dismiss()
				}
				.setPositiveButton(getString(R.string.sync_notes_dialog_pos_button_label)) { dialog, id ->
					dialog.dismiss()
					startRegisterActivity()
				}
				.show()
	}

	private fun startRegisterActivity() {
		val intent = Intent(context, RegisterActivity::class.java)
		startActivityForResult(intent, REGISTER_REQUEST_CODE)
	}

	override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_OK && isUserNotNull()) {
			val switchPreference =
				findPreference(getString(R.string.note_sync_key)) as SwitchPreferenceCompat
			switchPreference.isChecked = true
			startSyncService()
		}
	}

	private fun isUserNotNull() = mFirebaseAuth.currentUser != null

	companion object {
		private const val REGISTER_REQUEST_CODE = 7
	}
}