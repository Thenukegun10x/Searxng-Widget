package com.searxng.widget.data.api

import com.searxng.widget.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearxngApi {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("categories") categories: String? = null,
        @Query("language") language: String? = null,
        @Query("pageno") pageNo: Int? = null,
        @Query("time_range") timeRange: String? = null
    ): SearchResponse
}
