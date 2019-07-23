package elamien.abdullah.socialnote.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import elamien.abdullah.socialnote.database.Note
import elamien.abdullah.socialnote.database.NoteDao
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by AbdullahAtta on 7/23/2019.
 */
class NoteRepository : INoteRepository, KoinComponent {
    private val mNotesDao: NoteDao by inject()
    private val mDisposables = CompositeDisposable()

    override fun insertNote(note: Note): LiveData<Long> {
        val id = MutableLiveData<Long>()
        mDisposables.add(
            mNotesDao.insertNote(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<Long>() {
                    override fun onSuccess(t: Long) {
                        id.value = t
                    }

                    override fun onError(e: Throwable) {
                    }

                })
        )
        return id
    }

    override fun dispose() {
        mDisposables.clear()
    }
}