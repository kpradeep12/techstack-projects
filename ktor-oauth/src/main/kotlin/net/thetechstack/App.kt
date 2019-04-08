package net.thetechstack

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationStopping
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.origin
import io.ktor.html.respondHtml
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.header
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.param
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import kotlinx.html.*

fun main() {
    embeddedServer(Netty, 8080) {
        val authOauthForLogin = "authOauthForLogin"
        install(CallLogging)
        install(Sessions) {
            cookie<MySession>("ktorOAuthSessionId", SessionStorageMemory()) {
                cookie.path = "/"
            }
        }
        install(Authentication) {
            oauth(authOauthForLogin) {
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

                            // update below values with your keys
                            consumerKey = "***",
                            consumerSecret = "***"
                    )
                }
                urlProvider = {
                    redirectUrl("/login")
                }
            }
        }

        routing {
            get("/") {
                call.loginPage()
            }
            get("/main"){
                if(call.sessions.get<MySession>() == null)
                    call.respondRedirect("/")
                else {
                    call.loggedInSuccessResponse()
                }
            }
            get("/settings") {
                if(call.sessions.get<MySession>() == null)
                    call.respondRedirect("/")
                else {
                    call.accountPreferences()
                }
            }
            get("/logout") {
                call.sessions.clear<MySession>()
                call.response.header("Cache-Control", "no-cache, no-store, must-revalidate")
                call.respondRedirect("/")
            }
            authenticate(authOauthForLogin) {
                route("/login/{type?}") {
                    param("error") {
                        handle {
                            call.loginFailedPage(call.parameters.getAll("error").orEmpty())
                        }
                    }
                    handle {
                        val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth1a>()
                        if (principal != null) {
                            call.sessions.set(MySession(principal.extraParameters["screen_name"] ?: ""))
                            call.respondRedirect("/main")
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
            title { +"Login" }
        }
        body {
            h1 {
                +"Login"
            }
            hr{}
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
            title { +"Login" }
        }
        body {
            h1 {
                +"Login error"
            }
            hr{}
            for (e in errors) {
                p {
                    +e
                }
            }
        }
    }
}

private suspend fun ApplicationCall.loggedInSuccessResponse() {
    respondHtml {
        head {
            title { +"Home" }
        }
        body {
            h1 {
                +"Home"
            }
            hr{}
            p {
                +"Your screen name is ${sessions.get<MySession>()?.userId}"
            }
            p{
                a(href="/settings"){
                    +"Settings"
                }
                br {  }
                a(href="/logout"){
                    +"Logout"
                }
            }
        }
    }
}

private suspend fun ApplicationCall.accountPreferences() {
    respondHtml {
        head {
            title { +"Settings" }
        }
        body {
            h1{
                +"Settings"
            }
            hr{}
            p {
                +"Settings for ${sessions.get<MySession>()?.userId ?: ""}"
            }
            p{
                a(href="/main"){
                    +"Main"
                }
                br{}
                a(href="/logout"){
                    +"Logout"
                }
            }
        }
    }
}

class MySession(val userId: String)