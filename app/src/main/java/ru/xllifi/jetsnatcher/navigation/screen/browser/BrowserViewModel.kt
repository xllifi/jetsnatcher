package ru.xllifi.jetsnatcher.navigation.screen.browser

import android.content.Context
import android.util.Log
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Provider
import ru.xllifi.booru_api.Tag
import ru.xllifi.booru_api.TagCategory
import ru.xllifi.booru_api.gelbooru.Gelbooru
import ru.xllifi.jetsnatcher.posts
import ru.xllifi.jetsnatcher.proto.settingsDataStore

data class SearchData(
  var posts: List<Post> = emptyList(),
  var searchTags: List<Tag> = emptyList(),
  val provider: Provider,
  val page: Int = 0,
  val noMorePosts: Boolean = false,
)

data class BrowserUiState(
  val isLoadingNewPosts: Boolean = false,

  val searches: List<SearchData> = listOf(
    SearchData(provider = Gelbooru())
  ),

  val selectedPostIndex: Int = 0,
  val expandPost: Boolean = false,

  val postCardIntSizes: Map<Int, IntSize> = emptyMap(),
)

class BrowserViewModel : ViewModel() {
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

  fun newSearch(searchData: SearchData) {
    _uiState.update {
      it.copy(
        searches = it.searches + searchData
      )
    }
  }

  fun prevSearch() {
    _uiState.update {
      it.copy(
        searches = it.searches.dropLast(1)
      )
    }
  }

  suspend fun loadPosts(context: Context) {
    if (_uiState.value.isLoadingNewPosts) return
    Log.i("LOAD_POSTS", "Loading posts")
    _uiState.update {
      it.copy(
        isLoadingNewPosts = true,
      )
    }

    val settings = runBlocking {
      context.settingsDataStore.data.first()
    }
    val currentSearch = this.uiState.value.searches.last()

    val newPosts = try {
      currentSearch.provider.getPosts(
        tags = currentSearch.searchTags.map { it.value },
        limit = settings.pageSize,
        page = currentSearch.page
      )
    } catch (e: Exception) {
      Log.e("LOAD_POSTS", "Failed to load posts: $e")
      throw e
    } ?: listOf()

    _uiState.update {
      val noMorePosts = newPosts.isEmpty()
      val newSearches = it.searches.toMutableList()
      val index = newSearches.indexOf(currentSearch)
      if (index == -1) {
        throw IllegalArgumentException("Search no more present in searches stack!")
      }
      newSearches[index] =
        currentSearch.copy(
          posts = currentSearch.posts + newPosts,
          page = currentSearch.page + 1,
          noMorePosts = noMorePosts,
        )
      it.copy(
        isLoadingNewPosts = false,
        searches = newSearches,
      )
    }
  }


  suspend fun loadTagsAndNotes(postId: Int) {
    val newPost: Post = suspend {
      val search = _uiState.value.searches.last()
      val posts = search.posts
      val postIndex = posts.indexOfFirst { it.id == postId }
      if (postIndex == -1) {
        throw IllegalArgumentException("No post with id $postId found in last search's posts!")
      }
      val post = posts[postIndex]

      post.copy(
        tags = if (post.tags == null) {
          post.parseTags(search.provider)
        } else {
          post.tags
        },
        notes = if (post.hasNotes) {
          post.parseNotes(search.provider)
        } else null
      )
    }()

    _uiState.update {
      val newSearches = it.searches.toMutableList()
      newSearches[newSearches.lastIndex] =
        it.searches.last().copy(
          posts = it.searches.last().posts.map { post ->
            return@map if (post.id == postId) newPost else post
          }
        )
      it.copy(
        searches = newSearches
      )
    }
  }

  fun addIntSizeForPost(postId: Int, size: IntSize) {
    _uiState.update {
      val newVal = it.postCardIntSizes.toMutableMap()
      newVal[postId] = size
      it.copy(
        postCardIntSizes = newVal
      )
    }
  }
  // TODO: Add more precise methods for search tags
}