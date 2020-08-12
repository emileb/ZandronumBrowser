package com.opentouchgaming.zandronumbrowser.network

import com.github.michaelbull.result.*
import com.opentouchgaming.deltatouch.Browser.MasterServer.Network
import com.opentouchgaming.deltatouch.Browser.MasterServer.Server
import com.opentouchgaming.deltatouch.Browser.MasterServer.ServerInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MasterServerAccess {

    val SERVER_ADDRESS = "master.zandronum.com"
    val SERVER_PORT = 15300

    val MSC_SERVER = 1
    val MSC_ENDSERVERLIST = 2
    val MSC_IPISBANNED = 3
    val MSC_REQUESTIGNORED = 4
    val MSC_WRONGVERSION = 5
    val MSC_BEGINSERVERLISTPART = 6
    val MSC_ENDSERVERLISTPART = 7
    val MSC_SERVERBLOCK = 8

    val SQF_NAME = 0x00000001
    val SQF_URL = 0x00000002
    val SQF_EMAIL = 0x00000004
    val SQF_MAPNAME = 0x00000008
    val SQF_MAXCLIENTS = 0x00000010
    val SQF_MAXPLAYERS = 0x00000020
    val SQF_PWADS = 0x00000040
    val SQF_GAMETYPE = 0x00000080
    val SQF_GAMENAME = 0x00000100
    val SQF_IWAD = 0x00000200
    val SQF_FORCEPASSWORD = 0x00000400
    val SQF_FORCEJOINPASSWORD = 0x00000800
    val SQF_GAMESKILL = 0x00001000
    val SQF_BOTSKILL = 0x00002000
    val SQF_DMFLAGS = 0x00004000 // Deprecated
    val SQF_LIMITS = 0x00010000
    val SQF_TEAMDAMAGE = 0x00020000
    val SQF_TEAMSCORES = 0x00040000 // Deprecated
    val SQF_NUMPLAYERS = 0x00080000
    val SQF_PLAYERDATA = 0x00100000
    val SQF_TEAMINFO_NUMBER = 0x00200000
    val SQF_TEAMINFO_NAME = 0x00400000
    val SQF_TEAMINFO_COLOR = 0x00800000
    val SQF_TEAMINFO_SCORE = 0x01000000
    val SQF_TESTING_SERVER = 0x02000000
    val SQF_DATA_MD5SUM = 0x04000000
    val SQF_ALL_DMFLAGS = 0x08000000
    val SQF_SECURITY_SETTINGS = 0x10000000

    val SERVER_MASTER_CHALLENGE = 5660020
    val SERVER_MASTER_STATISTICS = 5660022
    val SERVER_LAUNCHER_CHALLENGE = 5660023
    val SERVER_LAUNCHER_IGNORING = 5660024
    val SERVER_LAUNCHER_BANNED = 5660025
    val CLIENT_MASTER_NEWACCOUNT = 5660026
    val CLIENT_MASTER_LOGIN = 5660027
    val LAUNCHER_MASTER_CHALLENGE = 5660028
    val SERVER_MASTER_VERIFICATION = 5660029
    val SERVER_MASTER_BANLIST_RECEIPT = 5660030
    val LAUNCHER_SERVER_CHALLENGE = 199

    val MASTER_SERVER_VERSION = 2

    suspend fun fetchServers(): Result<List<Server>, String> {

        val servers = ArrayList<Server>()

        var errorString = ""

        withContext(Dispatchers.IO) {

            val network = Network(SERVER_ADDRESS, SERVER_PORT)

            // Try to send command
            network.send(getMasterChallengeCmd())
                .onFailure { error: String ->
                    errorString = error
                    return@withContext
                }
                .onSuccess { println("Sent master challenge") }

            // Receive data
            while (true) {
                val ret = network.receive()
                    .onFailure { error: String ->
                        errorString = error
                        return@withContext
                    }
                    .onSuccess { array: ByteArray -> println("Received ${array.size} bytes") }

                val data = ret.component1()

                // Try to decode the data
                if (data != null) {
                    decodeMasterInfo(data, servers)
                        .onFailure { error: String ->
                            errorString = error
                            return@withContext
                        }
                        .onSuccess { endOfList: Boolean ->
                            if (endOfList)
                                return@withContext
                        }
                }
            }

            println("fetchServers FINISHED")
        }

        return if (errorString.isEmpty()) {
            Ok(servers)
        } else {
            Err(errorString)
        }
    }

    suspend fun fetchServerInfo(server: Server): Result<Boolean, String> {
        var errorString = ""

        withContext(Dispatchers.IO) {

            val network = Network(server.getIpStr(), server.port.toInt())

            val flags =
                SQF_NAME or SQF_IWAD or SQF_MAPNAME or SQF_PWADS or SQF_GAMENAME or SQF_URL or SQF_MAXCLIENTS or SQF_MAXPLAYERS

            // Try to send command
            network.send(getServerChallengeCmd(flags))
                .onFailure { error: String ->
                    errorString = error
                    return@withContext
                }
                .onSuccess {  }


            val ret = network.receive()
                .onFailure { error: String ->
                    println("Error receiving: $error")
                    errorString = error
                    return@withContext
                }
                .onSuccess { array: ByteArray -> println("Received ${array.size} bytes") }

            val data = ret.component1()

            // Try to decode the data
            if (data != null) {
                decodeServerInfo(data, server)
                    .onFailure { error: String ->
                        errorString = error
                        return@withContext
                    }
                    .onSuccess { endOfList: Boolean ->
                        if (endOfList)
                            return@withContext
                    }
            }


            println("fetchServers FINISHED")
        }

        return if (errorString.isEmpty()) {
            Ok(true)
        } else {
            Err(errorString)
        }
    }

    private fun getMasterChallengeCmd(): ByteArray {
        val bytes = ByteBuffer.allocate(6)
        bytes.order(ByteOrder.LITTLE_ENDIAN)
        bytes.putInt(LAUNCHER_MASTER_CHALLENGE)
        bytes.putShort(MASTER_SERVER_VERSION.toShort())
        return encode(bytes.array())
    }

    private fun getServerChallengeCmd(flags: Int): ByteArray {
        val bytes = ByteBuffer.allocate(4 * 3)
        bytes.order(ByteOrder.LITTLE_ENDIAN)
        bytes.putInt(LAUNCHER_SERVER_CHALLENGE)
        bytes.putInt(flags)
        bytes.putInt(System.currentTimeMillis().toInt())
        return encode(bytes.array())
    }

    private fun getString(buffer: ByteBuffer): String {
        val string = StringBuilder()

        while (true) {
            val byte = buffer.get()

            // Null terminated
            if (byte.toInt() == 0)
                break;

            string.append(byte.toChar())
        }
        return string.toString()
    }

    private fun decodeServerInfo(
        data: ByteArray,
        server: Server
    ): Result<Boolean, String> {
        val decoded = decode(data)
        //println("data = ${printBytes(decoded)}")

        val buffer = ByteBuffer.wrap(decoded)
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Get 4 byte header
        val header = buffer.getInt()

        when (header) {
            SERVER_LAUNCHER_IGNORING -> return Err("IP Address is banned")
            SERVER_LAUNCHER_BANNED -> return Err("Servers request ignored, wait 10 seconds")
            SERVER_LAUNCHER_CHALLENGE -> null
            else -> return Err("Unknown header $header")
        }

        val serverInfo = ServerInfo()

        val time = buffer.getInt()

        serverInfo.ping = System.currentTimeMillis().toInt() - time

        serverInfo.version = getString(buffer)

        val flags = buffer.getInt()

        if (flags and SQF_NAME != 0) {
            serverInfo.name = getString(buffer)
        }


        // Read the website URL.
        if (flags and SQF_URL != 0)
            serverInfo.url = getString(buffer)

        // Read the host's e-mail address.
        if (flags and SQF_EMAIL != 0)
            serverInfo.email = getString(buffer)

        if (flags and SQF_MAPNAME != 0)
            serverInfo.mapName = getString(buffer)

        if (flags and SQF_MAXCLIENTS != 0)
            serverInfo.maxClients = buffer.get().toInt()

        // Maximum slots.
        if (flags and SQF_MAXPLAYERS != 0)
            buffer.get()

        // Read in the PWAD information.

        // Read in the PWAD information.
        if (flags and SQF_PWADS != 0) {

            val numberPwads = buffer.get().toInt()

            val pwads = ArrayList<String>()

            if (numberPwads > 0) {
                var n = 0
                while (n < numberPwads) {
                    pwads.add(getString(buffer))
                    n = n + 1
                }
            }

            serverInfo.pwadInfo = pwads
        }

        println("serverInfo = $serverInfo")

        server.serverInfo = serverInfo

        return Ok(true)
    }


    private fun decodeMasterInfo(
        data: ByteArray,
        servers: MutableList<Server>
    ): Result<Boolean, String> {
        val decoded = decode(data)

        val buffer = ByteBuffer.wrap(decoded)
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Get 4 byte header
        val header = buffer.getInt()

        when (header) {
            MSC_IPISBANNED -> return Err("IP Address is banned")
            MSC_REQUESTIGNORED -> return Err("Servers request ignored, wait 10 seconds")
            MSC_WRONGVERSION -> return Err("Servers version wrong")
            MSC_BEGINSERVERLISTPART -> println("Header OK")
            else -> return Err("Unknown header $header")
        }

        val packetNum = buffer.get().toUByte()
        println("packetNum = $packetNum")

        var endOfList = false
        loop@ while (true) {

            val command = buffer.get().toInt()

            when (command) {
                MSC_SERVERBLOCK -> {

                    while (true) {
                        var numPorts = buffer.get().toUByte().toInt()

                        println("numPorts = $numPorts")

                        // If number of ports is 0 its the end of the list
                        if (numPorts == 0)
                            break;

                        val ip0 = buffer.get().toUByte()
                        val ip1 = buffer.get().toUByte()
                        val ip2 = buffer.get().toUByte()
                        val ip3 = buffer.get().toUByte()

                        while (numPorts > 0) {
                            val port = buffer.getShort().toUShort()
                            val server = Server(packetNum, ubyteArrayOf(ip0, ip1, ip2, ip3), port)
                            //println("server = ${server.toString()}")
                            servers.add(server)
                            numPorts--
                        }
                    }
                }
                MSC_ENDSERVERLIST -> {
                    endOfList = true;
                    break@loop;
                }
                MSC_ENDSERVERLISTPART -> break@loop;
            }
        }

        return Ok(endOfList)
    }


    private fun encode(data: ByteArray): ByteArray {
        return Huffman.encode(data)
    }

    private fun decode(data: ByteArray): ByteArray {
        return Huffman.decode(data)
    }

    private fun printBytes(bytes: ByteArray): String? {
        var ret = ""
        for (b in bytes) {
            ret += " " + java.lang.Byte.toString(b)
        }
        return ret
    }

}