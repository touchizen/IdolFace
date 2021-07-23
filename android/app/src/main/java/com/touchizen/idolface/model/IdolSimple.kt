package com.touchizen.idolface.model

data class IdolSimple(
    var id: String,
    var imageUrl: String,
    var name: String,
    var birth: String,
    var bodyProfile: String,
    var company: String,
    var avgRate: Float,
    var pictureCount: Int,
    var awards: String,
    var groupName: String,
    var jobClass: String
    )