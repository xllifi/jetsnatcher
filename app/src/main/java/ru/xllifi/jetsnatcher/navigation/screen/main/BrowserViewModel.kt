package ru.xllifi.jetsnatcher.navigation.screen.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Provider
import ru.xllifi.booru_api.Tag
import ru.xllifi.jetsnatcher.extensions.toReal
import ru.xllifi.jetsnatcher.proto.Settings

data class BrowserUiState(
  var posts: List<Post> = emptyList(),
  val page: Int = 0,

  val isLoadingNewPosts: Boolean = false,
  val noMorePosts: Boolean = false,

  val selectedPostIndex: Int = 0,
  val expandPost: Boolean = false,

  val loadPostsError: Exception? = null,
)

class BrowserViewModel(
  val settings: Settings,
  val provider: Provider,
  val searchTags: List<Tag>,
) : ViewModel() {
  private val _uiState = MutableStateFlow(BrowserUiState())
  val uiState = _uiState.asStateFlow()


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

    var error: Exception? = null
    val newPosts = try {
      provider.getPosts(
        tags = searchTags.map { it.value },
        limit = settings.pageSize,
        page = _uiState.value.page,
      )
    } catch (e: Exception) {
      error = e
      null
    } ?: emptyList()

    _uiState.update {
      it.copy(
        isLoadingNewPosts = false,
        posts = it.posts + newPosts,
        noMorePosts = newPosts.isEmpty(),
        loadPostsError = error,
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
        post.parseTags(provider)
      } else {
        post.tags
      },
      notes = if (post.hasNotes) {
        post.parseNotes(provider)
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
  private val settings: Settings,
  private val providerIndex: Int,
  private val searchTags: List<Tag>
) : ViewModelProvider.NewInstanceFactory() {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    val provider = settings.getProvider(providerIndex).toReal()
    @Suppress("UNCHECKED_CAST")
    return BrowserViewModel(settings, provider, searchTags) as T
  }
}