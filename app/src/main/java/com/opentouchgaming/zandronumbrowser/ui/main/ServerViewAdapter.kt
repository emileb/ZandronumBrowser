package com.opentouchgaming.zandronumbrowser.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.opentouchgaming.deltatouch.Browser.MasterServer.Server
import com.opentouchgaming.zandronumbrowser.R

class ServerViewAdapter(
    val clickListener: ServerClickListener
) :
    RecyclerView.Adapter<ServerViewAdapter.ViewHolder>() {

    var values: List<Server> = ArrayList()

    fun setNewData(items: List<Server>) {
        values = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values.get(position)
        holder.itemView.tag = item

        holder.serverName.text = item.serverInfo?.name
        holder.address.text = item.serverInfo?.url
        holder.iwad.text = item.serverInfo?.iwad

        holder.itemView.setOnClickListener {
            clickListener.onServerClickListener(item)
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val address: TextView = view.findViewById(R.id.address_text)
        val serverName: TextView = view.findViewById(R.id.server_text)
        val iwad: TextView = view.findViewById(R.id.iwad_text)

    }
}