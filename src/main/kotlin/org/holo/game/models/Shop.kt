package org.holo.game.models

import kotlin.random.Random

data class Shop(val content: MutableList<Monster>) {
    var chestPlate: ChestPlate? = null
    var helmet: Helmet? = null
    var chargeBlade: ChargeBlade? = null
    companion object {
        fun randomShop(): Shop {
            val shopItems = (0 until 5).map { Random.nextInt(globalMembers.size) }
                .map { globalMembers.entries.toList()[it].value.second.invoke() }
                .toMutableList()
            return Shop(shopItems)
        }
    }
}
sealed interface Modifier