package ru.xllifi.booru_api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.serialization.Serializable
import ru.xllifi.booru_api.errors.UnauthorizedException

typealias ProviderConstructor = (httpClient: HttpClient, routes: Routes) -> Provider

enum class ProviderType {
  Rule34xxx,
  Gelbooru;

  fun getFormattedName(): String {
    return when (this) {
      Rule34xxx -> "Rule34.xxx"
      Gelbooru -> "Gelbooru"
    }
  }
  fun getDefaultRoutes(): Routes {
    return when (this) {
      Rule34xxx -> ru.xllifi.booru_api.rule34xxx.Rule34xxx.defaultRoutes
      Gelbooru -> ru.xllifi.booru_api.gelbooru.Gelbooru.defaultRoutes
    }
  }
}

abstract class Provider(
  open var httpClient: HttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
      xml()
      json()
    }
  },
  open var routes: Routes,
  open var type: ProviderType,
) {
  suspend fun checkBadStatus(response: HttpResponse) {
    if (response.status == HttpStatusCode.Unauthorized) {
      throw UnauthorizedException(
        providerType = type,
        url = response.request.url.toString(),
        respBody = response.bodyAsText(),
      )
    }
  }
  abstract suspend fun getAutoComplete(tagPart: String): List<Tag>?
  abstract suspend fun getPosts(tags: List<String>, page: Int = 0, limit: Int = 100): List<Post>?
  abstract suspend fun getComments(postId: Int): List<Comment>?
  abstract suspend fun getNotes(postId: Int): List<Note>?
  abstract suspend fun getTags(
    tags: List<String>,
    page: Int,
    limit: Int,
  ): List<Tag>?
}

@Serializable
data class Routes(
  val base: String,
  /**
   * Substitutions:
   * - `{id}` for post ID
   * */
  val publicFacingPostPage: String,
  /**
   * Substitutions:
   * - `{tagPart}` for tagPart
   * */
  val autocomplete: String,
  /**
   * Substitutions:
   * - `{tags}` for tags
   * - `{page}` for page
   * - `{limit}` for limit
   * */
  val posts: String,
  /**
   * Substitutions:
   * - `{tags}` for tags
   * - `{page}` for page
   * - `{limit}` for limit
   * */
  val tags: String,
  /**
   * Substitutions:
   * - `{postId}` for postId
   * */
  val comments: String,
  /**
   * Substitutions:
   * - `{postId}` for postId
   * */
  val notes: String,
  val authSuffix: String?,
) { // TODO: write tests
  private fun base(): String {
    return this.base.replace(Regex("/$"), "") + "/"
  }

  private fun wrap(what: String): String {
    return this.base() + what.replace(Regex("^/"), "") + (authSuffix ?: "")
  }

  fun parsePublicFacingPostPage(id: Int): String {
    return publicFacingPostPage.replace("{id}", "$id")
  }

  fun parseAutocomplete(tagPart: String): String {
    return this.wrap(
      this.autocomplete
        .replace("{tagPart}", tagPart)
    )
  }

  fun parsePosts(tags: List<String>, page: Int = 0, limit: Int = 100): String {
    return this.wrap(
      this.posts
        .replace("{tags}", tags.joinToString("%20").replace(" ", "%20"))
        .replace("{page}", "$page")
        .replace("{limit}", "$limit")
    )
  }

  fun parseTags(tags: List<String>, page: Int = 0, limit: Int = 100): String {
    return this.wrap(
      this.tags
        .replace("{tags}", tags.joinToString("%20").replace(" ", "%20"))
        .replace("{page}", "$page")
        .replace("{limit}", "$limit")
    )
  }

  fun parseComments(postId: Int): String {
    return this.wrap(
      this.comments
        .replace("{postId}", postId.toString())
    )
  }

  fun parseNotes(postId: Int): String {
    return this.wrap(
      this.notes
        .replace("{postId}", postId.toString())
    )
  }
}

@Serializable
data class Tag(
  val label: String,
  val value: String,
  val postCount: Int,
  val category: TagCategory,
)

@Serializable
enum class TagCategory() {
  Unknown,
  General,
  Artist,
  Copyright,
  Character,
  Metadata,
  Deprecated;

  fun hue(): Float {
    return when (this) {
      Unknown -> 300f
      General -> 200f
      Artist -> 260f
      Copyright -> 150f
      Character -> 80f
      Metadata -> 30f
      Deprecated -> 0f
    }
  }

  fun priority(): Int {
    return when (this) {
      Unknown -> 0
      Artist -> 1
      Metadata -> 2
      Copyright -> 3
      Character -> 4
      Deprecated -> 5
      General -> 5
    }
  }

  companion object {
    fun fromInt(value: Int) = entries.first { it.ordinal == value }
  }
}

@Serializable
data class Post(
  val id: Int,
  val rating: Rating,
  val tags: List<Tag>?,
  val unparsedTags: List<String>,
  val score: Int,
  val bestQualityImage: Image,
  val mediumQualityImage: Image,
  val worstQualityImage: Image,
  val authorName: String?,
  val authorId: Int,
  val changedAt: Long,
  val createdAt: Long,
  val hasNotes: Boolean,
  val hasComments: Boolean,
  val hasChildren: Boolean,
  val locationLink: String? = null,
  val source: String? = null,
  val notes: List<Note>? = null,
) {
  fun isMediumQualityImagePresent(): Boolean {
    return !this.mediumQualityImage.url.isEmpty()
  }

  fun getImageForPreview(): Image {
    return this.worstQualityImage
  }

  fun getImageForFullscreen(): Image {
    return if (this.isMediumQualityImagePresent()) {
      this.mediumQualityImage
    } else {
      this.bestQualityImage
    }
  }

  suspend fun parseTags(provider: Provider): List<Tag> {
    return provider.getTags(this.unparsedTags, page = 0, limit = this.unparsedTags.size)
      ?.sortedWith(compareBy({ it.category.priority() }, { it.label }))
      ?: emptyList()
  }

  suspend fun parseNotes(provider: Provider): List<Note> {
    return provider.getNotes(this.id)?: emptyList()
  }
}

/** See [Danbooru Wiki `howto:rate`](https://danbooru.donmai.us/wiki_pages/howto:rate) */
@Serializable
enum class Rating {
  General,
  Sensitive,
  Questionable,
  Explicit,
}

/** Image's URL and dimensions. */
@Serializable
data class Image(
  val url: String,
  val width: Int,
  val height: Int,
)

@Serializable
data class Comment(
  val id: Int,
  /** Associated post's ID */
  val postId: Int,
  val body: String,
  val authorName: String,
  val authorId: Int,
  /** UNIX epoch (UTC) timestamp of when this comment was created */
  val createdAt: Long,
)

@Serializable
data class Note(
  val id: Int,
  /** Associated post's ID */
  val postId: Int,
  val isActive: Boolean,
  val x: Int,
  val y: Int,
  val width: Int,
  val height: Int,
  val body: String,
  val authorId: Int,
  /** UNIX epoch (UTC) timestamp of when this comment was created */
  val createdAt: Long,
)