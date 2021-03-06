/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package net.thetechstack

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Movie {
    var title: String? = ""
    var directedBy: String = ""
    var producedBy: String = ""
    var writtenBy: String = ""
    var starring: String = ""
    var musicBy: String = ""
    var releaseDate: String = ""
    //var posterURL: String = ""

    override fun toString(): String {
        return "Movie(title='$title')"
    }

}

val wiki = "https://en.wikipedia.org"

fun main() {
    val startTime = System.currentTimeMillis()
    val doc = Jsoup.connect("$wiki/wiki/List_of_films_with_a_100%25_rating_on_Rotten_Tomatoes").get()
    doc.select(".wikitable:first-of-type tr td:first-of-type a")
            .map { col -> col.attr("href") }
            .parallelStream()
            .map { extractMovieData(it) }
            .filter { it != null }
            .forEach { println(it) }

    println("${(System.currentTimeMillis() - startTime) / 1000} seconds")
}

fun extractMovieData(url: String): Movie? {
    val doc: Document
    try {
        doc = Jsoup.connect("$wiki$url").get()
    }catch (e: Exception){
        return null
    }

    val movie = Movie()
    doc.select(".infobox tr")
            .forEach { ele ->
                when {
                    ele.getElementsByTag("th")?.hasClass("summary") ?: false -> {
                        movie.title = ele.getElementsByTag("th")?.text()
                    }
                    /*ele.getElementsByTag("img").isNotEmpty() -> {
                        movie.posterURL = "https:" + ele.getElementsByTag("img").attr("src")
                    }*/
                    else -> {
                        val value: String? = if (ele.getElementsByTag("li").size > 1)
                            ele.getElementsByTag("li").map(Element::text).filter(String::isNotEmpty).joinToString(", ") else
                            ele.getElementsByTag("td")?.first()?.text()

                        when (ele.getElementsByTag("th")?.first()?.text()) {
                            "Directed by" -> movie.directedBy = value ?: ""
                            "Produced by" -> movie.producedBy = value ?: ""
                            "Written by" -> movie.writtenBy = value ?: ""
                            "Starring" -> movie.starring = value ?: ""
                            "Music by" -> movie.musicBy = value ?: ""
                            "Release date" -> movie.releaseDate = value ?: ""
                            //"poster URL" -> movie.posterURL = value ?: ""
                            "title" -> movie.title = value ?: ""
                        }
                    }
                }
            }
    return movie
}