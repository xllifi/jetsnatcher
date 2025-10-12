package ru.xllifi.booru_api.rule34xxx

import ru.xllifi.booru_api.IntOrNullSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@XmlSerialName("post")
data class Rule34xxxPost(
  val score: Int,
  @Serializable(with = IntOrNullSerializer::class)
  @SerialName("parent_id")
  val parentId: Int?,
  @SerialName("rating")
  @XmlElement(false)
  val rating: Rule34xxxPostRating,
  val tags: String,
  val id: Int,
  /** UNIX epoch UTC timestamp */
  @SerialName("change")
  val changedAt: Long,
  val md5: String,
  @SerialName("creator_id")
  val authorId: Int,
  /**
   * String formatted like `EEE MMM dd HH:mm:ss XX yyyy`
   * (see [Java Date Format](https://docs.oracle.com/en/java/javase/21/docs/api//java.base/java/time/format/DateTimeFormatter.html#patterns))
   * */
  @SerialName("created_at")
  val createdAt: String,
  val status: String,
  val source: String?,
  @SerialName("has_children")
  val hasChildren: Boolean,
  @SerialName("has_notes")
  val hasNotes: Boolean,
  @SerialName("has_comments")
  val hasComments: Boolean,
  @SerialName("width")
  val originalImgWidth: Int,
  @SerialName("height")
  val originalImgHeight: Int,
  @SerialName("file_url")
  val originalImgUrl: String,
  @SerialName("sample_width")
  val sampleImgWidth: Int,
  @SerialName("sample_height")
  val sampleImgHeight: Int,
  @SerialName("sample_url")
  val sampleImgUrl: String,
  @SerialName("preview_width")
  val previewImgWidth: Int,
  @SerialName("preview_height")
  val previewImgHeight: Int,
  @SerialName("preview_url")
  val previewImgUrl: String,
)

@Serializable
@XmlSerialName("rating")
enum class Rule34xxxPostRating() {
  @SerialName("s")
  Safe,
  @SerialName("q")
  Questionable,
  @SerialName("e")
  Explicit;
}

@Serializable
@XmlSerialName("tag")
data class Rule34xxxTag(
  @XmlElement(false)
  val type: Rule34xxxTagType,
  val count: Int,
  val name: String,
  val ambiguous: Boolean,
  val id: Int,
)

@Serializable
@XmlSerialName("type")
enum class Rule34xxxTagType {
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
}

@Serializable
data class Rule34xxxAutocompleteTag(
  val label: String,
  val value: String,
)

@Serializable
data class Rule34xxxComment(
  val id: Int,
  /**
   * String formatted like `yyyy-MM-dd HH:mm`
   * (see [Java Date Format](https://docs.oracle.com/en/java/javase/21/docs/api//java.base/java/time/format/DateTimeFormatter.html#patterns))
   * */
  @SerialName("created_at")
  val createdAt: String,
  @SerialName("post_id")
  val postId: Int,
  val body: String,
  @SerialName("creator")
  val creatorName: String,
  @SerialName("creator_id")
  val creatorId: Int,
)

@Serializable
@XmlSerialName("notes")
data class NotesResponse(
  val notes: List<Rule34xxxNote>
)

@Serializable
@XmlSerialName("note")
data class Rule34xxxNote(
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