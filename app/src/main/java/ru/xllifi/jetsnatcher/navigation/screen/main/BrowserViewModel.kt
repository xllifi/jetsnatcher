package ru.xllifi.jetsnatcher.navigation.screen.main

import android.content.Context
import android.util.Log
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Provider
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.gelbooru.Gelbooru
import ru.xllifi.jetsnatcher.extensions.toReal
import ru.xllifi.jetsnatcher.proto.settingsDataStore

data class BrowserUiState(
  var posts: List<Post> = emptyList(),
  val page: Int = 0,

  val isLoadingNewPosts: Boolean = false,
  val noMorePosts: Boolean = false,

  val selectedPostIndex: Int = 0,
  val expandPost: Boolean = false,
)

class BrowserViewModel(
  context: Context,
  val providerIndex: Int,
  val searchTags: List<Tag>
) : ViewModel() {
  private val _uiState = MutableStateFlow(BrowserUiState())
  val uiState = _uiState.asStateFlow()

  private val _settings = runBlocking {
    context.settingsDataStore.data.first()
  }
  private val _provider = _settings.getProvider(providerIndex).toReal()

  fun selectPost(index: Int) {
    _uiState.update {
      it.copy(
        selectedPostIndex = index
      )
    }
  }

  fun expandPost(to: Boolean = true) {
    _uiState.update {
      it.copy(
        expandPost = to
      )
    }
  }

  suspend fun loadPosts() {
    if (_uiState.value.isLoadingNewPosts) return
    Log.i("LOAD_POSTS", "Loading posts")
    _uiState.update {
      it.copy(
        isLoadingNewPosts = true,
      )
    }

    Log.i(
      "LOAD_POSTS",
      "Loading with provider: $_provider (proto ${_settings.getProvider(providerIndex)})"
    )
    val newPosts = _provider.getPosts(
      tags = searchTags.map { it.value },
      limit = _settings.pageSize,
      page = _uiState.value.page,
    ) ?: emptyList()

    _uiState.update {
      it.copy(
        isLoadingNewPosts = false,
        posts = it.posts + newPosts,
        noMorePosts = newPosts.isEmpty(),
      )
    }
  }

  suspend fun loadTagsAndNotes(postId: Int) {
    val posts = _uiState.value.posts
    Log.i(null, "Loading info for postid $postId of posts $posts")
    val index = posts.indexOfFirst { it.id == postId }
    if (index == -1) {
      throw UnsupportedOperationException("No post with id $postId found!")
    }
    val post = posts[index]

    val newPost = post.copy(
      tags = if (post.tags == null) {
        post.parseTags(_provider)
      } else {
        post.tags
      },
      notes = if (post.hasNotes) {
        post.parseNotes(_provider)
      } else null
    )

    _uiState.update {
      val posts = it.posts.toMutableList()
      val index = posts.indexOfFirst { post -> post.id == postId }
      if (index == -1) {
        throw UnsupportedOperationException("No post with id $postId found in uiState after networking!")
      }
      posts[index] = newPost
      it.copy(
        posts = posts
      )
    }
  }
}

class BrowserViewModelFactory(
  private val context: Context,
  private val providerIndex: Int,
  private val searchTags: List<Tag>
) : ViewModelProvider.NewInstanceFactory() {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T = BrowserViewModel(context, providerIndex, searchTags) as T
}