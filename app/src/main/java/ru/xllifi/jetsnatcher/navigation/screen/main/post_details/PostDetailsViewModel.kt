package ru.xllifi.jetsnatcher.navigation.screen.main.post_details

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.xllifi.booru_api.Tag

data class PostDetailsUiState(
  val selectedTags: List<Tag> = emptyList()
)

class PostDetailsViewModel : ViewModel() {
  private val _uiState = MutableStateFlow(PostDetailsUiState())
  val uiState = _uiState.asStateFlow()

  fun addTag(tag: Tag) {
    if (_uiState.value.selectedTags.contains(tag)) return
    _uiState.update {
      val selectedTags = it.selectedTags.toMutableList()
      selectedTags.add(tag)
      it.copy(
        selectedTags = selectedTags
      )
    }
  }
  fun removeTag(tag: Tag) {
    _uiState.update {
      val selectedTags = it.selectedTags.toMutableList()
      selectedTags.removeAll { it == tag }
      it.copy(
        selectedTags = selectedTags
      )
    }
  }
}