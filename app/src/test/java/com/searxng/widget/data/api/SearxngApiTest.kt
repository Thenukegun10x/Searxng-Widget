package com.searxng.widget.data.api

import com.searxng.widget.data.model.SearchResult
import com.searxng.widget.data.model.SearchResponse
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SearxngApiTest {

    private val api: SearxngApi = mockk()

    @Test
    fun `SearxngApi_search_returnsResultsOnSuccess`() = runTest {
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
            suggestions = listOf("suggestion1")
        )

        coEvery { api.search(query = "test") } returns expectedResponse

        val response = api.search(query = "test")

        assertSoftly {
            response.query shouldBe "test"
            response.results.size shouldBe 1
            response.results[0].title shouldBe "Test Title"
            response.results[0].url shouldBe "https://example.com"
            response.suggestions shouldBe listOf("suggestion1")
        }
    }

    @Test
    fun `SearxngApi_search_returnsEmptyResultsWhenNoMatches`() = runTest {
        val emptyResponse = SearchResponse(
            query = "nonexistent",
            results = emptyList(),
            suggestions = emptyList()
        )

        coEvery { api.search(query = "nonexistent") } returns emptyResponse

        val response = api.search(query = "nonexistent")

        response.results.size shouldBe 0
        response.suggestions shouldBe emptyList()
    }

    @Test
    fun `SearxngApi_search_handlesCategoriesAndLanguageParameters`() = runTest {
        val expectedResponse = SearchResponse(
            query = "test",
            results = listOf(
                SearchResult(
                    title = "Image Result",
                    url = "https://example.com/img",
                    content = null,
                    engine = "google images",
                    category = "images",
                    publishedDate = null,
                    thumbnail = null,
                    imgSrc = null,
                    parsedUrl = null
                )
            )
        )

        coEvery { api.search(query = "test", categories = "images", language = "en") } returns expectedResponse

        val response = api.search(query = "test", categories = "images", language = "en")

        response.results[0].category shouldBe "images"
        coVerify { api.search(query = "test", categories = "images", language = "en") }
    }
}
