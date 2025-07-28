package com.ultranet.simcardmanager.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.ultranet.simcardmanager.databinding.FragmentSimSwitchDialogBinding

class SimSwitchDialogFragment : DialogFragment() {
    
    private var _binding: FragmentSimSwitchDialogBinding? = null
    private val binding get() = _binding!!
    
    private var onConfirmListener: (() -> Unit)? = null
    private var onCancelListener: (() -> Unit)? = null
    
    companion object {
        const val TAG = "SimSwitchDialogFragment"
        
        fun newInstance(
            onConfirm: () -> Unit,
            onCancel: () -> Unit = {}
        ): SimSwitchDialogFragment {
            return SimSwitchDialogFragment().apply {
                onConfirmListener = onConfirm
                onCancelListener = onCancel
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimSwitchDialogBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnConfirm.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss()
        }
        
        binding.btnCancel.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 