package com.searxng.widget.data.repository

import com.searxng.widget.data.api.ApiClient
import com.searxng.widget.data.api.SearxngApi
import com.searxng.widget.data.model.SearchResult

class SearchRepository(private val api: SearxngApi) {

    constructor(instanceUrl: String, authToken: String? = null) : this(
        ApiClient.create(instanceUrl, authToken)
    )

    suspend fun search(query: String): Result<List<SearchResult>> {
        return try {
            val response = api.search(query = query)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
