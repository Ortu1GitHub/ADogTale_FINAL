package com.pmdm.adogtale.model

import java.io.Serializable

data class LocalUser(
    var email: String = "",
    var username: String = "",
    var town: String = "",
    var phone: String = "",
    var name: String = "",
    var surname: String = "",
    var password: String = "",
    var token: String = ""
) : Serializable {

}