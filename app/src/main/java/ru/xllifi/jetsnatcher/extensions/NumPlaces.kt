package ru.xllifi.jetsnatcher.extensions

fun Float.numPlaces(): Int {
  var n = this
  var r = 1
  if (n < 0) {
    n = -n
  }
  while (n > 9) {
    n /= 10
    r++
  }
  return r
}

fun Double.numPlaces(): Int {
  var n = this
  var r = 1
  if (n < 0) {
    n = -n
  }
  while (n > 9) {
    n /= 10
    r++
  }
  return r
}