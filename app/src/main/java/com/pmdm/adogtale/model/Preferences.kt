package com.pmdm.adogtale.model

import java.io.Serializable

data class Preferences(
    var lookingFor: String = "",
    var prefBreed: String = "",
    var prefDistance: String = "",
) : Serializable