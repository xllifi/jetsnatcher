package ru.xllifi.jetsnatcher.ui.dialog

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.dialogsNavigation(
  backStack: NavBackStack<NavKey>,
) {
  providerEditDialogNavigation(backStack)
  listSelectDialogNavigation(backStack)
  confirmDialogNavigation(backStack)
  textFieldDialogNavigation(backStack)
}