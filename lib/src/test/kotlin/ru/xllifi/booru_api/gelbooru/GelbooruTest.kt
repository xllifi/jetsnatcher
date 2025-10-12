package ru.xllifi.booru_api.gelbooru

import ru.xllifi.booru_api.Image
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Rating
import ru.xllifi.booru_api.Tag
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.xllifi.booru_api.Note
import ru.xllifi.booru_api.TagCategory

const val autocompleteResponse = """[
  {
    "type": "tag",
    "label": "tag 1",
    "value": "tag_1",
    "post_count": "100",
    "category": "tag"
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
const val postsResponse = """<posts limit="1" offset="0" count="1">
  <post>
    <score>0</score>
    <parent_id>0</parent_id>
    <rating>general</rating>
    <tags>tag1 tag2</tags>
    <id>0</id>
    <created_at>Thu Jan 01 00:00:00 Z 1970</created_at>
    <change>0</change>
    <md5>abcdef</md5>
    <owner>xllifi</owner>
    <creator_id>0</creator_id>
    <status>active</status>
    <source>https://github.com</source>
    <has_notes>false</has_notes>
    <has_comments>false</has_comments>
    <has_children>false</has_children>
    <width>1024</width>
    <height>1024</height>
    <file_url>https://example.com/best</file_url>
    <sample_width>512</sample_width>
    <sample_height>512</sample_height>
    <sample_url>https://example.com/medium</sample_url>
    <preview_width>256</preview_width>
    <preview_height>256</preview_height>
    <preview_url>https://example.com/worst</preview_url>
    <directory>ab/cd</directory>
    <image>abcdef.png</image>
    <sample>1</sample>
    <title />
    <post_locked>0</post_locked>
  </post>
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
    authorName = "xllifi",
    authorId = 0,
    changedAt = 0,
    createdAt = 0,
    hasNotes = false,
    hasComments = false,
    hasChildren = false,
    locationLink = "https://gelbooru.com/index.php?page=post&s=view&id=0",
    source = "https://github.com",
    notes = null,
  )
)
const val tagsResponse = """<tags type="array" limit="1" offset="0" count="1">
  <tag>
    <id>0</id>
    <name>tag_1</name>
    <count>100</count>
    <type>0</type>
    <ambiguous>0</ambiguous>
  </tag>
</tags>"""
val correctTags: List<Tag> = listOf(
  Tag(
    label = "tag 1",
    value = "tag_1",
    postCount = 100,
    category = TagCategory.General
  )
)

const val notesResponse = """<tags type="array" limit="1" offset="0" count="1">
  <tag>
    <id>0</id>
    <name>tag_1</name>
    <count>100</count>
    <type>0</type>
    <ambiguous>0</ambiguous>
  </tag>
</tags>"""
val correctNotes: List<Note> = listOf(
  Note(
    id = 0,
    postId = 0,
    isActive = true,
    x = 0,
    y = 0,
    width = 16,
    height = 16,
    body = "Note 1",
    authorId = 0,
    createdAt = 0
  )
)

fun MockRequestHandleScope.respondXml(content: String): HttpResponseData = respond(
  content,
  status = HttpStatusCode.OK,
  headers = headersOf("Content-Type" to listOf("text/xml"))
)

class GelbooruTest {
  private val engine = MockEngine { request ->
    val url = request.url.toString()
    if (url.contains("s=post")) {
      respondXml(
        content = postsResponse
      )
    } else if (url.contains("s=tag")) {
      respondXml(
        content = tagsResponse
      )
    } else if (url.contains("autocomplete")) {
      respond(
        content = autocompleteResponse,
        status = HttpStatusCode.OK,
        headers = headersOf("Content-Type" to listOf("application/json"))
      )
    } else {
      respondOk("Unknown request")
    }
  }
  private val httpClient = HttpClient(engine) {
    install(ContentNegotiation, Gelbooru.defaultContentConfig)
  }

  @Test
  fun getAutoComplete() {
    runBlocking {
      val gb = Gelbooru(httpClient)
      val autocomplete = gb.getAutoComplete("")
      assert(autocomplete == correctAutocomplete)
    }
  }

  @Test
  fun getPosts() {
    runBlocking {
      val gb = Gelbooru(httpClient)
      val posts = gb.getPosts(listOf(), 0, 0)
      println(posts)
      assert(posts == correctPosts)
    }
  }

  @Test
  fun getTags() {
    runBlocking {
      val gb = Gelbooru(httpClient)
      val tags = gb.getTags(listOf(), 0, 0)
      assert(tags == correctTags)
    }
  }
}