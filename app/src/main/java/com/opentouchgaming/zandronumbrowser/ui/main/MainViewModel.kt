package com.opentouchgaming.zandronumbrowser.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.opentouchgaming.deltatouch.Browser.MasterServer.Server
import com.opentouchgaming.deltatouch.Browser.Repository

class MainViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val repositor: Repository = Repository()

    // Create a LiveData with a String
    val serverListMutableData: MutableLiveData<List<Server>> by lazy {
        MutableLiveData<List<Server>>()
    }



    suspend fun refreshButtonPressed()
    {
        serverListMutableData.value = (repositor.getMasterServers().component1())
    }
}