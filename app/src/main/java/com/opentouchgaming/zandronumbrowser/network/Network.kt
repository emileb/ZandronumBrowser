package com.opentouchgaming.deltatouch.Browser.MasterServer

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

import com.github.michaelbull.result.Result

class Network(val address: String, val port: Int) {

    val TIMEOUT = 10000

    var socket: DatagramSocket? = null
    var sendPacket: DatagramPacket? = null

    fun send(data: ByteArray): Result<Boolean, String> {
        println("send, len = ${data.size}")
        try {
            socket = DatagramSocket()
            socket?.soTimeout = TIMEOUT

            sendPacket = DatagramPacket(data, data.size, InetAddress.getByName(address), port)
            socket?.send(sendPacket)

            return Ok(true)
        } catch (e: IOException) {
            socket = null
            return Err(e.toString())
        }
    }

    fun receive(): Result<ByteArray, String> {
        try {
            if (socket != null) {
                val buffer = ByteArray(2048)
                val packet = DatagramPacket(buffer, buffer.size)
                socket?.receive(packet)
                return Ok(packet.data.sliceArray(0..packet.length - 1))
            } else {
                return Err("Must send before you can receive")
            }
        } catch (e: IOException) {
            return Err(e.toString())
        }
    }
}