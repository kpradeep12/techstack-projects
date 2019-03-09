package net.thetechstack

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.engine.apache.*
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.origin
import io.ktor.html.respondHtml
import io.ktor.http.HttpMethod
import io.ktor.locations.*
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*
import java.util.concurrent.Executors

@Location("/") class index()
@Location("/login/{type?}") class login(val type: String = "")

fun main() {
    embeddedServer(Netty, 8080) {
        val authOauthForLogin = "authOauthForLogin"
        install(DefaultHeaders)
        install(CallLogging)
        install(Authentication) {
            oauth("authOauthForLogin") {
                client = HttpClient(Apache).apply {
                    environment.monitor.subscribe(ApplicationStopping) {
                        close()
                    }
                }
                providerLookup = {
                    OAuthServerSettings.OAuth1aServerSettings(
                            name = "twitter",
                            requestTokenUrl = "https://api.twitter.com/oauth/request_token",
                            authorizeUrl = "https://api.twitter.com/oauth/authorize",
                            accessTokenUrl = "https://api.twitter.com/oauth/access_token",

                            consumerKey = "rydpe5eCuBjkHLDDqvdzY2psr",
                            consumerSecret = "KeBHtKqTquhwZJgGBiZRfO2kA1MmTpav7ZEQ7gwBfO9NDpbPD1"
                    )
                }
                urlProvider = { p ->
                    //redirectUrl(login(p.name), false)
                    redirectUrl("/login")
                }
            }
        }

        routing {
            route("/") {
                get {
                    call.loginPage()
                }
            }

            authenticate(authOauthForLogin) {
                route("/login/{type?}") {
                    param("error") {
                        handle {
                            call.loginFailedPage(call.parameters.getAll("error").orEmpty())
                        }
                    }

                    handle {
                        val principal = call.authentication.principal<OAuthAccessTokenResponse>()
                        if (principal != null) {
                            call.loggedInSuccessResponse(principal)
                        } else {
                            call.loginPage()
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}

private fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") 80 else 443
    val hostPort = request.host()!! + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}

private suspend fun ApplicationCall.loginPage() {
    respondHtml {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login with:"
            }
            p {
                a(href = "/login/twitter") {
                    +"twitter"
                }
            }
        }
    }
}

private suspend fun ApplicationCall.loginFailedPage(errors: List<String>) {
    respondHtml {
        head {
            title { +"Login with" }
        }
        body {
            h1 {
                +"Login error"
            }

            for (e in errors) {
                p {
                    +e
                }
            }
        }
    }
}

private suspend fun ApplicationCall.loggedInSuccessResponse(callback: OAuthAccessTokenResponse) {
    respondHtml {
        head {
            title { +"Logged in" }
        }
        body {
            h1 {
                +"You are logged in"
            }
            p {
                +"Your token is $callback"
            }
        }
    }
}
