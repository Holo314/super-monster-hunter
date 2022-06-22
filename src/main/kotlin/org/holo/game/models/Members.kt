package org.holo.game.models

import org.holo.game.models.monsters.*
import java.util.*

interface Member {
    val tier: Int
    val name: String
    val price: Int
    var maxHealth: Int
    var health: Int
    var damage: Int
    var level: Int
    var quantity: Int
    val maxLevel: Int
    val statuses: MutableList<Status>
    var critChance: Int
    var stunResistance: Int

    fun enterShop(me: Player) {}

    fun exitShop(me: Player) {}
    fun enterBattle(me: Player?, myDeck: Deck, otherDeck: Deck) {}
    fun enterTurn(me: Player?, myDeck: Deck, otherDeck: Deck) {
        if (this.statuses.filterIsInstance<BlastBlight>().isNotEmpty()) {
            this.statuses.filterIsInstance<BlastBlight>().forEach {
                this.damaged(me, myDeck, otherDeck, it.dealer, it.damage)
            }
            BlastBlight.deal(this.statuses)
        }
    }

    fun attack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        val act = {
            var stunned = false
            if (this.statuses.filterIsInstance<Stunned>().isNotEmpty()) {
                Stunned.deal(this.statuses)
                stunned = Random().nextInt(100) < stunResistance
            }
            if (!stunned) {
                target.damaged(null, otherDeck, myDeck, this, this.damage)
                if (target.health == 0) {
                    if (target is Monster) {
                        otherDeck.members.remove(target)
                    }
                }
            }
        }

        if (Random().nextInt(100) < critChance) {
            val rd = damage
            damage = (damage * 1.5).toInt()
            act.invoke()
            damage = rd
        } else {
            act.invoke()
        }
    }

    fun damaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member, dmg: Int) {
        this.health = (this.health - dmg).coerceAtLeast(0)
        if (this.health == 0) {
            death(me, myDeck, otherDeck, by)
        }
    }

    fun setStatus(me: Player?, myDeck: Deck, otherDeck: Deck, status: Status) {
        this.statuses.add(status)
    }

    fun death(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}

    fun endTurn(me: Player?, myDeck: Deck, otherDeck: Deck) {}
    fun endBattle(me: Player) {}


    sealed interface Status {
        val dealer: Member
    }

    data class Stunned(val turns: Int, override val dealer: Member) : Status {
        companion object {
            fun deal(statuses: MutableList<Status>) {
                statuses.replaceAll {
                    if (it is Stunned) {
                        Stunned(it.turns - 1, it.dealer)
                    } else {
                        it
                    }
                }
                statuses.filter { !(it is Stunned && it.turns <= 0) }
            }
        }
    }

    data class BlastBlight(val damage: Int, override val dealer: Member) : Status {
        companion object {
            fun deal(statuses: MutableList<Status>) {
                statuses.filterNot { it is BlastBlight }
            }
        }

    }
}

interface Monster : Member {

    fun buy(me: Player, wish: Monster, to: Int) {
        (me.money >= wish.price).takeIf { it } ?: throw IllegalArgumentException("not enough money")
        if (me.deck.members[to] == null) {
            me.deck.members[to] = wish
            me.money = me.money - wish.price
            return
        }


        me.deck.members[to]!!.combine(me, wish)
        me.money = me.money - wish.price
    }

    fun combine(me: Player, member: Monster) {
        (this.level < this.maxLevel).takeIf { it }
            ?: throw IllegalArgumentException("member is already at max level")

        (member.level < member.maxLevel).takeIf { it }
            ?: throw IllegalArgumentException("member is already at max level")


        this.quantity += member.quantity
        if ((this.level == 1 && this.quantity == 3) || (this.level == 2 && this.quantity == 9)) {
            this.levelUp(me, this)
        }

        this.maxHealth = (this.maxHealth + 1).coerceAtMost(50)

        this.damage = (this.damage + 1).coerceAtMost(50)
    }

    fun levelUp(me: Player, member: Monster) {
        member.level += 1
    }

    fun sell(me: Player) {
        me.money += this.level
    }
    fun copyMonster(): Monster

    val chestPlate: ChestPlate
    val helmet: Helmet
    val chargeBlade: ChargeBlade
}

val globalMembers = mapOf(
    Glavenus.ID to Pair(Glavenus::class) { Glavenus() },
    Rathalos.ID to Pair(Rathalos::class) { Rathalos() },
    `Tzitzi-Ya-Ku`.ID to Pair(`Tzitzi-Ya-Ku`::class) { `Tzitzi-Ya-Ku`() },
    Lunastra.ID to Pair(Lunastra::class) { Lunastra() },
    Teostra.ID to Pair(Teostra::class) { Teostra() },
)

