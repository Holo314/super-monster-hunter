package org.holo.game.models.monsters

import org.holo.game.models.*
import java.util.*

data class Glavenus(
    override var maxHealth: Int = 1,
    override var damage: Int = 1,
    override var level: Int = 1,
    override var quantity: Int = 1,
    override val price: Int = 3,
) : Monster {
    override var health: Int = maxHealth
    override val name: String = this::class.simpleName!!
    override val tier: Int = 1
    override val maxLevel: Int = 3
    override val statuses: MutableList<Member.Status> = mutableListOf()

    override val chestPlate: ChestPlate = Glavplate()
    override val helmet: Helmet = Glavhelm()
    override val chargeBlade: ChargeBlade = Glavblade()
    override var critChance: Int = 0
    override var stunResistance: Int = 0

    var abilityBuff = 1

    override fun copyMonster() = copy()

    override fun levelUp(me: Player, member: Monster) {
        super.levelUp(me, member)
        abilityBuff += 1
    }

    override fun endTurn(me: Player?, myDeck: Deck, otherDeck: Deck) {
        this.damage += abilityBuff
        super.endTurn(me, myDeck, otherDeck)
    }


    companion object {
        val ID: UUID = UUID.fromString("607376a6-f0ba-4ccd-bbb2-c2f6461431ef")
    }
}


class Glavplate: ChestPlate {
    override val name = this::class.simpleName!!
    val healthBonus = 1
    val critBonus = 50
    var counter = 0

    override fun equip(player: Player) {
        player.deck.hunter.health += healthBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.health -= healthBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        counter = (counter + 1) % 2
        if (counter == 0) {
            myDeck.hunter.critChance += critBonus
        }
    }

    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        if (counter == 0) {
            myDeck.hunter.critChance -= critBonus
        }
    }

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}

class Glavhelm: Helmet {
    override val name = this::class.simpleName!!
    val healthBonus = 1
    val critBonus = 50
    var counter = 0

    override fun equip(player: Player) {
        player.deck.hunter.health += healthBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.health -= healthBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        counter = (counter + 1) % 2
        if (counter == 0) {
            myDeck.hunter.critChance += critBonus
        }
    }

    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        if (counter == 0) {
            myDeck.hunter.critChance -= critBonus
        }
    }

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}

class Glavblade: ChargeBlade {
    override val name = this::class.simpleName!!
    val dmgBonus = 1
    override fun equip(player: Player) {
        player.deck.hunter.damage += dmgBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.damage -= dmgBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {}
    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {}

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}