package org.holo.game.engine

import org.holo.game.models.*
import kotlin.random.Random

class Engine {
    companion object {
        fun newGame() = newGame(Player.new())

        fun newGame(player: Player): Game = Game(player)
    }
}

class Game(val player: Player) {
    val results: MutableList<BattleStage.Result> = mutableListOf()
    var shopStage: ShopStage? = null
    var battleStage: BattleStage? = null

    fun enterShop() : ShopStage {
        player.money += 10
        player.deck.hunter.enterShop(player)
        player.deck.members.filterNotNull()
            .forEach { it.enterShop(player) }
        this.shopStage = ShopStage(player, listOf())
        return this.shopStage!!
    }

    fun exitShop() {
        player.deck.hunter.exitShop(player)
        player.deck.members.filterNotNull()
            .forEach { it.exitShop(player) }
        shopStage = null
        player.money = 0
    }

    fun enterBattle(opponent: Deck) {

        if (shopStage != null) {
            throw IllegalStateException("Cannot enter battle while in shop stage")
        }
        val playerDeck = player.copyDeck()
        val opponentDeck = opponent.copy(
            hunter = opponent.hunter.copyHunter(),
            members = opponent.members.map { it?.copyMonster() }.toMutableList()
        )

        battleStage = BattleStage(player, playerDeck, opponentDeck)
    }

    fun endBattle() {
        battleStage.takeUnless { it!!.status == BattleStage.Result.IN_PROGRESS }
            ?: throw IllegalStateException("Battle has not ended")
        results.add(battleStage!!.status)
        player.deck.members.filterNotNull()
            .forEach {
                it.endBattle(battleStage!!.player)
            }
    }
}

class BattleStage(val player: Player, val playerDeck: Deck, val otherDeck: Deck) {
    data class GroupedMember(val t1: Member?, val t2: Player?, val t3: Deck, val t5: Deck)

    var status = Result.IN_PROGRESS

    // MyDeck
    private var mDeck = listOf(GroupedMember(playerDeck.hunter, player, playerDeck, otherDeck)) +
            playerDeck.members.map { GroupedMember(it, player, playerDeck, otherDeck) }

    // EnemyDeck
    private var eDeck = listOf(GroupedMember(otherDeck.hunter, null, otherDeck, playerDeck)) +
            otherDeck.members.map { GroupedMember(it, null, otherDeck, playerDeck) }

    init {
        (mDeck + eDeck).filter { it.t1 != null }
            // order of actions is damage + health + random tiebreaker
            .sortedBy { it.t1!!.damage + it.t1.health + Random.nextFloat() }
            .forEach { it.t1?.enterBattle(it.t2, it.t3, it.t5) }
        mDeck = mDeck.map { if (it.t1 == null || it.t1.health <= 0) it.copy(t1 = null) else it }
        eDeck = eDeck.map { if (it.t1 == null || it.t1.health <= 0) it.copy(t1 = null) else it }
    }

    fun enterTurn() {
        if (checkEnd()) return

        status.takeIf { it == Result.IN_PROGRESS } ?: throw IllegalStateException("Battle has ended")
        (mDeck + eDeck).filter { it.t1 != null }
            // order of actions is damage + health + random tiebreaker
            .sortedBy { it.t1!!.damage + it.t1.health + Random.nextFloat() }
            .forEach { it.t1?.enterTurn(player, playerDeck, otherDeck) }
        mDeck = mDeck.map { if (it.t1 == null || it.t1.health <= 0) it.copy(t1 = null) else it }
        eDeck = eDeck.map { if (it.t1 == null || it.t1.health <= 0) it.copy(t1 = null) else it }
    }

    fun attack() {
        if (checkEnd()) return

        val mine = mDeck.lastOrNull { it.t1 != null }
        val their = eDeck.lastOrNull { it.t1 != null }

        listOf(Pair(mine, their!!.t1!!), Pair(their, mine!!.t1!!))
            .sortedBy { Random.nextFloat() }
            .forEach { it.first!!.t1?.attack(it.first!!.t2, it.first!!.t3, it.first!!.t5, it.second) }

        mDeck = mDeck.map { if (it.t1 == null || it.t1.health <= 0) it.copy(t1 = null) else it }
        eDeck = eDeck.map { if (it.t1 == null || it.t1.health <= 0) it.copy(t1 = null) else it }
        return
    }

