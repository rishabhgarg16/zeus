package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import javax.persistence.*

@JsonIgnoreProperties(ignoreUnknown = true)
class PulseDataModel(
    var id: Int = 0,
    var matchId: Int = 0,
    var pulseQuestion: String = "",
    var optionA: String = "",
    var optionAWager: Long = -1L,
    var optionB: String = "",
    var optionBWager: Long = -1L,
    var userACount: Long? = -1L,
    var userBCount: Long? = -1L,
    var category: List<String>? = ArrayList(),
    var enabled: Boolean = false,
    var pulseResult: String? = "",
    var pulseImageUrl: String? = "",
    var pulseEndDate: Instant? = Instant.now(),
)

@Entity
@Table(name = "pulse_questions")
data class PulseQuestionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int = 0,
    @Lob
    @Column(columnDefinition = "TEXT", name="pulse_question")
    var pulseQuestion: String = "",

    @Column(name = "option_a")
    var optionA: String = "",

    @Column(name = "option_a_wager")
    var optionAWager: Long = -1L,

    @Column(name = "option_b")
    var optionB: String = "",

    @Column(name = "option_b_wager")
    var optionBWager: Long = -1L,

    @Column(name = "user_a_count")
    var userACount: Long? = -1L,

    @Column(name = "user_b_count")
    var userBCount: Long? = -1L,

    @Column(name = "category")
    var category: String? = "",

    var status: Boolean = false,
    var pulseResult: String? = "",
    var pulseImageUrl: String? = "",
    var pulseEndDate: Instant? = Instant.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),


    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

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

    fun getCategoryList(): List<String>? = category?.split(",")?.map { it.trim() }

    fun setCategoryList(categories: List<String>) {
        category = categories.joinToString(",")
    }

    fun mapToPulseDataModel(): PulseDataModel {
        return PulseDataModel(
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
            pulseEndDate = this.pulseEndDate
        )
    }
}