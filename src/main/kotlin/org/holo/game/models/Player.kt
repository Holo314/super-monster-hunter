package org.holo.game.models

data class Player(val deck: Deck, var money: Int = 0) {
    companion object {
        fun new() = Player(Deck(Hunter(), mutableListOf(null, null, null, null)))
    }

    fun copyDeck() = this.deck.copy(
        hunter = this.deck.hunter.copyHunter(),
        members = this.deck.members.map { it?.copyMonster() }.toMutableList()
    )
}

data class Deck(val hunter: Hunter, val members: MutableList<Monster?>)