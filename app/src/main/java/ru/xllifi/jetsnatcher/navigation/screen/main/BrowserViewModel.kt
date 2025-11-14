package ru.xllifi.jetsnatcher.navigation.screen.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Tag
import ru.xllifi.jetsnatcher.extensions.toReal
import ru.xllifi.jetsnatcher.proto.settings.ProviderProto
import ru.xllifi.jetsnatcher.proto.settingsDataStore
import ru.xllifi.jetsnatcher.samplePosts

data class BrowserUiState(
  var posts: List<Post> = emptyList(),
  val page: Int = 0,

  val isLoadingNewPosts: Boolean = false,
  val noMorePosts: Boolean = false,

  val selectedPostIndex: Int = 0,
  val expandPost: Boolean = false,

  val loadPostsError: Exception? = null,
  val loadPostMetaErrors: Map<Int, LoadPostMetaErrors> = emptyMap(),
)

data class LoadPostMetaErrors(
  val tags: Exception? = null,
  val notes: Exception? = null,
) {
  fun isEmpty(): Boolean {
    return tags == null && notes == null
  }
}

class BrowserViewModel(
  context: Context,
  providerProto: ProviderProto,
  val searchTags: List<Tag>,
  loadPreviewPosts: Boolean = false,
) : ViewModel() {
  private val _uiState = MutableStateFlow(
    BrowserUiState(
      posts = if (loadPreviewPosts) samplePosts else emptyList()
    )
  )
  val uiState = _uiState.asStateFlow()

  private val settingsDataStore = context.settingsDataStore
  val provider = providerProto.toReal()

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

  suspend fun refresh() {
    _uiState.update {
      BrowserUiState()
    }
    loadPosts()
  }

  suspend fun loadPosts(evenIfNoMore: Boolean = false) {
    if (_uiState.value.isLoadingNewPosts) return
    if (_uiState.value.noMorePosts && !evenIfNoMore) return
    Log.i("LOAD_POSTS", "Loading posts")
    _uiState.update {
      it.copy(
        isLoadingNewPosts = true,
        loadPostsError = null,
        noMorePosts = false,
      )
    }

    var error: Exception? = null
    val newPosts = try {
      provider.getPosts(
        tags = searchTags.map { it.value } + settingsDataStore.data.first().blacklistedTags.map { "-${it.value}" },
        limit = settingsDataStore.data.first().pageSize.toInt(),
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
        page = it.page + 1,
        noMorePosts = newPosts.isEmpty(),
        loadPostsError = error,
      )
    }
  }

  suspend fun loadPostMeta(postIndex: Int) {
    _uiState.update {
      val loadPostMetaErrors = it.loadPostMetaErrors.toMutableMap()
      loadPostMetaErrors.remove(postIndex)
      it.copy(
        loadPostMetaErrors = loadPostMetaErrors
      )
    }
    val post = _uiState.value.posts[postIndex]
    var errors = LoadPostMetaErrors()
    val newPost = post.copy(
      tags = if (post.tags == null) {
        try {
          post.parseTags(provider)
        } catch (e: Exception) {
          errors = errors.copy(
            tags = e
          )
          null
        }
      } else {
        post.tags
      },
      notes = if (post.hasNotes) {
        try {
          post.parseNotes(provider)
        } catch (e: Exception) {
          errors = errors.copy(
            notes = e
          )
          null
        }
      } else null
    )

    _uiState.update {
      val posts = it.posts.toMutableList()
      posts[postIndex] = newPost
      val loadPostMetaErrors = it.loadPostMetaErrors.toMutableMap()
      loadPostMetaErrors[postIndex] = errors
      it.copy(
        posts = posts,
        loadPostMetaErrors = loadPostMetaErrors
      )
    }
  }
}

class BrowserViewModelFactory(
  private val context: Context,
  private val providerProto: ProviderProto,
  private val searchTags: List<Tag>
) : ViewModelProvider.NewInstanceFactory() {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    @Suppress("UNCHECKED_CAST")
    return BrowserViewModel(context, providerProto, searchTags) as T
  }
}