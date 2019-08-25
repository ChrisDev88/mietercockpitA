package de.datatrain.mietercockpitnative

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import com.sap.cloud.mobile.odata.DataQuery
import datatrain.mietercockpitnative.tp_srv.Tile

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuEntries()
        menuInflater.inflate(de.datatrain.mietercockpitnative.R.menu.header, menu)
        return true
    }

    fun getMenuEntries() {
        var tiles2: List<Tile> = listOf()
        Log.d(this.javaClass.name, "blubber")
        val tp_srv = (application as MyApplication).tp_srv

        val query = DataQuery().orderBy(Tile.sortID)
        tp_srv?.getTilesAsync(query, { tiles: List<Tile> ->
            setMenu(tiles)
        }, { re: RuntimeException -> Log.d("myDebug", "An error occurred when querying for tiles:  " + re.message) })
    }

    fun setMenu(tiles: List<Tile>) {
        val toolbar = findViewById(de.datatrain.mietercockpitnative.R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            Toast.makeText(applicationContext, "Toolbar", Toast.LENGTH_SHORT).show()
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->

                Log.d("myDebug", item.title.toString())
                when (item.title) {
                    "Startseite" -> startActivityHome()
                    "Aktuelles" -> Toast.makeText(applicationContext, "Toolbar", Toast.LENGTH_SHORT).show()
                    "Meine Anliegen" -> startActivityMeineAnliegen()
                    "Mein Mietvertrag" -> startActivityMeinMietvertrag()
                    "Meine Termine" -> startActivityTermine()
                    "Meine Verbräuche" -> startActivityVerbrauch()
                }
                false
            }


            val inflater = popupMenu.menuInflater
            val menu: Menu
            inflater.inflate(de.datatrain.mietercockpitnative.R.menu.menu, popupMenu.menu)

            popupMenu.menu.add("Startseite")
            for (tile in tiles) {
                popupMenu.menu.add(tile.title)
            }
            popupMenu.show()
        }
    }

    private fun startActivityMeineAnliegen() {
        //val intent = Intent(this, MaDashboard::class.java)
        //startActivityForResult(intent,1)
    }

    private fun startActivityHome() {
        val intent = Intent(this, TileActivity::class.java)
        startActivityForResult(intent,1)
    }

    private fun startActivityMeinMietvertrag() {
        val intent = Intent(this, ContractActivity::class.java)
        startActivityForResult(intent,1)
    }


    private fun startActivityTermine() {
        //val intent = Intent(this, HomeActivity::class.java)
        //startActivityForResult(intent,1)
    }

    private fun startActivityVerbrauch() {
        //val intent = Intent(this, HomeActivity::class.java)
        //startActivityForResult(intent,1)
    }

}
