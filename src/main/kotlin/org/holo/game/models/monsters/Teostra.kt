package org.holo.game.models.monsters

import org.holo.game.models.*
import java.util.*

data class Teostra(
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

    override val chestPlate: ChestPlate = Toastplate()
    override val helmet: Helmet = Toasthelm()
    override val chargeBlade: ChargeBlade = Toastblade()

    var abilityDamage = 1

    override fun levelUp(me: Player, member: Monster) {
        super.levelUp(me, member)
        abilityDamage += 1
    }

    override fun damaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member, dmg: Int) {
        super.damaged(me, myDeck, otherDeck, by, dmg)
        val rEnemy = otherDeck.members.filterNotNull().plus(otherDeck.hunter).random()
        rEnemy.setStatus(null, otherDeck, myDeck, Member.BlastBlight(abilityDamage, this))
    }

    override fun copyMonster() = copy()

    companion object {
        val ID: UUID = UUID.fromString("bd32b153-6192-49f1-b9e6-0159d59f3a2d")
    }
}


class Toastplate : ChestPlate {
    override val name = this::class.simpleName!!
    val healthBonus = 1
    val stunResistanceBonus = 20
    override fun equip(player: Player) {
        player.deck.hunter.health += healthBonus
        player.deck.hunter.stunResistance += stunResistanceBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.health -= healthBonus
        player.deck.hunter.stunResistance -= stunResistanceBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {}
    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {}

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}

class Toasthelm : Helmet {
    override val name = this::class.simpleName!!
    val baseCritBonus = 10
    val CritBonus = 20
    val threshold = 30
    var bonusFlag = false

    override fun equip(player: Player) {
        player.deck.hunter.critChance += baseCritBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.critChance -= baseCritBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        bonusFlag = myDeck.hunter.health < myDeck.hunter.maxHealth * (threshold / 100)
        if (bonusFlag) {
            myDeck.hunter.critChance += baseCritBonus
        }
    }

    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        if (bonusFlag) {
            myDeck.hunter.critChance -= baseCritBonus
        }
    }

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}

class Toastblade : ChargeBlade {
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