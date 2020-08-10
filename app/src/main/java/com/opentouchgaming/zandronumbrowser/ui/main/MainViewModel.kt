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
}