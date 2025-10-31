package ru.xllifi.jetsnatcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.DeDupeInFlightRequestStrategy
import coil3.network.NetworkFetcher
import coil3.network.okhttp.asNetworkClient
import com.jobinlawrance.downloadprogressinterceptor.DownloadProgressInterceptor
import com.jobinlawrance.downloadprogressinterceptor.ProgressEventBus
import okhttp3.OkHttpClient
import ru.xllifi.booru_api.Image
import ru.xllifi.booru_api.Post
import ru.xllifi.booru_api.Rating
import ru.xllifi.jetsnatcher.navigation.NavRoot
import ru.xllifi.jetsnatcher.ui.theme.JetSnatcherTheme

val samplePosts = listOf(
  Post(
    id = 0,
    worstQualityImage = Image(
      height = 350,
      width = 293,
      url = "https://gelbooru.com/thumbnails/3b/fe/thumbnail_3bfeb701bcf27f06325d54fcee62442d.jpg"
    ),
    mediumQualityImage = Image(
      height = 1014,
      width = 850,
      url = "https://img4.gelbooru.com/samples/3b/fe/sample_3bfeb701bcf27f06325d54fcee62442d.jpg"
    ),
    rating = Rating.General,
    tags = null,
    unparsedTags = listOf("2girls", "arms_around_neck", "black_jacket", "blazer", "blush", "brown_eyes", "brown_hair", "closed_mouth", "commentary", "grey_skirt", "heart", "highres", "hirasawa_yui", "hug", "jacket", "k-on!", "multiple_girls", "pleated_skirt", "sakuragaoka_high_school_uniform", "school_uniform", "shaofangziran", "shirt", "short_hair", "simple_background", "skirt", "symbol-only_commentary", "tainaka_ritsu", "translated", "white_background", "white_shirt", "winter_uniform", "yuri"),
    score = 0,
    bestQualityImage = Image(
      height = 1014,
      width = 850,
      url = "https://img4.gelbooru.com/samples/3b/fe/sample_3bfeb701bcf27f06325d54fcee62442d.jpg"
    ),
    authorName = "xllifi",
    authorId = 0,
    changedAt = 0,
    createdAt = 0,
    hasNotes = false,
    hasComments = false,
    hasChildren = false,
    source = "https://twitter.com/yuiui_keion/status/1926605675938873496",
    locationLink = "https://gelbooru.com/index.php?page=post&s=view&id=12660001",
  ),
  Post(
    id = 1,
    worstQualityImage = Image(
      height = 350,
      width = 247,
      url = "https://gelbooru.com/thumbnails/8a/00/thumbnail_8a00de4b61283b3e0566fece8fc76f35.jpg"
    ),
    mediumQualityImage = Image(
      height = 1202,
      width = 850,
      url = "https://img4.gelbooru.com/samples/8a/00/sample_8a00de4b61283b3e0566fece8fc76f35.jpg"
    ),
    rating = Rating.General,
    tags = null,
    unparsedTags = listOf(),
    score = 0,
    bestQualityImage = Image(
      height = 1014,
      width = 850,
      url = "https://img4.gelbooru.com/samples/3b/fe/sample_3bfeb701bcf27f06325d54fcee62442d.jpg"
    ),
    authorName = "xllifi",
    authorId = 0,
    changedAt = 0,
    createdAt = 0,
    hasNotes = true,
    hasComments = false,
    hasChildren = false,
  ),
  Post(
    id = 12660001,
    worstQualityImage = Image(
      height = 350,
      width = 262,
      url = "https://gelbooru.com/thumbnails/aa/d1/thumbnail_aad133eb490b80015bb2efe812c5ac3f.jpg"
    ),
    mediumQualityImage = Image(
      height = 1134,
      width = 850,
      url = "https://img4.gelbooru.com/samples/aa/d1/sample_aad133eb490b80015bb2efe812c5ac3f.jpg"
    ),
    rating = Rating.General,
    tags = null,
    unparsedTags = listOf("2girls", "arms_around_neck", "black_jacket", "blazer", "blush", "brown_eyes", "brown_hair", "closed_mouth", "commentary", "grey_skirt", "heart", "highres", "hirasawa_yui", "hug", "jacket", "k-on!", "multiple_girls", "pleated_skirt", "sakuragaoka_high_school_uniform", "school_uniform", "shaofangziran", "shirt", "short_hair", "simple_background", "skirt", "symbol-only_commentary", "tainaka_ritsu", "translated", "white_background", "white_shirt", "winter_uniform", "yuri"),
    score = 0,
    bestQualityImage = Image(
      height = 2187,
      width = 1640,
      url = "https://img4.gelbooru.com/images/aa/d1/aad133eb490b80015bb2efe812c5ac3f.jpg"
    ),
    authorName = "xllifi",
    authorId = 0,
    changedAt = 0,
    createdAt = 0,
    hasNotes = true,
    hasComments = false,
    hasChildren = false,
    source = "https://www.pixiv.net/artworks/135292885"
  )
)

val progressEventBus: ProgressEventBus = ProgressEventBus()

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
          .memoryCache {
            MemoryCache.Builder()
              .maxSizePercent(context, 0.15)
              .build()
          }
          .diskCache {
            DiskCache.Builder()
              .directory(context.cacheDir.resolve("image_cache"))
              .maxSizePercent(0.02)
              .build()
          }
          .components {
            add(
              NetworkFetcher.Factory(
                networkClient = {
                  OkHttpClient.Builder()
                    .addNetworkInterceptor(DownloadProgressInterceptor(progressEventBus))
                    .build().asNetworkClient()
                },
                inFlightRequestStrategy = { DeDupeInFlightRequestStrategy() },
              )
            )
          }
          .build()
      }
      
      JetSnatcherTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          NavRoot(innerPadding)
        }
      }
    }
  }
}