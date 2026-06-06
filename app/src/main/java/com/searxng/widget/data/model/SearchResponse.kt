package com.searxng.widget.data.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    val query: String,
    val results: List<SearchResult>,
    val answers: List<String>?,
    val infoboxes: List<Infobox>?,
    val suggestions: List<String>?,
    @SerializedName("unresponsive_engines")
    val unresponsiveEngines: List<EngineInfo>?
)

data class SearchResult(
    val title: String,
    val url: String,
    val content: String?,
    val engine: String?,
    val category: String?,
    @SerializedName("publishedDate")
    val publishedDate: String?,
    val thumbnail: String?,
    @SerializedName("img_src")
    val imgSrc: String?,
    @SerializedName("parsed_url")
    val parsedUrl: ParsedUrl?
)

data class ParsedUrl(
    val parts: List<String>?,
    val url: String?
)

data class Infobox(
    val infobox: String?,
    val content: String?,
    val urls: List<InfoboxUrl>?
)

data class InfoboxUrl(
    val title: String?,
    val url: String?
)

data class EngineInfo(
    val engine: String?
)
