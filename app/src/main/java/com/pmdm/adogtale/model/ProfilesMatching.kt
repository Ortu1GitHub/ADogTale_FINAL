package com.pmdm.adogtale.model

import java.io.Serializable

class ProfilesMatching(

    var user_original: String? = null,
    var profile_original: String? = null,
    var user_target: String? = null,
    var profile_target: String? = null,
    val likeAlreadyChecked: Boolean? = false,
) : Serializable {
}
