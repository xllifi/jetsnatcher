package ru.xllifi.jetsnatcher.extensions

import android.text.format.DateUtils

fun timestampToRelativeTimeSpan(timestamp: Long): String {
  return DateUtils.getRelativeTimeSpanString(
    timestamp,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE
  ).toString()
}