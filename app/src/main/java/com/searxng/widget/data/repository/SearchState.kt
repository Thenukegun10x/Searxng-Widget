package com.searxng.widget.data.repository

import com.searxng.widget.data.model.SearchResult

sealed interface SearchState {
    data object Idle : SearchState
    data object Loading : SearchState
    data class Success(val results: List<SearchResult>, val query: String) : SearchState
    data class Error(val message: String) : SearchState
}
