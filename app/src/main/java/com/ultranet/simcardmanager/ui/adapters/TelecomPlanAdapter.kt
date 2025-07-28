package com.ultranet.simcardmanager.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ultranet.simcardmanager.databinding.ItemTelecomPlanBinding
import com.ultranet.simcardmanager.domain.models.TelecomPlan

class TelecomPlanAdapter(
    private val onPlanClick: (TelecomPlan) -> Unit,
    private val onPlanSelect: (TelecomPlan) -> Unit
) : ListAdapter<TelecomPlan, TelecomPlanAdapter.TelecomPlanViewHolder>(TelecomPlanDiffCallback()) {
    
    private var selectedPlanId: String? = null
    
    fun setSelectedPlan(planId: String?) {
        val previousSelected = selectedPlanId
        selectedPlanId = planId
        notifyItemChanged(currentList.indexOfFirst { it.id == previousSelected })
        notifyItemChanged(currentList.indexOfFirst { it.id == selectedPlanId })
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TelecomPlanViewHolder {
        val binding = ItemTelecomPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TelecomPlanViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: TelecomPlanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class TelecomPlanViewHolder(
        private val binding: ItemTelecomPlanBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(plan: TelecomPlan) {
            binding.apply {
                // Set plan details
                tvPlanName.text = plan.name
                tvPlanPrice.text = "$${plan.price}"
                tvPlanData.text = plan.data
                tvCarrierName.text = plan.carrierName ?: "Unknown Carrier"
                tvPlanType.text = plan.planType ?: "POSTPAID"
                
                // Set contract length if available
                if (plan.contractLength != null) {
                    tvContractLength.text = "${plan.contractLength} months"
                    tvContractLength.visibility = android.view.View.VISIBLE
                } else {
                    tvContractLength.visibility = android.view.View.GONE
                }
                
                // Set features if available
                if (!plan.features.isNullOrEmpty()) {
                    tvFeatures.text = plan.features
                    tvFeatures.visibility = android.view.View.VISIBLE
                } else {
                    tvFeatures.visibility = android.view.View.GONE
                }
                
                // Set selection state
                val isSelected = plan.id == selectedPlanId
                btnSelect.text = if (isSelected) "Selected" else "Select"
                btnSelect.isEnabled = !isSelected
                
                // Set background color based on selection
                root.setCardBackgroundColor(
                    if (isSelected) {
                        android.graphics.Color.parseColor("#E3F2FD") // Light blue for selected
                    } else {
                        android.graphics.Color.WHITE
                    }
                )
                
                // Set price color based on plan type
                val priceColor = when (plan.planType) {
                    "PREPAID" -> android.graphics.Color.parseColor("#4CAF50") // Green
                    "POSTPAID" -> android.graphics.Color.parseColor("#2196F3") // Blue
                    else -> android.graphics.Color.parseColor("#FF9800") // Orange
                }
                tvPlanPrice.setTextColor(priceColor)
                
                // Click listeners
                root.setOnClickListener { onPlanClick(plan) }
                btnSelect.setOnClickListener { onPlanSelect(plan) }
            }
        }
    }
    
    private class TelecomPlanDiffCallback : DiffUtil.ItemCallback<TelecomPlan>() {
        override fun areItemsTheSame(oldItem: TelecomPlan, newItem: TelecomPlan): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: TelecomPlan, newItem: TelecomPlan): Boolean {
            return oldItem == newItem
        }
    }
} 