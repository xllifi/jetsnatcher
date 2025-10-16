package ru.xllifi.booru_api.gelbooru

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
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.Response
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import ru.xllifi.booru_api.Note
import ru.xllifi.booru_api.Providers
import ru.xllifi.booru_api.errors.UnauthorizedException
import ru.xllifi.booru_api.toUnixTimestamp

class Gelbooru(
  override var httpClient: HttpClient = HttpClient(CIO) {
    install(ContentNegotiation, defaultContentConfig)
  },
  override var routes: Routes = defaultRoutes,
) : Provider(httpClient, routes) {
  suspend fun checkBadStatus(response: HttpResponse) {
    if (response.status == HttpStatusCode.Unauthorized) {
      throw UnauthorizedException(
        providerType = Providers.Gelbooru,
        url = response.request.url.toString(),
        respBody = response.bodyAsText(),
      )
    }
  }

  override suspend fun getAutoComplete(tagPart: String): List<Tag> {
    val response = httpClient.get(this.routes.parseAutocomplete(tagPart))
    val tags: List<GelbooruAutocompleteTag> = response.body()
    return tags.map { it.toGenericTag() }
  }

  override suspend fun getPosts(
    tags: List<String>,
    page: Int,
    limit: Int,
  ): List<Post> {
    val response = httpClient.get(this.routes.parsePosts(tags, page, limit))
    checkBadStatus(response)
    val resp: PostsResponse = response.body()
    return resp.posts.map { it.toGenericPost(this.routes) }
  }

  override suspend fun getComments(postId: Int): List<Comment> {
    return emptyList()
  }

  override suspend fun getNotes(postId: Int): List<Note>? {
    val response = httpClient.get(this.routes.parseNotes(postId))
    checkBadStatus(response)
    val body: List<GelbooruNote> = response.body()
    return body.map { it.toGenericNote() }
  }

  override suspend fun getTags(
    tags: List<String>,
    page: Int,
    limit: Int,
  ): List<Tag> {
    val response = httpClient.get(this.routes.parseTags(tags, page, limit))
    checkBadStatus(response)
    val body: List<GelbooruTag> = response.body()
    return body.map { it.toGenericTag() }
  }

  companion object {
    val defaultContentConfig: ContentNegotiationConfig.() -> Unit = {
      xml(contentType = ContentType.Text.Xml)
      json()
    }
    val defaultRoutes = Routes(
      base = "https://gelbooru.com",
      publicFacingPostPage = "https://gelbooru.com/index.php?page=post&s=view&id={id}",
      autocomplete = "index.php?page=autocomplete2&type=tag_query&term={tagPart}",
      posts = "index.php?page=dapi&s=post&q=index&limit={limit}&pid={page}&tags={tags}",
      tags = "index.php?page=dapi&s=tag&q=index&limit={limit}&pid={page}&names={tags}",
      comments = "index.php?page=dapi&s=comment&q=index&post_id={postId}",
      notes = "index.php?page=dapi&s=note&q=index&post_id={postId}",
      authSuffix = null,
    )
  }
}

fun GelbooruPost.toGenericPost(routes: Routes): Post {
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
    authorName = this.authorName,
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

fun GelbooruPostRating.toGenericRating(): Rating {
  return when (this) {
    GelbooruPostRating.General -> Rating.General
    GelbooruPostRating.Sensitive -> Rating.Sensitive
    GelbooruPostRating.Questionable -> Rating.Questionable
    GelbooruPostRating.Explicit -> Rating.Explicit
  }
}

fun GelbooruAutocompleteTag.toGenericTag(): Tag {
  return Tag(
    label = this.label,
    value = this.value,
    postCount = this.postCount.toIntOrNull() ?: -1,
    category = this.category.toGenericTagCategory()
  )
}

fun GelbooruAutocompleteTagCategory.toGenericTagCategory(): TagCategory {
  return when (this) {
    GelbooruAutocompleteTagCategory.General -> TagCategory.General
    GelbooruAutocompleteTagCategory.Artist -> TagCategory.Artist
    GelbooruAutocompleteTagCategory.Copyright -> TagCategory.Copyright
    GelbooruAutocompleteTagCategory.Character -> TagCategory.Character
    GelbooruAutocompleteTagCategory.Metadata -> TagCategory.Metadata
  }
}

fun GelbooruTag.toGenericTag(): Tag {
  return Tag(
    label = this.name.replace('_', ' '),
    value = this.name,
    postCount = this.count,
    category = this.type.toGenericTagCategory()
  )
}

fun GelbooruTagType.toGenericTagCategory(): TagCategory {
  return when (this) {
    GelbooruTagType.General -> TagCategory.General
    GelbooruTagType.Artist -> TagCategory.Artist
    GelbooruTagType.Unknown -> TagCategory.Unknown
    GelbooruTagType.Copyright -> TagCategory.Copyright
    GelbooruTagType.Character -> TagCategory.Character
    GelbooruTagType.Metadata -> TagCategory.Metadata
    GelbooruTagType.Deprecated -> TagCategory.Deprecated
  }
}

//fun GelbooruComment.toGenericComment(): Comment {
//  return Comment(
//    id = this.id,
//    body = this.body,
//    authorName = this.creatorName,
//    authorId = this.creatorId,
//    createdAt = this.createdAt.ru.xllifi.booru_api.toUnixTimestamp("yyyy-MM-dd HH:mm"),
//    postId = this.postId
//  )
//}

fun GelbooruNote.toGenericNote(): Note {
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