    fun endTurn() {
        if (checkEnd()) return

        status.takeIf { it == Result.IN_PROGRESS } ?: throw IllegalStateException("Battle has ended")

        (mDeck + eDeck).filter { it.t1 != null }
            // order of actions is damage + health + random tiebreaker
            .sortedBy { it.t1!!.damage + it.t1.health + Random.nextFloat() }
            .forEach { it.t1?.endTurn(player, playerDeck, otherDeck) }
        mDeck = mDeck.map { if (it.t1 == null || it.t1.health <= 0) it.copy(t1 = null) else it }
        eDeck = eDeck.map { if (it.t1 == null || it.t1.health <= 0) it.copy(t1 = null) else it }
    }

    private fun checkEnd(): Boolean {
        if (status != Result.IN_PROGRESS) {
            return true
        }
        if (mDeck.first().t1 == null || eDeck.first().t1 == null) {
            status =
                if (eDeck.first().t1 != null) {
                    Result.LOSER
                } else if (mDeck.first().t1 != null) {
                    Result.WINNER
                } else {
                    Result.TIE
                }
            return true
        }
        return false
    }

    enum class Result {
        IN_PROGRESS, WINNER, LOSER, TIE
    }
}

class ShopStage(val player: Player, modifiers: List<Modifier>, var shop: Shop = Shop.randomShop()) {
    fun reroll() {
        shop = Shop.randomShop()
        player.money -= 1
    }

    fun slay(pos: Int) {
        (pos < player.deck.members.size || player.deck.members[pos] == null).takeIf { it }
            ?: throw IllegalArgumentException("deck item doesn't exists")
        val monster = player.deck.members[pos]!!
        player.deck.members[pos] = null
        monster.sell(player)
        when (Random.nextInt(3)) {
            0 -> {
                shop.chestPlate = monster.chestPlate
                shop.helmet = monster.helmet
            }
            1 -> {
                shop.helmet = monster.helmet
                shop.chargeBlade = monster.chargeBlade
            }
            2 -> {
                shop.chestPlate = monster.chestPlate
                shop.chargeBlade = monster.chargeBlade
            }
            else -> error("Unreachable code")
        }
    }

    fun equip(at: Int): Unit {
        (at in (0 until 3)).takeIf { it }
            ?: throw IllegalArgumentException("Item to equip must be between 0 and 2")
        ((at == 0 && shop.chestPlate == null)
                || (at == 1 && shop.helmet == null)
                || (at == 2 && shop.chargeBlade == null))
            .takeUnless { it } ?: throw IllegalArgumentException("Item must be available to equip it")

        when (at) {
            0 -> {
                if (player.deck.hunter.chestPlate != null) {
                    player.deck.hunter.chestPlate!!.unequip(player)
                }
                player.deck.hunter.chestPlate = shop.chestPlate
                player.deck.hunter.chestPlate!!.equip(player)
            }
            1 -> {
                if (player.deck.hunter.helmet != null) {
                    player.deck.hunter.helmet!!.unequip(player)
                }
                player.deck.hunter.helmet = shop.helmet
                player.deck.hunter.helmet!!.equip(player)
            }
            2 -> {
                if (player.deck.hunter.chargeBlade != null) {
                    player.deck.hunter.chargeBlade!!.unequip(player)
                }
                player.deck.hunter.chargeBlade = shop.chargeBlade
                player.deck.hunter.chargeBlade!!.equip(player)
            }
            else -> error("Unreachable code")
        }
        shop.chestPlate = null
        shop.helmet = null
        shop.chargeBlade = null

    }

    fun buy(at: Int, to: Int) {
        (at < shop.content.size).takeIf { it } ?: throw IllegalArgumentException("shop item doesn't exists")
        (to < player.deck.members.size).takeIf { it } ?: throw IllegalArgumentException("deck item doesn't exists")
        val wish = shop.content[at]

        (player.deck.members[to] == null).takeIf { it || wish::class == player.deck.members[to]!!::class }
            ?: throw IllegalArgumentException("position occupied by a different member")

        wish.buy(player, wish, to)
        shop.content.removeAt(at)

        return
    }

