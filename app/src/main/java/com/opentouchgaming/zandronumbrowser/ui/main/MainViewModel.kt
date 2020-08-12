package com.opentouchgaming.zandronumbrowser.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.opentouchgaming.deltatouch.Browser.MasterServer.Server
import com.opentouchgaming.deltatouch.Browser.Repository
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors

class MainViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val repository: Repository = Repository()

    // Create a LiveData with a String
    val serverListMutableData: MutableLiveData<List<Server>> by lazy {
        MutableLiveData<List<Server>>()
    }


    fun refreshButtonPressed() {
        viewModelScope.launch {
            // Coroutine that will be canceled when the ViewModel is cleared.
            repository.getMasterServers()
                .onFailure { s: String -> println("Error in refresh: $s") }
                .onSuccess { servers -> serverListMutableData.value = servers }
        }
    }

    fun updateServerInfo(servers: List<Server>, callback: (n: Int) -> Unit) {

        val queue: ConcurrentLinkedQueue<Server> = ConcurrentLinkedQueue()

        // Add all to the queue
        queue.addAll(servers)

        repeat (5) {

            viewModelScope.launch {
                while(true) {
                    val server = queue.poll()
                    if(server!==null) {
                        repository.getServerInfo(server)
                    }
                    else
                    {
                        break;
                    }
                    callback(0)
                    //println("${Thread.currentThread()}, millis: ${System.currentTimeMillis()}")
                }
                println("FINISHED")
            }
        }

        println("updateServerInfo finished")
    }

    fun serverItemPressed(server: Server) {
        viewModelScope.launch {
            repository.getServerInfo(server)
        }
    }
}