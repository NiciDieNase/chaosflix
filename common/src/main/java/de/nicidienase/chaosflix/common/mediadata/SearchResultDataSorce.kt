package de.nicidienase.chaosflix.common.mediadata

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import de.nicidienase.chaosflix.common.mediadata.entities.recording.persistence.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SearchResultDataSource(
    private val query: String,
    private val repository: MediaRepository,
    private val coroutineScope: CoroutineScope
) : PageKeyedDataSource<Int, Event>() {
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Event>
    ) {
        coroutineScope.launch {
            val response = repository.findEvents(query, 1)
            if (response != null) {
                response.apply {
                    Log.i(TAG, "Initial Load, query: $query, total Items: $total")
                    callback.onResult(events, 0, total, null, if (hasNext) 2 else null)
                }
            } else {
                callback.onResult(emptyList<Event>(), 1, 0, null, null)
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Event>) {
        coroutineScope.launch {
            Log.i(TAG, "query: $query, Update page ${params.key}")
            val response = repository.findEvents(query, params.key)
            if (response != null) {
                callback.onResult(response.events, if (response.hasNext) params.key + 1 else null)
            }
            // TODO retry
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Event>) {
    }

    companion object {
        private val TAG = SearchResultDataSource::class.java.simpleName
    }
}

class SearchResultDataSourceFactory(private val query: String, private val repository: MediaRepository, private val scope: CoroutineScope) : DataSource.Factory<Int, Event>() {
    override fun create(): DataSource<Int, Event> {
        return SearchResultDataSource(query, repository, scope)
    }
}
