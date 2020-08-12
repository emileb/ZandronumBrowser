package com.opentouchgaming.deltatouch.Browser.MasterServer


data class Server(
    val numServers: UByte,
    val ip: UByteArray,
    val port: UShort,
    var serverInfo: ServerInfo? = null
) {
    fun getIpStr(): String {
        return "${ip[0]}.${ip[1]}.${ip[2]}.${ip[3]}"
    }
}

data class ServerInfo(
    var version: String = "",
    var ping: Int = 0,
    var flags: Int = 0,
    var name: String = "",
    var url: String = "",
    var email: String = "",
    var mapName: String = "",
    var maxClients: Int = 0,
    var pwadInfo: List<String> = mutableListOf(),
    var gameType: Int = 0,
    var gameName: String = "",
    var iwad: String = ""
// TODO finish
)
