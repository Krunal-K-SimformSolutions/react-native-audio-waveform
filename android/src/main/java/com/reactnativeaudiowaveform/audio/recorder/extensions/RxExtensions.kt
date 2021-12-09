package com.reactnativeaudiowaveform.audio.recorder.extensions

import com.reactnativeaudiowaveform.audio.recorder.model.DebugState
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.CheckReturnValue
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * internal extensions to handle onNext, onError, onComplete event
 */
@CheckReturnValue
fun <T: Any> Observable<T>.subscribeIO(callback: (T?, Throwable?, Boolean) -> Unit): Disposable {
    return this.subscribeOn(Schedulers.io()).subscribe({
        callback.invoke(it, null, false)
    }, {
        DebugState.error("error", it)
        callback.invoke(null, it, false)
    }, {
        callback.invoke(null, null, true)
    })
}

@CheckReturnValue
fun <T: Any> Observable<T>.subscribeMain(callback: (T?, Throwable?, Boolean) -> Unit): Disposable {
    return this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
        callback.invoke(it, null, false)
    }, {
        DebugState.error("error", it)
        callback.invoke(null, it, false)
    }, {
        callback.invoke(null, null, true)
    })
}

/**
 * internal extensions to handle dispose Disposable
 */
fun Disposable?.safeDispose() {
    if (this != null && !this.isDisposed) this.dispose()
}
