package com.ultranet.simcardmanager.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultranet.simcardmanager.databinding.ItemSimCardInfoBinding
import com.ultranet.simcardmanager.domain.models.SimCardInfo

class SimCardInfoAdapter(
    private val onSimCardClick: (SimCardInfo) -> Unit
) : ListAdapter<SimCardInfo, SimCardInfoAdapter.SimCardInfoViewHolder>(SimCardInfoDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimCardInfoViewHolder {
        val binding = ItemSimCardInfoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SimCardInfoViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SimCardInfoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SimCardInfoViewHolder(
        private val binding: ItemSimCardInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(simCardInfo: SimCardInfo) {
            binding.apply {
                tvSlotNumber.text = "Slot ${simCardInfo.slotNumber}"
                tvCarrierName.text = simCardInfo.carrierName ?: "Unknown Carrier"
                tvSimState.text = "State: ${simCardInfo.simState}"
                tvNetworkType.text = "Network: ${simCardInfo.networkType ?: "Unknown"}"
                
                // Set color based on SIM state
                val stateColor = when (simCardInfo.simState) {
                    "READY" -> android.graphics.Color.GREEN
                    "ABSENT" -> android.graphics.Color.RED
                    "PIN_REQUIRED", "PUK_REQUIRED" -> android.graphics.Color.YELLOW
                    else -> android.graphics.Color.GRAY
                }
                tvSimState.setTextColor(stateColor)
                
                // Click listener
                root.setOnClickListener { onSimCardClick(simCardInfo) }
            }
        }
    }
    
    private class SimCardInfoDiffCallback : DiffUtil.ItemCallback<SimCardInfo>() {
        override fun areItemsTheSame(oldItem: SimCardInfo, newItem: SimCardInfo): Boolean {
            return oldItem.slotNumber == newItem.slotNumber
        }
        
        override fun areContentsTheSame(oldItem: SimCardInfo, newItem: SimCardInfo): Boolean {
            return oldItem == newItem
        }
    }
} 