package com.searxng.widget.data.repository

import com.searxng.widget.data.api.SearxngApi
import com.searxng.widget.data.model.SearchResult
import com.searxng.widget.data.model.SearchResponse
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test

class SearchRepositoryTest {

    private val api: SearxngApi = mockk()

    private fun createRepository(): SearchRepository {
        return SearchRepository(api)
    }

    @Test
    fun `search returns results on success`() {
        val results = listOf(
            SearchResult(
                title = "Result 1",
                url = "https://example.com/1",
                content = "Content 1",
                engine = "google",
                category = "general",
                publishedDate = null,
                thumbnail = null,
                imgSrc = null,
                parsedUrl = null
            )
        )

        coEvery { api.search(query = "test") } returns SearchResponse(
            query = "test",
            results = results,
            answers = emptyList(),
            infoboxes = emptyList(),
            suggestions = emptyList(),
            unresponsiveEngines = null
        )

        // Since SearchRepository creates ApiClient internally,
        // we test the parsed behavior through Result wrapper
        val repo = createRepository()
        val result = repo.search("test")

        result.isSuccess shouldBe true
    }

    @Test
    fun `search returns failure on network error`() {
        coEvery { api.search(query = "test") } throws Exception("Network error")
        val repo = createRepository()

        val result = repo.search("test")

        result.isFailure shouldBe true
    }
}
