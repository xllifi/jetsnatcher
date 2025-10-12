package ru.xllifi.booru_api.rule34xxx

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.xllifi.booru_api.Image
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Rating
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory

const val autocompleteResponse = """[
  {
    "label": "tag_1 (100)",
    "value": "tag_1"
  }
]"""
val correctAutocomplete: List<Tag> = listOf(
  Tag(
    label = "tag 1",
    value = "tag_1",
    postCount = 100,
    category = TagCategory.General
  )
)
const val postsResponse = """<posts count="1" offset="0">
  <post
    score="0"
    parent_id="0"
    rating="s"
    tags="tag1 tag2"
    id="0"
    created_at="Thu Jan 01 00:00:00 Z 1970"
    change="0"
    md5="abcdef"
    creator_id="0"
    status="active"
    source="https://github.com"
    has_notes="false"
    has_comments="false"
    has_children="false"
    width="1024"
    height="1024"
    file_url="https://example.com/best"
    sample_width="512"
    sample_height="512"
    sample_url="https://example.com/medium"
    preview_width="256"
    preview_height="256"
    preview_url="https://example.com/worst"
  />
</posts>"""
val correctPosts: List<Post> = listOf(
  Post(
    id = 0,
    rating = Rating.General,
    tags = null,
    unparsedTags = listOf("tag1", "tag2"),
    score = 0,
    bestQualityImage = Image(
      url = "https://example.com/best",
      width = 1024,
      height = 1024
    ),
    mediumQualityImage = Image(
      url = "https://example.com/medium",
      width = 512,
      height = 512
    ),
    worstQualityImage = Image(
      url = "https://example.com/worst",
      width = 256,
      height = 256
    ),
    authorName = null,
    authorId = 0,
    changedAt = 0,
    createdAt = 0,
    hasNotes = false,
    hasComments = false,
    hasChildren = false,
    locationLink = "https://rule34.xxx/index.php?page=post&s=view&id=0",
    source = "https://github.com",
    notes = null,
  )
)
const val tagsResponse = """<tags type="array">
    <tag
        id="0"
        name="tag_1"
        count="100"
        type="0"
        ambiguous="false"
    />
</tags>"""
val correctTags: List<Tag> = listOf(
  Tag(
    label = "tag 1",
    value = "tag_1",
    postCount = 100,
    category = TagCategory.General
  )
)

fun MockRequestHandleScope.respondXml(content: String): HttpResponseData = respond(
  content,
  status = HttpStatusCode.OK,
  headers = headersOf(HttpHeaders.ContentType, "text/xml")
)

class Rule34xxxTest {
  private val engine = MockEngine { request ->
    val url = request.url.toString()
    if (url.contains("s=post")) {
      respondXml(content = postsResponse)
    } else if (url.contains("s=tag")) {
      respondXml(content = tagsResponse)
    } else if (url.contains("autocomplete")) {
      respond(
        content = autocompleteResponse,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "text/html"),
      )
    } else {
      respondOk("Unknown request")
    }
  }
  private val httpClient = HttpClient(engine) {
    install(ContentNegotiation, Rule34xxx.defaultContentConfig)
  }

  @Test
  fun getAutoComplete() {
    runBlocking {
      val gb = Rule34xxx(httpClient)
      val autocomplete = gb.getAutoComplete("yui")
      println(autocomplete)
      println(correctAutocomplete)
      assert(autocomplete == correctAutocomplete)
    }
  }

  @Test
  fun getPosts() {
    runBlocking {
      val gb = Rule34xxx(httpClient)
      val posts = gb.getPosts(listOf(), 0, 0)
      println(posts)
      assert(posts == correctPosts)
    }
  }

  @Test
  fun getTags() {
    runBlocking {
      val gb = Rule34xxx(httpClient)
      val tags = gb.getTags(listOf(), 0, 0)
      assert(tags == correctTags)
    }
  }

  @Test
  fun getComments() {
  }
}