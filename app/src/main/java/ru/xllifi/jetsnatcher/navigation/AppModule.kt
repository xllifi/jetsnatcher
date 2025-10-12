package ru.xllifi.jetsnatcher.navigation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.xllifi.jetsnatcher.navigation.screen.browser.BrowserViewModel

val appModule = module {
  viewModelOf(::BrowserViewModel)
}