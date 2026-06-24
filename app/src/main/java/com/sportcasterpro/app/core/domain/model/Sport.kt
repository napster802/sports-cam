package com.sportcasterpro.app.core.domain.model

/**
 * Every sport SportCaster Pro can produce a scoreboard for. [periodLabel] and [periodCount]
 * describe the default period structure used to seed a new match before the user customizes it.
 */
enum class Sport(
    val displayName: String,
    val periodLabel: String,
    val defaultPeriodCount: Int,
    val hasPossessionIndicator: Boolean,
) {
    BASKETBALL("Basketball", "Quarter", 4, hasPossessionIndicator = true),
    VOLLEYBALL("Volleyball", "Set", 5, hasPossessionIndicator = true),
    FOOTBALL("Football", "Half", 2, hasPossessionIndicator = false),
    FUTSAL("Futsal", "Half", 2, hasPossessionIndicator = false),
    BADMINTON("Badminton", "Game", 3, hasPossessionIndicator = false),
    TENNIS("Tennis", "Set", 3, hasPossessionIndicator = true),
    TABLE_TENNIS("Table Tennis", "Game", 5, hasPossessionIndicator = false),
    BASEBALL("Baseball", "Inning", 9, hasPossessionIndicator = false),
    CRICKET("Cricket", "Innings", 2, hasPossessionIndicator = false),
    RUGBY("Rugby", "Half", 2, hasPossessionIndicator = false),
    HOCKEY("Hockey", "Period", 4, hasPossessionIndicator = true),
}
