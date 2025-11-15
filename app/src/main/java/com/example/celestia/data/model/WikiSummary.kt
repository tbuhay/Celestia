package com.example.celestia.data.model

data class WikiSummary(
    val title: String?,
    val description: String?,
    val extract: String?,
    val thumbnail: WikiImage?,
    val originalimage: WikiImage?
)

data class WikiImage(
    val source: String?,
    val width: Int?,
    val height: Int?
)