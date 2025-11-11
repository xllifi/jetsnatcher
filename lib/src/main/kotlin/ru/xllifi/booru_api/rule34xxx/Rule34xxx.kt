package ru.xllifi.booru_api.rule34xxx

import ru.xllifi.booru_api.Comment
import ru.xllifi.booru_api.Image
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Provider
import ru.xllifi.booru_api.Rating
import ru.xllifi.booru_api.Routes
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiationConfig
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.xml.xml
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import ru.xllifi.booru_api.Note
import ru.xllifi.booru_api.ProviderType
import ru.xllifi.booru_api.toUnixTimestamp

class Rule34xxx(
  override var httpClient: HttpClient = HttpClient(CIO) {
    install(
      ContentNegotiation,
      defaultContentConfig
    )
  },
  override var routes: Routes = defaultRoutes,
) : Provider(httpClient, routes, ProviderType.Rule34xxx) {
  override suspend fun getAutoComplete(tagPart: String): List<Tag> {
    val response = httpClient.get(this.routes.parseAutocomplete(tagPart))
    checkBadStatus(response)
    val tags: List<Rule34xxxAutocompleteTag> = response.body()
    return tags.map { it.toGenericTag() }
  }

  override suspend fun getPosts(
    tags: List<String>,
    page: Int,
    limit: Int,
  ): List<Post> {
    val response = httpClient.get(this.routes.parsePosts(tags, page, limit))
    checkBadStatus(response)
    val posts: List<Rule34xxxPost> = response.body()
    return posts.map { it.toGenericPost(this.routes) }
  }

  override suspend fun getComments(postId: Int): List<Comment> {
    val response = httpClient.get(this.routes.parseComments(postId))
    checkBadStatus(response)
    val body: List<Rule34xxxComment> = response.body()
    return body.map { it.toGenericComment() }
  }

  override suspend fun getNotes(postId: Int): List<Note> {
    val response = httpClient.get(this.routes.parseNotes(postId))
    checkBadStatus(response)
    val body: List<Rule34xxxNote> = response.body()
    return body.map { it.toGenericNote() }
  }

  @OptIn(DelicateCoroutinesApi::class)
  override suspend fun getTags(
    tags: List<String>,
    page: Int,
    limit: Int,
  ): List<Tag> {
    val deferredResults = tags.map {
      GlobalScope.async {
        httpClient.get(
          this@Rule34xxx.routes.parseTags(listOf(it), page, limit)
        )
      }
    }
    val respTags = deferredResults
      .awaitAll()
      .mapIndexed { index, response ->
        response
          .body<List<Rule34xxxTag>>()
          .firstOrNull()
          ?: Rule34xxxTag(
            type = Rule34xxxTagType.Unknown,
            count = -1,
            name = tags[index],
            ambiguous = false,
            id = index,
          )
      }
    return respTags.map { it.toGenericTag() }
  }

  companion object {
    val defaultContentConfig: ContentNegotiationConfig.() -> Unit = {
      xml(
        format = XML(
          configure = {
            xmlDeclMode = XmlDeclMode.Charset
          }
        ),
        contentType = ContentType.Text.Xml
      )
      json(contentType = ContentType.Text.Html)
    }
    val defaultRoutes = Routes(
      base = "https://api.rule34.xxx",
      publicFacingPostPage = "https://rule34.xxx/index.php?page=post&s=view&id={id}",
      autocomplete = "autocomplete.php?q={tagPart}",
      posts = "index.php?page=dapi&s=post&q=index&limit={limit}&pid={page}&tags={tags}",
      tags = "index.php?page=dapi&s=tag&q=index&limit={limit}&pid={page}&name={tags}",
      comments = "index.php?page=dapi&s=comment&q=index&post_id={postId}",
      notes = "index.php?page=dapi&s=note&q=index&post_id={postId}",
      authSuffix = null,
    )
  }
}

fun Rule34xxxPost.toGenericPost(routes: Routes): Post {
  return Post(
    id = this.id,
    rating = this.rating.toGenericRating(),
    tags = null,
    unparsedTags = this.tags.split(' '),
    score = this.score,
    bestQualityImage = Image(
      url = this.originalImgUrl,
      width = this.originalImgWidth,
      height = this.originalImgHeight,
    ),
    mediumQualityImage = Image(
      url = this.sampleImgUrl,
      width = this.sampleImgWidth,
      height = this.sampleImgHeight,
    ),
    worstQualityImage = Image(
      url = this.previewImgUrl,
      width = this.previewImgWidth,
      height = this.previewImgHeight,
    ),
    authorName = null,
    authorId = this.authorId,
    changedAt = this.changedAt,
    createdAt = this.createdAt.toUnixTimestamp("EEE MMM dd HH:mm:ss XX yyyy"),
    hasNotes = this.hasNotes,
    hasComments = this.hasComments,
    hasChildren = this.hasChildren,
    source = this.source,
    locationLink = routes.parsePublicFacingPostPage(this.id),
  )
}

fun Rule34xxxPostRating.toGenericRating(): Rating {
  return when (this) {
    Rule34xxxPostRating.Safe -> Rating.General
    Rule34xxxPostRating.Questionable -> Rating.Questionable
    Rule34xxxPostRating.Explicit -> Rating.Explicit
  }
}

fun Rule34xxxAutocompleteTag.toGenericTag(): Tag {
  val split = this.label.replace(Regex("""(.+)\s\((\d+)\)"""), "$1:$2").split(":")
  val label = split.first().replace("_", " ")
  val count = split.last().toIntOrNull() ?: 0
  return Tag(
    label = label,
    value = this.value,
    postCount = count,
    category = TagCategory.General
  )
}

fun Rule34xxxTag.toGenericTag(): Tag {
  return Tag(
    label = this.name.replace('_', ' '),
    value = this.name,
    postCount = this.count,
    category = this.type.toGenericTagCategory()
  )
}

fun Rule34xxxTagType.toGenericTagCategory(): TagCategory {
  return when (this) {
    Rule34xxxTagType.General -> TagCategory.General
    Rule34xxxTagType.Artist -> TagCategory.Artist
    Rule34xxxTagType.Unknown -> TagCategory.Unknown
    Rule34xxxTagType.Copyright -> TagCategory.Copyright
    Rule34xxxTagType.Character -> TagCategory.Character
    Rule34xxxTagType.Metadata -> TagCategory.Metadata
  }
}

fun Rule34xxxComment.toGenericComment(): Comment {
  return Comment(
    id = this.id,
    body = this.body,
    authorName = this.creatorName,
    authorId = this.creatorId,
    createdAt = this.createdAt.toUnixTimestamp("yyyy-MM-dd HH:mm"),
    postId = this.postId
  )
}

fun Rule34xxxNote.toGenericNote(): Note {
  return Note(
    id = this.id,
    postId = this.postId,
    isActive = this.isActive,
    x = this.x,
    y = this.y,
    width = this.width,
    height = this.height,
    body = this.body,
    authorId = this.authorId,
    createdAt = this.createdAt.toUnixTimestamp("EEE MMM dd HH:mm:ss XX yyyy")
  )
}