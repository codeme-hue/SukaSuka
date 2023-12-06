package com.app.sukasuka.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    var username: String? = null,
    var fullname: String? = null,
    var bio: String? = null,
    var image: String? = null,
    var uid: String? = null): Parcelable
