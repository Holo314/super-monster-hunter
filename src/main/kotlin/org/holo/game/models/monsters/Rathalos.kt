package org.holo.game.models.monsters

import org.holo.game.models.*
import java.util.*

data class Rathalos(
    override var maxHealth: Int = 1,
    override var damage: Int = 1,
    override var level: Int = 1,
    override var quantity: Int = 1,
    override val price: Int = 3
) : Monster {
    override var health: Int = maxHealth
    override val name: String = this::class.simpleName!!
    override val tier: Int = 1
    override val maxLevel: Int = 3
    override val statuses: MutableList<Member.Status> = mutableListOf()
    override var critChance: Int = 0
    override var stunResistance: Int = 0


    override val chestPlate: ChestPlate = Rathplate()
    override val helmet: Helmet = Rathhelm()
    override val chargeBlade: ChargeBlade = Rathblade()

    private val met: MutableList<Member> = mutableListOf()
    var abilityDamage = 1

    override fun enterTurn(me: Player?, myDeck: Deck, otherDeck: Deck) {
        val target = otherDeck.members.firstOrNull() ?: otherDeck.hunter
        if (!met.contains(target)) {
            target.damaged(null, otherDeck, myDeck, this, abilityDamage)
            target.setStatus(null, otherDeck, myDeck, Member.Stunned(1, this))
        }
        super.enterTurn(me, myDeck, otherDeck)
    }

    override fun levelUp(me: Player, member: Monster) {
        super.levelUp(me, member)
        abilityDamage += 1
    }

    override fun copyMonster() = copy()

    companion object {
        val ID: UUID = UUID.fromString("838c2389-a1b2-4163-a687-6d3ee2e5a603")
    }
}

class Rathplate: ChestPlate {
    override val name = this::class.simpleName!!
    val healthBonus = 1
    val critBonus = 20
    var bonusFlag = false

    override fun equip(player: Player) {
        player.deck.hunter.health += healthBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.health -= healthBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        bonusFlag = target.health < target.maxHealth
        if (bonusFlag) {
            myDeck.hunter.critChance += critBonus
        }
    }
    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        if (bonusFlag) {
            myDeck.hunter.critChance
        }
    }

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}

class Rathhelm: Helmet  {
    override val name = this::class.simpleName!!
    val healthBonus = 1
    val critBonus = 20
    var bonusFlag = false

    override fun equip(player: Player) {
        player.deck.hunter.health += healthBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.health -= healthBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        if (target.health < target.maxHealth) {
            bonusFlag = true
            myDeck.hunter.critChance += critBonus
        }
    }
    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        if (bonusFlag) {
            bonusFlag = false
            myDeck.hunter.critChance
        }
    }

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}
class Rathblade: ChargeBlade {
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