package com.opentouchgaming.deltatouch.Browser
import com.github.michaelbull.result.*
import com.opentouchgaming.zandronumbrowser.network.MasterServerAccess
import com.opentouchgaming.deltatouch.Browser.MasterServer.Server

object Repository {

    val masterServerAccess = MasterServerAccess()

    suspend fun getMasterServers(): Result<List<Server>, String>
    {
        return masterServerAccess.fetchServers()
    }

    suspend fun getServerInfo(server : Server):  Result<Boolean, String>
    {
        return masterServerAccess.fetchServerInfo(server)
    }

    operator fun invoke(): Repository {
        return this
    }

}