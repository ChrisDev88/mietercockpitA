package de.datatrain.mietercockpitnative

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class ContractActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract)
        setSupportActionBar(findViewById(R.id.toolbar))
    }
}
