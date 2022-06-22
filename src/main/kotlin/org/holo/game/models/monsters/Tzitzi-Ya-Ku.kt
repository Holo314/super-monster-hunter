package org.holo.game.models.monsters

import org.holo.game.models.*
import java.util.*

data class `Tzitzi-Ya-Ku`(
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


    override val chestPlate: ChestPlate = Yakuplate()
    override val helmet: Helmet = Yakuhelm()
    override val chargeBlade: ChargeBlade = Yakublade()

    var abilityTime = 1

    override fun levelUp(me: Player, member: Monster) {
        super.levelUp(me, member)
        if (member.level == 3) {
            abilityTime += 1
        }
    }

    override fun enterBattle(me: Player?, myDeck: Deck, otherDeck: Deck) {
        otherDeck.members.filterNotNull().plus(otherDeck.hunter)
            .forEach {
                it.setStatus(null, otherDeck, myDeck, Member.Stunned(abilityTime, this))
            }
        super.enterBattle(me, myDeck, otherDeck)
    }

    override fun copyMonster() = copy()

    companion object {
        val ID: UUID = UUID.fromString("78be1aaa-ca05-410f-8b55-53fb64d22fc2")
    }
}


class Yakuplate : ChestPlate {
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

class Yakuhelm : Helmet {
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

class Yakublade : ChargeBlade {
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