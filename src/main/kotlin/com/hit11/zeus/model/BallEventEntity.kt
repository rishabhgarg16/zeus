package com.hit11.zeus.model

import java.math.BigDecimal
import javax.persistence.*

@Entity
@Table(name = "innings")
data class Inning(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    var matchId: Int = 0,
    var inningsNumber: Int = 0,
    var battingTeamId: Int? = null,
    var bowlingTeamId: Int? = null,
    // runs
    var totalRuns: Int = 0,
    var totalSixes: Int = 0,
    var totalFours: Int = 0,
    // wickets
    var totalWickets: Int = 0,
    var overs: BigDecimal = BigDecimal.ZERO,
    var runRate: Float = 0.0f,
    // extras
    var totalExtras: Int = 0,
    var noBalls: Int = 0,
    var wides: Int = 0,
    var byes: Int = 0,
    var legByes: Int = 0,
    var penalties: Int = 0,
)

@Entity
@Table(name = "scores")
data class BallEventEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int = 0,
    val inningId: Int = 0,
    val batsmanId: Int = 0,
    val bowlerId: Int = 0,
    val batsmanRuns: Int = 0,
    val extraRuns: Int = 0,
    val overNumber: Int = 0,
    val ballNumber: Int = 0,
    val isWicket: Boolean = false,
    val wicketType: String? = null,
    val isWide: Boolean = false,
    val isNoBall: Boolean = false,
    val isBye: Boolean = false,
    val isLegBye: Boolean = false,
    val isPenalty: Boolean = false,
    val isSix: Boolean = false,
    val isFour: Boolean = false
)

@Entity
@Table(name = "batsman_performances")
data class BatsmanPerformance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int = 0,
    val inningId: Int = 0,
    val playerId: Int = 0,
    var ballsFaced: Int = 0,
    var runsScored: Int = 0,
    var fours: Int = 0,
    var sixes: Int = 0,
    var outDescription: String? = null,
    var bowlerId: Int? = null,
    var fielderId: Int? = null,
    var strikeRate: Float = 1.0F,
//    val battingStatus: BattingStatus = BattingStatus.NOT_OUT,
    @Column(name = "wicketkeeper_catch")
    var wicketkeeperCatch: Boolean = false, // true if the catch was taken by the wicketkeeper
)

enum class BattingStatus(val value: String) {
    NOT_OUT("not_out"),
    OUT("out"),
    RETIRED_HURT("retired_hurt"),
    DOUBT_WICKET("doubt_wicket"),
    CAUGHT_AND_RETIRED("caught_and_retired"),
    YET_TO_BAT("yet_to_bat");

    companion object {
        fun fromValue(value: String): BattingStatus? =
            BattingStatus.values().find { it.value == value }
    }

}

@Entity
@Table(name = "bowler_performances")
data class BowlerPerformance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int = 0,
    val inningId: Int = 0,
    val playerId: Int = 0,
    var oversBowled: Double = 0.0,
    var runsConceded: Int = 0,
    var wicketsTaken: Int = 0,
    var wides: Int = 0,
    var noBalls: Int = 0,
    var maidens: Int = 0,
    var economy: BigDecimal = BigDecimal.ZERO,
)