package de.nicidienase.chaosflix.common.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class LiveDataMerger<T, U, R> {
    fun merge(liveData1: LiveData<T>, liveData2: LiveData<U>, mergeFunction: (T?, U?) -> R): LiveData<R> {
        val result = MediatorLiveData<R>()

        result.addSource(liveData1) { t: T? ->
            result.value = mergeFunction(t, liveData2.value)
        }

        result.addSource(liveData2) { u: U? ->
            result.value = mergeFunction(liveData1.value, u)
        }
        return result
    }
}
