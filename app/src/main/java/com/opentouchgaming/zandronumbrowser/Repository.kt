package com.opentouchgaming.deltatouch.Browser
import com.github.michaelbull.result.*
import com.opentouchgaming.zandronumbrowser.network.MasterServerAccess
import com.opentouchgaming.deltatouch.Browser.MasterServer.Server

class Repository {

    val masterServerAccess = MasterServerAccess()

    suspend fun getMasterServers(): Result<ArrayList<Server>, String>
    {
        return masterServerAccess.fetchServers()
    }

}