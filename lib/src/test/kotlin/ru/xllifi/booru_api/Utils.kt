package ru.xllifi.booru_api

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

fun MockRequestHandleScope.respondXml(content: String): HttpResponseData = respond(
  content,
  status = HttpStatusCode.OK,
  headers = headersOf("Content-Type" to listOf("text/xml"))
)