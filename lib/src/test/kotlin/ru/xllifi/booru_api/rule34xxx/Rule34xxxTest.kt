package ru.xllifi.booru_api.rule34xxx

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.xllifi.booru_api.Image
import ru.xllifi.booru_api.Note
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Rating
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory
import ru.xllifi.booru_api.respondXml

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
private const val notesResponse = """<notes type="array">
  <note
    version="1"
    id="0"
    post_id="0"
    x="100"
    y="100"
    width="10"
    height="10"
    body="Note text"
    creator_id="0"
    updated_at="Thu Jan 01 00:00:00 +0000 1970"
    created_at="Thu Jan 01 00:00:00 +0000 1970"
    is_active="true"
  />
</notes>"""
private val correctNotes: List<Note> = listOf(
  Note(
    id = 0,
    postId = 0,
    x = 100,
    y = 100,
    width = 10,
    height = 10,
    body = "Note text",
    authorId = 0,
    createdAt = 0,
    isActive = true,
  )
)

class Rule34xxxTest {
  private val engine = MockEngine { request ->
    val url = request.url.toString()
    if (url.contains("s=post")) {
      respondXml(content = postsResponse)
    } else if (url.contains("s=tag")) {
      respondXml(content = tagsResponse)
    } else if (url.contains("s=note")) {
      respondXml(content = notesResponse)
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
      val r34 = Rule34xxx(httpClient)
      val autocomplete = r34.getAutoComplete("yui")
      println(autocomplete)
      println(correctAutocomplete)
      assert(autocomplete == correctAutocomplete)
    }
  }

  @Test
  fun getPosts() {
    runBlocking {
      val r34 = Rule34xxx(httpClient)
      val posts = r34.getPosts(listOf(), 0, 0)
      println(posts)
      assert(posts == correctPosts)
    }
  }

  @Test
  fun getTags() {
    runBlocking {
      val r34 = Rule34xxx(httpClient)
      val tags = r34.getTags(listOf("tag_1"), 0, 0)
      assert(tags == correctTags)
    }
  }

  @Test
  fun getComments() {
    TODO("Not yet implemented")
  }

  @Test
  fun getNotes() {
    runBlocking {
      val r34 = Rule34xxx(httpClient)
      val notes = r34.getNotes(0)
      assert(notes == correctNotes)
    }
  }

}