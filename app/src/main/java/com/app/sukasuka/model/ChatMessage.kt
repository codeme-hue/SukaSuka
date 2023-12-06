package com.app.sukasuka.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ChatMessage(
    val text: String = "",
    val fromId: String = "",
    val toId: String = "",
    val senderData: String = "",
    val receiverData: String = "",
    val timestamp: Long = 0
): Parcelable