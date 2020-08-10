package com.opentouchgaming.deltatouch.Browser.MasterServer

data class Server(val numServers: UByte, val ip: UByteArray, val port: UShort)
{
    fun getIpStr(): String
    {
        return "${ip[3]}:${ip[2]}:${ip[1]}:${ip[0]}"
    }
}

