package playground.develop.socialnote.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import org.koin.core.KoinComponent
import playground.develop.socialnote.utils.PreferenceUtils
import playground.develop.socialnote.utils.SyncUtils

/**
 * Created by AbdullahAtta on 30-Sep-19.
 */
class SyncWorker(appContext: Context, workerParameters: WorkerParameters) : Worker(appContext, workerParameters), KoinComponent {

    override fun doWork(): Result {
        if (PreferenceUtils.getPreferenceUtils().isUserEnableSync(applicationContext) && FirebaseAuth.getInstance().currentUser != null) {
            SyncUtils.getSyncUtils().loadNotesFromFirestore()
            SyncUtils.getSyncUtils().addNotesToFirestore()
            SyncUtils.getSyncUtils().syncNeedNoteUpdatesToFirestore()
        } else {
            return Result.failure()
        }
        return Result.success()
    }

}