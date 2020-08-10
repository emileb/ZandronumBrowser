package com.opentouchgaming.zandronumbrowser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.opentouchgaming.zandronumbrowser.ui.main.MasterServerFragment


class MasterServerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.master_server_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MasterServerFragment.newInstance())
                    .commitNow()
        }
    }
}