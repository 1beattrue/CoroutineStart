package edu.mirea.onebeattrue.coroutinestart

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val parentJob = SupervisorJob() // отличие в том,
    // что после поимки исключения в одном из наследников, остальные продолжат работу
    // кстати viewModelScope имеет в качестве контекста как раз таки SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(LOG_TAG, "Exception caught: $throwable")
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob + exceptionHandler)

    fun method() {
        val childJob1 = coroutineScope.launch {
            delay(3000)
            Log.d(LOG_TAG, "first coroutine finished")
        }
        val childJob2 = coroutineScope.launch {
            delay(2000)
            Log.d(LOG_TAG, "second coroutine finished")
        }
        val childJob3 = coroutineScope.async {
            // теперь метод возвращает тип Deferred, а поэтому CoroutineExceptionHandler не поймает исключение,
            // при этом приложение не упадет, потому что исключение будет обернуто в объект Deferred
            delay(1000)
            error()
            Log.d(LOG_TAG, "third coroutine finished")
        }
        coroutineScope.launch {
            childJob3.await() // в данном случае CoroutineExceptionHandler поймает исключение у объекта Deferred
        }
    }

    private fun error() {
        throw RuntimeException()
    }

    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
    }

    companion object {
        private const val LOG_TAG = "MainViewModel"
    }
}