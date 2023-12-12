package com.app.sukasuka.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class GroupMessage(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val groupName: String = "",
    val message: String = "",
    val timestamp: Long = 0
) : Parcelable