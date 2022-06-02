package com.example.posedetector.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.posedetector.databinding.ListItemAngleInfoBinding
import com.example.posedetector.model.AngleInfo
import com.example.posedetector.viewholder.AngleInfoViewHolder

class AngleInfoAdapter(
    private val context: Context,
    private val angleList: MutableList<AngleInfo>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflatedView =
            ListItemAngleInfoBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        return AngleInfoViewHolder(
            context,
            inflatedView,
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemObj = angleList[position]
        when (holder) {
            is AngleInfoViewHolder ->
                holder.onBind(itemObj)
        }
    }

    override fun getItemCount(): Int {
        return angleList.size
    }
}