package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.BatchSize
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class QuestionType {
    // match questions
    MATCH_WINNER,
    RUNS_IN_MATCH,
    SUPER_OVER_IN_MATCH,
    WIN_BY_RUNS_MARGIN,
    TOSS_WINNER,
    TOSS_DECISION,

    // batting questions
    TOP_SCORER,
    SIX_BY_PLAYER,
    RUNS_SCORED_BY_BATSMAN,
    TEAM_RUNS_IN_MATCH,
    MAN_OF_THE_MATCH,

    // bowling
    WICKETS_IN_MATCH,
    WICKETS_BY_BOWLER,
    WICKETS_IN_OVER,
    WIDES_BY_BOWLER,
    ECONOMY_RATE,
    TOTAL_EXTRAS,

    // invalid
    INVALID;
}

enum class QuestionStatus {
    SYSTEM_GENERATED,  // Newly created by the system, waiting for review
    DISABLED,
    MANUAL_DRAFT,      // Manually created, not yet approved
    LIVE,              // Currently active
    RESOLVED,          // Question has been answered/resolved
    CANCELLED          // Question was cancelled or deemed invalid
}


enum class PulseResult { Yes, No, UNDECIDED }
enum class PulseOption { Yes, No }

@Entity
@Table(name = "pulse_questions")
@BatchSize(size = 50)
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val matchId: Int = 0,

    @Lob
    @Column(columnDefinition = "TEXT", name = "pulse_question")
    val pulseQuestion: String = "",

    @Column(name = "option_a")
    val optionA: String = "",

    @Column(name = "option_a_wager")
    val optionAWager: BigDecimal = BigDecimal(5),

    @Column(name = "option_b")
    val optionB: String = "",

    @Column(name = "option_b_wager")
    val optionBWager: BigDecimal = BigDecimal(5),

    @Column(name = "user_a_count")
    val userACount: Long? = -1L,

    @Column(name = "user_b_count")
    val userBCount: Long? = -1L,

    @JsonIgnore
    val category: String? = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: QuestionStatus = QuestionStatus.SYSTEM_GENERATED,

    @Enumerated(EnumType.STRING)
    @Column(name = "pulse_result")
    var pulseResult: PulseResult = PulseResult.UNDECIDED,

    val pulseImageUrl: String? = "",
    val pulseEndDate: Instant? = Instant.now(),

    // Target fields
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

    @Enumerated(EnumType.STRING)
    val questionType: QuestionType? = QuestionType.INVALID,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
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

    // Helper methods
    fun getCategory(): List<String> = category?.split(",")?.map { it.trim() } ?: emptyList()
    val categoryList: List<String> get() = getCategory()
}