package de.datatrain.mietercockpitnative

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar
import com.sap.cloud.mobile.odata.DataQuery
import datatrain.mietercockpitnative.tp_srv.Tile


class TileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tile)
        setSupportActionBar(findViewById(R.id.toolbar))
        getTiles()
    }

    fun getTiles() {
        val tpSrv = (application as MyApplication).tp_srv
        val query = DataQuery().orderBy(Tile.sortID)
        tpSrv?.getTilesAsync(query, { tiles: List<Tile> ->
            var progressBar: FioriProgressBar = findViewById(de.datatrain.mietercockpitnative.R.id.progressBar)
            progressBar.visibility = View.GONE
            for (tile in tiles) {
                Log.d("myDebug", "${tile.task}")
            }
            showTiles(tiles);
        }, { re: RuntimeException -> Log.d("myDebug", "An error occurred when querying for tiles:  " + re.message) })
        Log.d(this.javaClass.name, "blubber")
    }

    fun showTiles(tiles: List<Tile>) {
        var recycler: RecyclerView = findViewById(de.datatrain.mietercockpitnative.R.id.tileView)
        recycler.setLayoutManager(GridLayoutManager(this, 1))
        recycler.adapter = TileAdapter(tiles)
    }

}
