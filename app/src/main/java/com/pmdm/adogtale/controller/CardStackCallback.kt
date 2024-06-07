package com.pmdm.adogtale.controller

import androidx.recyclerview.widget.DiffUtil
import com.pmdm.adogtale.model.Itemx

class CardStackCallback(private val old: List<Itemx>, private val new: List<Itemx>) :
    DiffUtil.Callback() {

    override fun getOldListSize() = old.size

    override fun getNewListSize() = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition].image == new[newItemPosition].image

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        old[oldItemPosition] == new[newItemPosition]
}