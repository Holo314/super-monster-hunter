package org.holo.game.models.monsters

import org.holo.game.models.*
import java.util.*

data class Lunastra(
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

    override val chestPlate: ChestPlate = Lunaplate()
    override val helmet: Helmet = Lunahelm()
    override val chargeBlade: ChargeBlade = Lunablade()
    override var critChance: Int = 0
    override var stunResistance: Int = 0

    var abilityDamage = 1
    var duoAbilityDamage = 1

    override fun levelUp(me: Player, member: Monster) {
        super.levelUp(me, member)
        if (member.level == 2) {
            abilityDamage += 1
        } else {
            duoAbilityDamage += 1
        }
    }

    override fun copyMonster() = copy()

    override fun enterTurn(me: Player?, myDeck: Deck, otherDeck: Deck) {
        val all = listOf(myDeck.hunter) +
                myDeck.members.toList().filterNotNull() +
                otherDeck.members.toList().reversed().filterNotNull() +
                listOf(otherDeck.hunter)

        val pos = all.indexOf(this)
        all[pos + 1].damaged(me, myDeck, otherDeck, this, abilityDamage)
        all[pos - 1].damaged(me, myDeck, otherDeck, this, abilityDamage)
    }

    override fun enterBattle(me: Player?, myDeck: Deck, otherDeck: Deck) {
        if (myDeck.members.filterIsInstance<Teostra>().isEmpty()) return

        otherDeck.members.filterNotNull()
            .forEach {
                it.damaged(null, otherDeck, myDeck, this, duoAbilityDamage)
            }
    }

    companion object {
        val ID: UUID = UUID.fromString("41190293-c2a5-435f-8e06-e86e0e66de66")
    }
}

class Lunaplate : ChestPlate {
    override val name = this::class.simpleName!!
    val healthBonus = 1

    override fun equip(player: Player) {
        player.deck.hunter.health += healthBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.health -= healthBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {}
    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {}

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}

class Lunahelm : Helmet {
    override val name = this::class.simpleName!!
    val healthBonus = 1

    override fun equip(player: Player) {
        player.deck.hunter.health += healthBonus
    }

    override fun unequip(player: Player) {
        player.deck.hunter.health -= healthBonus
    }

    override fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {}
    override fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {}

    override fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member) {}
}

class Lunablade : ChargeBlade {
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