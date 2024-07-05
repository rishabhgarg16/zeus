package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.ResourceNotFoundException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.*
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service class EventService(
    private val matchRepository: MatchRepository,
    private val inningRepository: InningRepository,
    private val batsmanPerformanceRepository: BatsmanPerformanceRepository,
    private val bowlerPerformanceRepository: BowlerPerformanceRepository,
    private val scoreRepository: ScoreRepository,
    private val questionService: QuestionService
) {

    private val logger = Logger.getLogger(EventService::class.java)

    @Transactional fun processBallEvent(ballEvent: BallEvent) {
        // Validate ball event data
        validateBallEvent(ballEvent)

        val inning = inningRepository.findByMatchIdAndInningNumber(
            ballEvent.matchId,
            ballEvent.inningId
        )
        if (inning == null) {
            logger.info("New Inning started for match ${ballEvent.matchId}")
            startNextInning(matchId = ballEvent.matchId)
        }

        val match = matchRepository.findById(ballEvent.matchId)
                .orElseThrow { ResourceNotFoundException("Match not found") }
                .mapToMatch()

        // Validate match and inning state
        validateMatchAndInningState(
            match,
            inning!!
        )

        // Ensure ball events are unique and processed in order
        val previousScore = validateBallOrder(ballEvent)

        // Update Batsman Performance
        val batsmanPerformance = batsmanPerformanceRepository.findByMatchIdAndPlayerId(
            match.id,
            ballEvent.batsmanId
        ) ?: BatsmanPerformance(
            matchId = match.id,
            inningId = inning.id,
            playerId = ballEvent.batsmanId
        )

        if (!ballEvent.isWide && !ballEvent.isBye) {
            batsmanPerformance.ballsFaced += 1
        }

        if (ballEvent.isNoBall || ballEvent.isLegBye) {
            // if leg bye hits hand or gloves then extra runs are added to batsman runs
            // on no ball extra runs are added to batsman runs if hit by batsman
            batsmanPerformance.runsScored += ballEvent.extraRuns
            if (ballEvent.extraRuns == 4) batsmanPerformance.fours += 1
            if (ballEvent.extraRuns == 6) batsmanPerformance.sixes += 1
            batsmanPerformance.ballsFaced += 1
        } else {
            // batsmanRuns will be 0 when wide or bye, and normal runs when normal delivery
            batsmanPerformance.runsScored += ballEvent.batsmanRuns
            if (ballEvent.batsmanRuns == 4) batsmanPerformance.fours += 1
            if (ballEvent.batsmanRuns == 6) batsmanPerformance.sixes += 1
        }

        if (ballEvent.isWicket) {
            batsmanPerformance.howOut = ballEvent.wicketType
            batsmanPerformance.bowlerId = ballEvent.bowlerId
            batsmanPerformance.fielderId = ballEvent.fielderId
            batsmanPerformance.wicketkeeperCatch = ballEvent.wicketkeeperCatch
        }
        batsmanPerformanceRepository.save(batsmanPerformance)

        // Calculate total extras from ball event
        val totalExtras = calculateTotalExtras(ballEvent)

        // Update Bowler Performance
        val bowlerPerformance = bowlerPerformanceRepository.findByMatchIdAndPlayerId(
            matchId = match.id,
            ballEvent.bowlerId
        ) ?: BowlerPerformance(
            matchId = match.id,
            inningId = inning.id,
            playerId = ballEvent.bowlerId
        )

        if (!ballEvent.isWide && !ballEvent.isNoBall) {
            bowlerPerformance.oversBowled += 0.1 // Assuming each ball increases overs by 0.1
        }

        bowlerPerformance.runsConceded += calculateRunsConcededByBowler(ballEvent)

        if (ballEvent.isWicket) {
            bowlerPerformance.wicketsTaken += 1
        }
        if (ballEvent.isWide) bowlerPerformance.wides += 1
        if (ballEvent.isNoBall) bowlerPerformance.noBalls += 1
        bowlerPerformanceRepository.save(bowlerPerformance)

        val newTotalRuns =
                previousScore.totalRuns.plus(batsmanPerformance.runsScored) ?: batsmanPerformance.runsScored

        val newTotalWickets =
                if (ballEvent.isWicket) {
                    previousScore.totalWickets.plus(1)
                } else {
                    previousScore.totalWickets
                }

        val newTotalExtras = previousScore.totalExtras.plus(totalExtras) ?: totalExtras

        // Update Score
        val score = Score(
            matchId = ballEvent.matchId,
            inningId = ballEvent.inningId,
            batmanId = batsmanPerformance.playerId,
            bowlerId = bowlerPerformance.playerId,
            batsmanRuns = batsmanPerformance.runsScored,
            extraRuns = totalExtras,
            overNumber = ballEvent.overNumber,
            ballNumber = ballEvent.ballNumber,
            totalRuns = newTotalRuns, // cumulative
            totalWickets = newTotalWickets, // cumulative
            totalExtras = newTotalExtras, // cumulative totalExtras
            isWicket = ballEvent.isWicket,
            wicketType = ballEvent.wicketType,
            isWide = ballEvent.isWide,
            isNoBall = ballEvent.isNoBall,
            isBye = ballEvent.isBye,
            isLegBye = ballEvent.isLegBye,
            isPenalty = ballEvent.isPenalty,
            isSix = ballEvent.batsmanRuns == 6 || ballEvent.extraRuns == 6, // no ball Six
            isFour = ballEvent.batsmanRuns == 4 || ballEvent.extraRuns == 4, // wide or bye 4 runs
        )

        scoreRepository.save(score)

        // Update Match Scores
        if (inning.inningNumber == 1) {
            match.team1Score += batsmanPerformance.runsScored
            if (ballEvent.isWicket) match.team1Wickets += 1
        } else if (inning.inningNumber == 2) {
            match.team2Score += batsmanPerformance.runsScored
            if (ballEvent.isWicket) match.team2Wickets += 1
        }
        matchRepository.save(match.mapToEntity())

        // Call QuestionService to update questions based on the ball event
        questionService.updateQuestions(ballEvent)
    }

    private fun calculateRunsConcededByBowler(ballEvent: BallEvent): Int {
        var totalRuns = 0
        if (ballEvent.isWide || ballEvent.isNoBall) {
            // wide and no ball are scored against the bowler
            totalRuns = 1 + ballEvent.extraRuns

        } else if (ballEvent.isBye || ballEvent.isLegBye) {
            // BYES DO NOT COUNT AS RUNS AGAINST THE BOWLER.
            // LEG BYES DO NOT COUNT AS RUNS AGAINST THE BOWLER.

            totalRuns = 0
        }
        return totalRuns
    }

    @Throws(Exception::class) fun startNextInning(
        matchId: Int
    ) {
        try {
            val match = matchRepository.findById(matchId).orElseThrow {
                ResourceNotFoundException(
                    "[EventService] Match not found"
                )
            }

            val inningNumber = if (match.currentInningId == null) 1 else 2
            val inning = Inning(
                matchId = match.id,
                inningNumber = inningNumber
            )
            inningRepository.save(inning)

            match.currentInningId = inning.id
            matchRepository.save(match)
        } catch (e: Exception) {
            throw Exception("[EventService] Error saving new innings ${e.message}")
        }
    }

    private fun validateBallEvent(ballEvent: BallEvent) {
        requireNotNull(ballEvent.matchId) { "Match ID is required" }
        requireNotNull(ballEvent.inningId) { "Inning ID is required" }
        requireNotNull(ballEvent.batsmanId) { "Batsman ID is required" }
        requireNotNull(ballEvent.bowlerId) { "Bowler ID is required" }
        requireNotNull(ballEvent.overNumber) { "Over number is required" }
        require(ballEvent.batsmanRuns in 0..6) { "Batsman runs must be between 0 and 6" }
        require(ballEvent.extraRuns >= 0) { "Extra runs must be non-negative" }
        require(ballEvent.wicketType in WicketType.entries.map { it.text }) { "Invalid wicket type" }

        // cricket scoring validation
        require(ballEvent.batsmanRuns == 0 && ballEvent.isWide) { "Wides cannot be scored by batsman" }
        require(ballEvent.batsmanRuns == 0 && ballEvent.isBye) { "Byes cannot be scored by batsman" }

        val extrasCount = listOf(
            ballEvent.isWide,
            ballEvent.isNoBall,
            ballEvent.isBye,
            ballEvent.isLegBye,
            ballEvent.isPenalty
        ).count { it }
        require(extrasCount <= 1) { "Only one type of extra can be recorded per ball" }
    }

    private fun validateMatchAndInningState(
        match: Match,
        inning: Inning
    ) {
        require(inning.id == match.currentInningId) { "Inning ID does not match current inning of the match" }
        require(inning.inningNumber in 1..2) { "Invalid inning number" }
        // Additional validations for match and inning states can be added here
    }

    private fun validateBallOrder(ballEvent: BallEvent): Score {
        val previousBall =
                scoreRepository.findTopByMatchIdAndInningIdOrderByOverNumberDescBallNumberDesc(
                    ballEvent.matchId,
                    ballEvent.inningId
                )
        previousBall?.let {
            if (previousBall.overNumber == ballEvent.overNumber && previousBall.ballNumber == ballEvent.ballNumber) {
                throw IllegalArgumentException("[EventService] Ball event already processed")
            }
        }

        previousBall?.let {
            require(
                it.overNumber < ballEvent.overNumber ||
                        (it.overNumber == ballEvent.overNumber && it.ballNumber < ballEvent.ballNumber)
            ) {
                "Ball event must be processed in order"
            }
        }

        return previousBall!!
    }

    private fun calculateTotalExtras(ballEvent: BallEvent): Int {
        var totalExtras = 0
        if (ballEvent.isWide) {
            totalExtras += 1
            totalExtras += ballEvent.extraRuns // as batsman runs are not scored
        } else if (ballEvent.isNoBall) {
            totalExtras += 1
            totalExtras += ballEvent.extraRuns
        } else if (ballEvent.isBye || ballEvent.isLegBye || ballEvent.isPenalty) {
            totalExtras += ballEvent.extraRuns
        }
        return totalExtras
    }
}
