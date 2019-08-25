package de.datatrain.mietercockpitnative

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import datatrain.mietercockpitnative.tp_srv.Tile
import kotlinx.android.synthetic.main.template_tile.view.*




class TileAdapter(private val tiles: List<Tile>) : RecyclerView.Adapter<TileAdapter.TileHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileHolder {
        val inflatedView = parent.inflate(de.datatrain.mietercockpitnative.R.layout.template_tile, false)
        return TileHolder(inflatedView)
    }

    override fun getItemCount() = tiles.size


    override fun onBindViewHolder(holder: TileHolder, position: Int) {
        val tile = tiles[position]
        holder.bindTile(tile)
    }

    class TileHolder(val v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        private var view: View = v
        private var tile: Tile? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            Log.d("myDebug", adapterPosition.toString())
            when(adapterPosition){
                0 -> {
                    var context: Context = v.context
                    val intent = Intent(context.applicationContext, ContractActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.applicationContext.startActivity(intent)
                }
                1 -> {
                    var context: Context = v.context
                    //val intent = Intent(context.applicationContext, MaDashboard::class.java)
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    //context.applicationContext.startActivity(intent)
                }
            }
        }

        private fun startActivityMeineAufgaben() {

        }

        companion object {
            private val TILE_KEY = "TILE"
        }

        fun bindTile(tile: Tile) {
            this.tile = tile
            view.tileName.text = tile.title
            view.tileDescription.text = tile.description

            when(tile.iconID){
                "app-icon-calendar" -> view.tileIcon.setImageResource(de.datatrain.mietercockpitnative.R.drawable.app_icon_calendar);

                else -> view.tileIcon.setImageResource(de.datatrain.mietercockpitnative.R.drawable.app_icon_calendar)
            }
        }
    }
}