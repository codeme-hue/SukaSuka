package com.app.sukasuka.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PostModel(
    var postid: String? = null,
    var postimage: String? = null,
    var publisher: String? = null,
    var description: String? = null,
    var dateTime: String? = null
) : Parcelable
