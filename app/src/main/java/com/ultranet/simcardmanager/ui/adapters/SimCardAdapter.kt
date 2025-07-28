package com.ultranet.simcardmanager.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultranet.simcardmanager.databinding.ItemSimCardBinding
import com.ultranet.simcardmanager.domain.models.SimCard

class SimCardAdapter(
    private val onSimCardClick: (SimCard) -> Unit,
    private val onActivateClick: (SimCard) -> Unit
) : ListAdapter<SimCard, SimCardAdapter.SimCardViewHolder>(SimCardDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimCardViewHolder {
        val binding = ItemSimCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SimCardViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SimCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class SimCardViewHolder(
        private val binding: ItemSimCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(simCard: SimCard) {
            binding.apply {
                tvSlotIndex.text = "Slot ${simCard.slotNumber}"
                tvIccid.text = "ICCID: ${simCard.iccid ?: "N/A"}"
                tvImsi.text = "IMSI: ${simCard.imsi ?: "N/A"}"
                tvPhoneNumber.text = simCard.phoneNumber ?: "No phone number"
                tvCarrierName.text = simCard.carrierName ?: "Unknown carrier"
                tvCountryCode.text = simCard.countryCode ?: "Unknown country"
                
                // Set active state
                chipActive.isChecked = simCard.isActive
                
                // Click listeners
                root.setOnClickListener { onSimCardClick(simCard) }
                btnActivate.setOnClickListener { onActivateClick(simCard) }
            }
        }
    }
    
    private class SimCardDiffCallback : DiffUtil.ItemCallback<SimCard>() {
        override fun areItemsTheSame(oldItem: SimCard, newItem: SimCard): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: SimCard, newItem: SimCard): Boolean {
            return oldItem == newItem
        }
    }
} 