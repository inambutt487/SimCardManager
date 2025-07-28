package com.ultranet.simcardmanager.domain.models

data class SimCardInfo(
    val slotNumber: Int,
    val carrierName: String?,
    val simState: String,
    val networkType: String?
) 