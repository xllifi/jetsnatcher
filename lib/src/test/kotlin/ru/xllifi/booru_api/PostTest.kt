package ru.xllifi.booru_api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.xllifi.booru_api.gelbooru.Gelbooru
import kotlin.test.assertEquals

val samplePost = Post(
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
  hasNotes = true,
  hasComments = false,
  hasChildren = false,
  locationLink = "https://gelbooru.com/index.php?page=post&s=view&id=0",
  source = "https://github.com",
  notes = null,
)

private const val tagsResponse = """<tags type="array" limit="2" offset="0" count="2">
  <tag>
    <id>0</id>
    <name>tag1</name>
    <count>100</count>
    <type>0</type>
    <ambiguous>0</ambiguous>
  </tag>
  <tag>
    <id>1</id>
    <name>tag2</name>
    <count>200</count>
    <type>1</type>
    <ambiguous>0</ambiguous>
  </tag>
</tags>"""
private val correctTags: List<Tag> = listOf(
  Tag(
    label = "tag1",
    value = "tag1",
    postCount = 100,
    category = TagCategory.General
  ),
  Tag(
    label = "tag2",
    value = "tag2",
    postCount = 200,
    category = TagCategory.Artist
  )
).sortedWith(tagSortingComparator)


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

class PostTest {
  private val engine = MockEngine { request ->
    val url = request.url.toString()
    if (url.contains("s=tag")) {
      respondXml(tagsResponse)
    } else if (url.contains("s=note")) {
      respondXml(notesResponse)
    } else {
      respondOk("Unknown request")
    }
  }
  private val httpClient = HttpClient(engine) {
    install(ContentNegotiation, Gelbooru.defaultContentConfig)
  }

  @Test
  fun parseTags() {
    runBlocking {
      val gb = Gelbooru(httpClient)
      val tags = samplePost.parseTags(gb)
      assertEquals(correctTags, tags)
    }
  }

  @Test
  fun parseNotes() {
    runBlocking {
      val gb = Gelbooru(httpClient)
      val notes = samplePost.parseNotes(gb)
      assertEquals(correctNotes, notes)
    }
  }
}