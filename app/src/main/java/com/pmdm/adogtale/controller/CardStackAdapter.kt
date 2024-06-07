package com.pmdm.adogtale.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pmdm.adogtale.R
import com.pmdm.adogtale.model.Itemx
import com.squareup.picasso.Picasso
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.Direction
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting

class CardStackAdapter(
    var items: List<Itemx>,
    private val manager: CardStackLayoutManager,
    private val onDislike: Runnable
) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.item_image)
        val name: TextView = itemView.findViewById(R.id.item_name)
        val age: TextView = itemView.findViewById(R.id.item_age)
        val town: TextView = itemView.findViewById(R.id.item_city)
        val button1: ImageView = itemView.findViewById(R.id.likeButton)
        val button2: ImageView = itemView.findViewById(R.id.dislikeButton)

        fun setData(data: Itemx) {
            if (!data.image.isNullOrEmpty()) {
                Picasso.get()
                    .load(data.image)
                    .fit()
                    .centerCrop()
                    .into(image)
            }
            name.text = data.name
            age.text = data.age
            town.text = data.town

            button1.setOnClickListener {
                Toast.makeText(itemView.context, "like", Toast.LENGTH_SHORT).show()
                manager.scrollToPosition(adapterPosition + 1)
            }

            button2.setOnClickListener {
                Toast.makeText(itemView.context, "dislike", Toast.LENGTH_SHORT).show()
                manager.setSwipeAnimationSetting(
                    SwipeAnimationSetting.Builder().setDirection(Direction.Left).build()
                )
                manager.scrollToPosition(adapterPosition + 1)
                onDislike.run()
            }
        }
    }
}
