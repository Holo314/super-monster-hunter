package org.holo.game.models

import java.util.*

data class Hunter(
    override var damage: Int = 1,
    override var maxHealth: Int = 1,
    override var level: Int = 1,
) : Member {
    override var quantity: Int = 1
        get() {
            throw Error("Can not change quantity of Hunter")
        }

    override var health: Int = maxHealth
    override val price: Int = Int.MAX_VALUE
    override val name: String = this::class.simpleName!!
    override val tier: Int = 0
    override val maxLevel: Int = 3
    override val statuses: MutableList<Member.Status> = mutableListOf()
    override var critChance: Int = 0
    override var stunResistance: Int = 0
    var chestPlate: ChestPlate? = null
    var helmet: Helmet? = null
    var chargeBlade: ChargeBlade? = null

    fun copyHunter() = copy()


    override fun attack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member) {
        chestPlate?.beforeAttack(me, myDeck, otherDeck, target)
        helmet?.beforeAttack(me, myDeck, otherDeck, target)
        chargeBlade?.beforeAttack(me, myDeck, otherDeck, target)
        super.attack(me, myDeck, otherDeck, target)
        chestPlate?.afterAttack(me, myDeck, otherDeck, target)
        helmet?.afterAttack(me, myDeck, otherDeck, target)
        chargeBlade?.afterAttack(me, myDeck, otherDeck, target)
    }

    override fun damaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member, dmg: Int) {
        super.damaged(me, myDeck, otherDeck, by, dmg)
        chestPlate?.afterDamaged(me, myDeck, otherDeck, by)
        helmet?.afterDamaged(me, myDeck, otherDeck, by)
        chargeBlade?.afterDamaged(me, myDeck, otherDeck, by)
    }

    companion object {
        val ID: UUID = UUID.fromString("e60dd226-99d4-4026-9be7-5bb0e9ecb701")
    }
}

interface Item {
    val name: String
    fun equip(player: Player)
    fun unequip(player: Player)
    fun beforeAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member)
    fun afterAttack(me: Player?, myDeck: Deck, otherDeck: Deck, target: Member)
    fun afterDamaged(me: Player?, myDeck: Deck, otherDeck: Deck, by: Member)
}

interface ChestPlate : Item
interface Helmet : Item
interface ChargeBlade : Item