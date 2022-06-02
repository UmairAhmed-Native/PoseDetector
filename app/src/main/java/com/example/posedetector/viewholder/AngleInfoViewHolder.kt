package com.example.posedetector.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.example.posedetector.databinding.ListItemAngleInfoBinding
import com.example.posedetector.model.AngleInfo

class AngleInfoViewHolder(
    private val context: Context,
    private val binding: ListItemAngleInfoBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(angleInfo: AngleInfo) {
        binding.txtAngleName.text = angleInfo.angleName
        binding.txtAngleValue.text = angleInfo.angleValue.toString()
    }
}