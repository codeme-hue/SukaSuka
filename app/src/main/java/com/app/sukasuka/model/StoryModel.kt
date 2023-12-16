package com.app.sukasuka.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryModel
    (
    var imageurl: String? = null,
    var timestart: Long? = 0,
    var timeend: Long? = 0,
    var storyid: String? = null,
    var userid: String? = null): Parcelable