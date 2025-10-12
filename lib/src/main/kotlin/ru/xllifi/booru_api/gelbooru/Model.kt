package ru.xllifi.booru_api.gelbooru

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("posts")
data class PostsResponse(
  val limit: Int,
  val offset: Int,
  val count: Int,
  val posts: List<GelbooruPost>
)

@Serializable
@XmlSerialName("post")
data class GelbooruPost(
  @XmlElement val score: Int,
  @SerialName("parent_id")
  @XmlElement val parentId: Int,
  @XmlElement val rating: GelbooruPostRating,
  @XmlElement val tags: String,
  @XmlElement val id: Int,
  /**
   * String formatted like `EEE MMM dd HH:mm:ss XX yyyy`
   * (see [Java Date Format](https://docs.oracle.com/en/java/javase/21/docs/api//java.base/java/time/format/DateTimeFormatter.html#patterns))
   * */
  @SerialName("created_at")
  @XmlElement val createdAt: String,
  /** UNIX epoch UTC timestamp */
  @SerialName("change")
  @XmlElement val changedAt: Long,
  @XmlElement val md5: String,
  @SerialName("owner")
  @XmlElement val authorName: String,
  @SerialName("creator_id")
  @XmlElement val authorId: Int,
  @XmlElement val status: String,
  @XmlElement val source: String?,
  @SerialName("has_notes")
  @XmlElement val hasNotes: Boolean,
  @SerialName("has_comments")
  @XmlElement val hasComments: Boolean,
  @SerialName("has_children")
  @XmlElement val hasChildren: Boolean,
  @SerialName("width")
  @XmlElement val originalImgWidth: Int,
  @SerialName("height")
  @XmlElement val originalImgHeight: Int,
  @SerialName("file_url")
  @XmlElement val originalImgUrl: String,
  @SerialName("sample_width")
  @XmlElement val sampleImgWidth: Int,
  @SerialName("sample_height")
  @XmlElement val sampleImgHeight: Int,
  @SerialName("sample_url")
  @XmlElement val sampleImgUrl: String,
  @SerialName("preview_width")
  @XmlElement val previewImgWidth: Int,
  @SerialName("preview_height")
  @XmlElement val previewImgHeight: Int,
  @SerialName("preview_url")
  @XmlElement val previewImgUrl: String,

  @XmlElement val directory: String,
  @XmlElement val image: String,
  @XmlElement val sample: Boolean,
  @XmlElement val title: String,
  @SerialName("post_locked")
  @XmlElement val postLocked: Boolean,
)

@XmlSerialName("rating")
enum class GelbooruPostRating {
  @SerialName("general")
  General,
  @SerialName("sensitive")
  Sensitive,
  @SerialName("questionable")
  Questionable,
  @SerialName("explicit")
  Explicit;
}

@Serializable
@XmlSerialName("tags")
data class TagsResponse(
  val limit: Int,
  val offset: Int,
  val count: Int,
  val tags: List<GelbooruTag>
)

@Serializable
@XmlSerialName("tag")
data class GelbooruTag(
  @XmlElement val id: Int,
  @XmlElement val name: String,
  @XmlElement val count: Int,
  @XmlElement val type: GelbooruTagType,
  @XmlElement val ambiguous: Boolean,
)

@Serializable
@XmlSerialName("type")
enum class GelbooruTagType {
  @SerialName("0")
  General,
  @SerialName("1")
  Artist,
  @SerialName("2")
  Unknown,
  @SerialName("3")
  Copyright,
  @SerialName("4")
  Character,
  @SerialName("5")
  Metadata,
  @SerialName("6")
  Deprecated,
}

@Serializable
data class GelbooruAutocompleteTag(
  val type: String,
  val label: String,
  val value: String,
  @SerialName("post_count")
  val postCount: String,
  val category: GelbooruAutocompleteTagCategory,
)

@Serializable
enum class GelbooruAutocompleteTagCategory() {
  @SerialName("tag")
  General,
  @SerialName("artist")
  Artist,
  @SerialName("copyright")
  Copyright,
  @SerialName("character")
  Character,
  @SerialName("metadata")
  Metadata,
}

//@Serializable
//data class GelbooruComment(
//  val id: Int,
//  /**
//   * String formatted like `yyyy-MM-dd HH:mm`
//   * (see [Java Date Format](https://docs.oracle.com/en/java/javase/21/docs/api//java.base/java/time/format/DateTimeFormatter.html#patterns))
//   * */
//  @SerialName("created_at")
//  val createdAt: String,
//  @SerialName("post_id")
//  val postId: Int,
//  val body: String,
//  @SerialName("creator")
//  val creatorName: String,
//  @SerialName("creator_id")
//  val creatorId: Int,
//)

@Serializable
@XmlSerialName("notes")
data class NotesResponse(
  val notes: List<GelbooruNote>
)

@Serializable
@XmlSerialName("note")
data class GelbooruNote(
  @XmlElement(false) val version: Int,
  @XmlElement(false) val id: Int,
  /** Associated post's ID */
  @SerialName("post_id")
  @XmlElement(false) val postId: Int,
  @XmlElement(false) val x: Int,
  @XmlElement(false) val y: Int,
  @XmlElement(false) val width: Int,
  @XmlElement(false) val height: Int,
  @XmlElement(false) val body: String,
  @SerialName("creator_id")
  @XmlElement(false) val authorId: Int,
  /**
   * String formatted like `EEE MMM dd HH:mm:ss XX yyyy`
   * (see [Java Date Format](https://docs.oracle.com/en/java/javase/21/docs/api//java.base/java/time/format/DateTimeFormatter.html#patterns))
   * */
  @SerialName("updated_at")
  @XmlElement(false) val updatedAt: String,
  /**
   * String formatted like `EEE MMM dd HH:mm:ss XX yyyy`
   * (see [Java Date Format](https://docs.oracle.com/en/java/javase/21/docs/api//java.base/java/time/format/DateTimeFormatter.html#patterns))
   * */
  @SerialName("created_at")
  @XmlElement(false) val createdAt: String,
  @SerialName("is_active")
  @XmlElement(false) val isActive: Boolean,
)