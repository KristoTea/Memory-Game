package hr.fer.ruzaosa.projekt.ruzaosa.memory.retrofit

import hr.fer.ruzaosa.lecture4.ruzaosa.k.retrofit.User

data class GameBody(val challenger: User, val challenged: User, val gameId: Long) {
    // je li potreban LocalDateTime1 / 2 ?
}