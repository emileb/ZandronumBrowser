package com.opentouchgaming.zandronumbrowser.network

import com.github.michaelbull.result.*
import com.opentouchgaming.deltatouch.Browser.MasterServer.Network
import com.opentouchgaming.deltatouch.Browser.MasterServer.Server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    suspend fun fetchServers(): Result<List<Server>, String> {

        val servers = ArrayList<Server>()

        var errorString = ""

        withContext(Dispatchers.IO) {

            val network = Network(SERVER_ADDRESS, SERVER_PORT)

            // Try to send command
            network.send(getServersCmd())
                .onFailure { error: String ->
                    errorString = error
                    return@withContext
                }
                .onSuccess { println("Sent") }

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
                    decodeServers(data, servers)
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

    private fun encode(data: ByteArray): ByteArray {
        return Huffman.encode(data)
    }

    private fun decode(data: ByteArray): ByteArray {
        return Huffman.decode(data)
    }

    fun b(bytes: ByteArray): String? {
        var ret = ""
        for (b in bytes) {
            ret += " " + java.lang.Byte.toString(b)
        }
        return ret
    }

    private fun getServersCmd(): ByteArray {
        val bytes = ByteBuffer.allocate(6)
        bytes.order(ByteOrder.LITTLE_ENDIAN)
        bytes.putInt(5660028)
        bytes.putShort(2.toShort())
        return encode(bytes.array())
    }

    private fun decodeServers(
        data: ByteArray,
        servers: MutableList<Server>
    ): Result<Boolean, String> {
        val decoded = decode(data)
        println("data = ${b(decoded)}")
        val buffer = ByteBuffer.wrap(decoded)
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Get 4 byte header
        val header = buffer.getInt()
        println("header = $header")

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
}