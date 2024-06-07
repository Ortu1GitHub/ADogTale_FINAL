package com.pmdm.adogtale.model

import com.google.firebase.Timestamp

class ChatMessageModel {
    var message: String? = null
    var senderId: String? = null
    var timestamp: Timestamp? = null
    var alreadyRead: Boolean? = false

    constructor() {}
    constructor(message: String?, senderId: String?, timestamp: Timestamp?) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
    }

    constructor(message: String?, senderId: String?, timestamp: Timestamp?, alreadyRead: Boolean) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
        this.alreadyRead = alreadyRead
    }
}