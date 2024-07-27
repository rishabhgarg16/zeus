package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import javax.persistence.*

enum class QuestionType(val text: String) {
    // match questions
    MATCH_WINNER("match_winner"),
    RUNS_IN_MATCH("runs_in_match"),
    SUPER_OVER_IN_MATCH("super_over_in_match"),
    WIN_BY_RUNS_MARGIN("win_by_runs_margin"),
    TOSS_WINNER("toss_winner"),
    TOSS_DECISION("toss_decision"),

    // batting questions
    TOP_SCORER("top_scorer"),
    SIXES_IN_MATCH("sixes_in_match"),
    RUNS_SCORED_BY_BATSMAN("runs_scored_by_batsman"),
    TEAM_RUNS_IN_MATCH("team_runs_in_match"),
    MAN_OF_THE_MATCH("man_of_the_match"),

    // bowling
    WICKETS_IN_MATCH("wickets_in_match"),
    WICKETS_BY_BOWLER("wickets_by_bowler"),
    WICKETS_IN_OVER("wickets_in_over"),
    WIDES_IN_MATCH("wides_in_match"),
    ECONOMY_RATE("economy_rate_in_match"),
    TOTAL_EXTRAS("total_extras"),

    // invalid
    INVALID("invalid");

    companion object {
        fun fromText(type: String?): QuestionType {
            return entries.find { it.text == type } ?: INVALID
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class QuestionDataModel(
    val id: Int = 0,
    val matchId: Int = 0,
    val pulseQuestion: String = "",
    val optionA: String = "",
    val optionAWager: Long = -1L,
    val optionB: String = "",
    val optionBWager: Long = -1L,
    val userACount: Long? = -1L,
    val userBCount: Long? = -1L,
    val category: List<String>? = ArrayList(),
    var enabled: Boolean = false,
    var pulseResult: String? = "",
    val pulseImageUrl: String? = "",
    val pulseEndDate: Instant? = Instant.now(),
    val targetRuns: Int? = 0,
    val targetOvers: Int? = 0,
    val targetWickets: Int? = 0,
    val targetSixes: Int? = 0,
    val targetSpecificOver: Int? = 0,
    val targetExtras: Int? = 0,
    val targetWides: Int? = 0,
    val targetBoundaries: Int? = 0,
    val questionType: QuestionType = QuestionType.INVALID,
    val targetBatsmanId: Int? = 0,
    val targetBowlerId: Int? = 0,
    val targetTeamId: Int? = 0,
    val targetTossDecision: String? = "",
) {

    fun maptoEntity(): QuestionEntity {
        return QuestionEntity(
            id = this.id,
            matchId = this.matchId,
            pulseQuestion = this.pulseQuestion,
            optionA = this.optionA,
            optionAWager = this.optionAWager,
            optionB = this.optionB,
            optionBWager = this.optionBWager,
            userACount = this.userACount,
            userBCount = this.userBCount,
            category = this.category.toString(),
            status = this.enabled,
            pulseResult = this.pulseResult,
            pulseImageUrl = this.pulseImageUrl,
            pulseEndDate = this.pulseEndDate,
            targetRuns = this.targetRuns,
            targetOvers = this.targetOvers,
            targetExtras = this.targetExtras,
            targetWickets = this.targetWickets,
            targetWides = this.targetWides,
            targetSixes = this.targetSixes,
            targetBoundaries = this.targetBoundaries,
            targetSpecificOver = this.targetSpecificOver,
            targetBatsmanId = this.targetBatsmanId,
            targetBowlerId = this.targetBowlerId,
            targetTeamId = this.targetTeamId,
            targetTossDecision = this.targetTossDecision,
            questionType = this.questionType.text
        )
    }
}

@Entity
@Table(name = "pulse_questions")
data class QuestionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int = 0,
    @Lob
    @Column(
        columnDefinition = "TEXT",
        name = "pulse_question"
    )
    val pulseQuestion: String = "",

    @Column(name = "option_a")
    val optionA: String = "",

    @Column(name = "option_a_wager")
    val optionAWager: Long = -1L,

    @Column(name = "option_b")
    val optionB: String = "",

    @Column(name = "option_b_wager")
    val optionBWager: Long = -1L,

    @Column(name = "user_a_count")
    val userACount: Long? = -1L,

    @Column(name = "user_b_count")
    val userBCount: Long? = -1L,

    val category: String? = "",

    var status: Boolean = false,
    var pulseResult: String? = "",
    val pulseImageUrl: String? = "",
    val pulseEndDate: Instant? = Instant.now(),
    val targetRuns: Int? = 0,
    val targetOvers: Int? = 0,
    val targetBowlerId: Int? = 0,
    val targetBatsmanId: Int? = 0,
    val targetWickets: Int? = 0,
    val targetSixes: Int? = 0,
    val targetSpecificOver: Int? = 0,
    val targetExtras: Int? = 0,
    val targetWides: Int? = 0,
    val targetBoundaries: Int? = 0,
    val targetTeamId: Int? = 0,
    @Column(name = "target_toss_decision")
    val targetTossDecision: String? = null,
    val questionType: String? = "",

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    )
    var createdAt: Instant = Instant.now(),

    @Column(
        name = "updated_at",
        nullable = false
    )
    var updatedAt: Instant = Instant.now(),
) {
    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    private fun getCategoryList(): List<String>? = category?.split(",")?.map { it.trim() }

    fun mapToQuestionDataModel(): QuestionDataModel {
        return QuestionDataModel(
            id = this.id,
            matchId = matchId,
            pulseQuestion = this.pulseQuestion,
            optionA = this.optionA,
            optionAWager = this.optionAWager,
            optionB = this.optionB,
            optionBWager = this.optionBWager,
            userACount = this.userACount ?: -1L,
            userBCount = this.userBCount ?: -1L,
            category = getCategoryList(),
            enabled = this.status,
            pulseResult = this.pulseResult,
            pulseImageUrl = this.pulseImageUrl,
            pulseEndDate = this.pulseEndDate,
            targetRuns = this.targetRuns,
            targetOvers = this.targetOvers,
            targetExtras = this.targetExtras,
            targetWides = this.targetWides,
            targetWickets = this.targetWickets,
            targetSixes = this.targetSixes,
            targetBoundaries = this.targetBoundaries,
            targetSpecificOver = this.targetSpecificOver,
            targetBatsmanId = this.targetBatsmanId,
            targetBowlerId = this.targetBowlerId,
            questionType = QuestionType.fromText(this.questionType),
            targetTeamId = this.targetTeamId,
            targetTossDecision = this.targetTossDecision,
            )
    }
}