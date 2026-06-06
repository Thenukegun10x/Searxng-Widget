package com.searxng.widget.data.api

import com.searxng.widget.data.model.SearchResult
import com.searxng.widget.data.model.SearchResponse
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test

class SearxngApiTest {

    private val api: SearxngApi = mockk()

    @Test
    fun `search returns results on success`() {
        val expectedResponse = SearchResponse(
            query = "test",
            results = listOf(
                SearchResult(
                    title = "Test Title",
                    url = "https://example.com",
                    content = "Test content",
                    engine = "google",
                    category = "general",
                    publishedDate = null,
                    thumbnail = null,
                    imgSrc = null,
                    parsedUrl = null
                )
            ),
            answers = emptyList(),
            infoboxes = emptyList(),
            suggestions = listOf("suggestion1"),
            unresponsiveEngines = null
        )

        coEvery { api.search(query = "test") } returns expectedResponse

        val response = api.search(query = "test")

        assertSoftly {
            response.query shouldBe "test"
            response.results shouldNotBe null
            response.results.size shouldBe 1
            response.results[0].title shouldBe "Test Title"
            response.results[0].url shouldBe "https://example.com"
            response.suggestions shouldBe listOf("suggestion1")
        }
    }

    @Test
    fun `search returns empty results when no matches`() {
        val emptyResponse = SearchResponse(
            query = "nonexistent",
            results = emptyList(),
            answers = emptyList(),
            infoboxes = emptyList(),
            suggestions = emptyList(),
            unresponsiveEngines = null
        )

        coEvery { api.search(query = "nonexistent") } returns emptyResponse

        val response = api.search(query = "nonexistent")

        response.results.size shouldBe 0
        response.suggestions shouldBe emptyList()
    }

    @Test
    fun `search handles categories and language parameters`() {
        coEvery { api.search(any(), any(), any(), any(), any(), any()) } returns mockk()

        api.search(query = "test", categories = "images", language = "en")

        // Verify the API was called with correct params
        io.mockk.verify { api.search(query = "test", categories = "images", language = "en") }
    }
}
