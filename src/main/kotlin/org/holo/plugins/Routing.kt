package org.holo.plugins

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.routing.*
import io.ktor.locations.*
import io.ktor.application.*
import io.ktor.response.*
import org.holo.game.engine.Engine
import org.holo.game.engine.Game
import org.holo.game.models.Deck
import java.util.UUID

val decks = mutableMapOf<Int, MutableList<Deck>>()
val games = mutableMapOf<String, Game>()
val gson = GsonBuilder().serializeNulls().create()

fun Application.configureRouting() {
    install(Locations) {
    }

    routing {
        get("/ping") {
            call.respondText("Pong")
        }
        get("/game/new") {
            val key = UUID.randomUUID()
            games[key.toString()] = Engine.newGame()
            call.respondText(key.toString())
        }

        get("/game") {
            val params= call.request.queryParameters
            val key = params["key"].toString()
            val game = games.getValue(key)

            call.respond(gson.toJson(game))
        }

        get("/game/shop/enter") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)
            val shopStage = game.enterShop()
            call.respond(gson.toJson(game))
        }


        get("/game/shop/buy") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)

            val from = call.request.queryParameters["from"]!!.toInt()
            val to = call.request.queryParameters["to"]!!.toInt()

            game.shopStage!!.buy(from, to)
            call.respond(gson.toJson(game))
        }

        get("/game/shop/move") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)

            val from = call.request.queryParameters["from"]!!.toInt()
            val to = call.request.queryParameters["to"]!!.toInt()

            game.shopStage!!.move(from, to)
            call.respond(gson.toJson(game))
        }

        get("/game/shop/equip") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)

            val item = call.request.queryParameters["item"]!!

            val at = when (item) {
                "chestPlate" -> 0
                "helmet" -> 1
                "chargeBlade" -> 2
                else -> error("invalid item")
            }
            game.shopStage!!.equip(at)
            call.respond(gson.toJson(game))
        }

        get("/game/shop/reroll") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)

            game.shopStage!!.reroll()
            call.respond(gson.toJson(game))
        }

        get("/game/shop/slay") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)
            val at = call.request.queryParameters["at"]!!.toInt()

            game.shopStage!!.slay(at)
            call.respond(gson.toJson(game))
        }

        get("/game/shop/exit") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)

            game.exitShop()
            if (decks[game.results.size] == null) decks[game.results.size] = mutableListOf()
            decks[game.results.size]!!.add(game.player.copyDeck())
            call.respond(gson.toJson(game))
        }

        get("/game/match/find") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)
            val opponent = decks[game.results.size]!!
                //.filter { it != game.player.deck }
                .random()

            game.enterBattle(opponent)
            call.respond(gson.toJson(game))
        }

        get("/game/match/enterTurn") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)
            game.battleStage!!.enterTurn()
            call.respond(gson.toJson(game))
        }

        get("/game/match/attack") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)
            game.battleStage!!.attack()
            call.respond(gson.toJson(game))
        }

        get("/game/match/endTurn") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)
            game.battleStage!!.enterTurn()
            call.respond(gson.toJson(game))
        }

        get("/game/match/endMatch") {
            val key = call.request.queryParameters["key"]!!
            val game = games.getValue(key)
            game.endBattle()
            call.respond(gson.toJson(game))
        }
    }
}
