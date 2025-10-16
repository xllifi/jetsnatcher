package ru.xllifi.jetsnatcher.navigation

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.xllifi.jetsnatcher.navigation.screen.main.BrowserViewModel

val appModule = module {
  viewModelOf(::BrowserViewModel)
}