    fun move(from: Int, to: Int): Unit {
        (from < player.deck.members.size).takeIf { it }
            ?: throw IllegalArgumentException("deck item doesn't exists $from")
        (to < player.deck.members.size).takeIf { it } ?: throw IllegalArgumentException("deck item doesn't exists $to")
        (player.deck.members[from] != null).takeIf { it } ?: throw IllegalArgumentException("no member exist at $from")

        (player.deck.members[to] == null).takeIf { it || player.deck.members[from]!!::class == player.deck.members[to]!!::class }
            ?: throw IllegalArgumentException("position occupied by a different member of a different type")

        if (player.deck.members[to] == null) {
            player.deck.members[to] = player.deck.members[from]
            player.deck.members[from] = null
            return
        }
        player.deck.members[to]!!.combine(player, player.deck.members[from]!!)
        player.deck.members[from] = null
    }
}

fun printPlayer(x: Player): Unit {
    println("You have: ${x.money} gold")
    println("Your deck is:")
    println("- ${x.deck.hunter}")
    println("\tchestPlate: ${x.deck.hunter.chestPlate?.javaClass?.simpleName ?: "none"}")
    println("\tchargeBlade: ${x.deck.hunter.chargeBlade?.javaClass?.simpleName ?: "none"}")
    println("\thelmet: ${x.deck.hunter.helmet?.javaClass?.simpleName ?: "none"}")
    x.deck.members.forEachIndexed { index, member ->
        println("${index + 1}. ${member.takeIf { it != null } ?: "none"}")
    }
}

fun printShopt(shop: Shop): Unit {
    println("Shop:")
    shop.content
        .forEachIndexed { i, it ->
            println("${i + 1}. $it")
        }
    println("Items:")
    println("1. ChestPlate: ${shop.chestPlate?.javaClass?.simpleName ?: "none"}")
    println("2. Helmet: ${shop.helmet?.javaClass?.simpleName ?: "none"}")
    println("3. ChargeBlade: ${shop.chargeBlade?.javaClass?.simpleName ?: "none"}")
}


fun main() {
    println("Starting building player 1")
    val game = Engine.newGame()
    val player = game.player
    game.enterShop()
    playShop(game, player)
    game.exitShop()

    printPlayer(player)

    println("===========================")
    println("Starting building player 2")
    val game2 = Engine.newGame()
    val player2 = game2.player
    game2.enterShop()
    playShop(game2, player2)
    game2.exitShop()

    printPlayer(player2)

    game.enterBattle(player2.deck)
    val battle = game.battleStage!!
    while (battle.status == BattleStage.Result.IN_PROGRESS) {
        battle.enterTurn()
        battle.attack()
        battle.endTurn()
    }
    println(battle.status)
    game.endBattle()
}

fun playShop(game: Game, player: Player) {
    do {
        printPlayer(player)
        println()
        printShopt(game.shopStage!!.shop)
        println()
        println("what you want to do: (b) buy (r) reroll (s) slay (m) move (e) equip (w) exit shop")

        val action = readln()
        when (action) {
            "b" -> {
                println("which animal you want to buy")
                val from = readln().toInt() - 1
                println("to what position you want it to go")
                val to = readln().toInt() - 1

                game.shopStage!!.buy(from, to)
            }
            "r" -> game.shopStage!!.reroll()
            "s" -> {
                println("which member you want to slay")
                val at = readln().toInt() - 1
                game.shopStage!!.slay(at)
            }
            "m" -> {
                println("which member you want to move")
                val from = readln().toInt() - 1
                println("to where you want to move it")
                val to = readln().toInt() - 1
                game.shopStage!!.move(from, to)
            }
            "e" -> {
                if (game.shopStage!!.shop.chestPlate == null &&
                    game.shopStage!!.shop.helmet == null &&
                    game.shopStage!!.shop.chargeBlade == null
                ) {
                    continue
                }
                println("which item you want to equip?")
                val item = readln().toInt() - 1
                game.shopStage!!.equip(item)
            }
            "w" -> {}
            else -> {
                println("action doesn't exists")
            }
        }
    } while (action != "w")
}