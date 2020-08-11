package com.opentouchgaming.zandronumbrowser.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.opentouchgaming.deltatouch.Browser.MasterServer.Server

import com.opentouchgaming.zandronumbrowser.R
import kotlinx.android.synthetic.main.main_fragment.*


class MasterServerFragment : Fragment() {

    companion object {
        fun newInstance() = MasterServerFragment()
    }

    private lateinit var viewModel: MainViewModel

    private val adapter: ServerViewAdapter = ServerViewAdapter(ArrayList<Server>(0))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        toolbar_home.setNavigationIcon(R.drawable.ic_launcher_background) // need to set the icon here to have a navigation icon. You can simple create an vector image by "Vector Asset" and using here
        toolbar_home.setNavigationOnClickListener {
            // do something when click navigation
        }
*/
        recyclerView.adapter = adapter

        button.setOnClickListener {
            viewModel.refreshButtonPressed()
        }

        toolbar_home.inflateMenu(R.menu.menu_home)
        toolbar_home.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_add -> {
                    // do something
                    true
                }
                R.id.action_update_room -> {
                    // do something
                    true
                }
                else -> {
                    super.onOptionsItemSelected(it)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.serverListMutableData.observe(
            viewLifecycleOwner,
            Observer { t: List<Server>? ->
                if (t != null) {
                    println("OBSERVED, len = ${t.size}")
                    adapter.setNewData(t)
                    adapter.notifyDataSetChanged()
                }
            })

    }

}