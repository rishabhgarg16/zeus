package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.ResourceNotFoundException
import com.hit11.zeus.livedata.BallEvent
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.livedata.Innings
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.*
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class EventService(
    private val matchRepository: MatchRepository,
    private val inningRepository: InningRepository,
    private val batsmanPerformanceRepository: BatsmanPerformanceRepository,
    private val bowlerPerformanceRepository: BowlerPerformanceRepository,
    private val ballEventRepository: BallEventRepository,
    private val questionService: QuestionService
) {

    private val logger = Logger.getLogger(EventService::class.java)

    @Transactional
    fun processBallEvent(
        liveScoreEvent: Hit11Scorecard
    ): UpdateQuestionsResponse {
        // Validate ball event data
        validateBallEvent(liveScoreEvent)

        val match = matchRepository.findById(liveScoreEvent.matchId)
            .orElseThrow { ResourceNotFoundException("Match not found") }
            .mapToMatch(null, null)

        val currentInnings = findCurrentInnings(liveScoreEvent) ?: throw Exception("Match completed")

        val inningEntity = inningRepository.findByMatchIdAndInningsNumber(
            liveScoreEvent.matchId,
            currentInnings.inningsId
        )

        if (inningEntity == null) {
            throw ResourceNotFoundException("No innings found for match ${liveScoreEvent.matchId}")
        }
//            ?: run {
//            logger.info("New Inning started for match ${ballEvent.matchId}")
//            startNextInning(match = match)
//        }

        // Validate match and inning state
//        validateMatchAndInningState(
//            match,
//            inning
//        )

        // Ensure ball events are unique and processed in order
        val ballEvents = currentInnings.ballByBallEvents
        val currentBallEvent = ballEvents.sortedWith(
            compareByDescending<BallEvent> { it.overNumber }
                .thenByDescending { it.ballNumber }).first()


        val previousScore = validateBallOrder(match, inningEntity, currentBallEvent)

        updateBatsmanPerformances(match, currentInnings)
        updateBowlerPerformances(match, currentInnings)

        updateInningEntity(currentInnings, inningEntity, match.id)
        inningRepository.save(inningEntity)

        // Update Score
        val ballEventEntity = BallEventEntity(
            matchId = liveScoreEvent.matchId,
            inningId = inningEntity.inningsNumber,
            batsmanId = currentBallEvent.batsmanId,
            bowlerId = currentBallEvent.bowlerId,
            batsmanRuns = currentBallEvent.runsScored,
            extraRuns = currentBallEvent.extraRuns,
            overNumber = currentBallEvent.overNumber,
            ballNumber = currentBallEvent.ballNumber,
            isWicket = currentBallEvent.isWicket,
            wicketType = currentBallEvent.wicketType,
            isWide = currentBallEvent.isWide,
            isNoBall = currentBallEvent.isNoBall,
            isBye = currentBallEvent.isBye,
            isLegBye = currentBallEvent.isLegBye,
            isPenalty = currentBallEvent.isPenalty,
            isSix = currentBallEvent.runsScored == 6 || currentBallEvent.extraRuns == 6, // no ball Six
            isFour = currentBallEvent.runsScored == 4 || currentBallEvent.extraRuns == 4, // wide or bye 4 runs
        )

        ballEventRepository.save(ballEventEntity)
        matchRepository.save(match.mapToEntity())

        // Call QuestionService to update questions based on the ball event
        val updatedQuestionsResponse =
            questionService.updateQuestions(liveScoreEvent)
        return updatedQuestionsResponse
    }

    private fun updateInningEntity(currentInnings: Innings, inningEntity: Inning, matchId: Int) {
        inningEntity.apply {
            this.matchId = matchId
            inningsNumber = currentInnings.inningsId
            battingTeamId = currentInnings.battingTeam.id
            bowlingTeamId = currentInnings.bowlingTeam.id
            totalRuns += currentInnings.totalRuns
            totalWickets += currentInnings.wickets
            overs = currentInnings.overs
            runRate = currentInnings.runRate
            totalExtras = currentInnings.totalExtras
        }
    }

    private fun updateBatsmanPerformances(match: Match, currentInnings: Innings) {
        val batsmanPerformanceUpdate = currentInnings.battingPerformances
        val battingPerformanceEntities = batsmanPerformanceRepository.findByMatchIdAndPlayerIdIn(
            match.id, batsmanPerformanceUpdate.map { it.playerId })

        val updatedBatsmanPerformances = batsmanPerformanceUpdate.map { event ->
            battingPerformanceEntities.find { it?.playerId == event.playerId }?.apply {
                runsScored += event.runs
                ballsFaced += event.balls
                fours += event.fours
                sixes += event.sixes
                strikeRate = event.strikeRate
            } ?: BatsmanPerformance(
                playerId = event.playerId,
                runsScored = event.runs,
                ballsFaced = event.balls,
                fours = event.fours,
                sixes = event.sixes,
                strikeRate = event.strikeRate
            )
        }

        batsmanPerformanceRepository.saveAll(updatedBatsmanPerformances)
    }

    private fun updateBowlerPerformances(match: Match, currentInnings: Innings) {
        val bowlerPerformances = currentInnings.bowlingPerformances
        val bowlingPerformanceEntities = bowlerPerformanceRepository.findByMatchIdAndPlayerIdIn(
            match.id, bowlerPerformances.map { it.playerId })

        val updatedBowlerPerformances = bowlerPerformances.map { event ->
            bowlingPerformanceEntities.find { it?.playerId == event.playerId }?.apply {
                wicketsTaken += event.wickets
                runsConceded += event.runs
                wides += event.wides
                noBalls += event.noBalls
                maidens += event.maidens
                economy = event.economy
            } ?: BowlerPerformance(
                playerId = event.playerId,
                wicketsTaken = event.wickets,
                runsConceded = event.runs,
                wides = event.wides,
                noBalls = event.noBalls,
                maidens = event.maidens,
                economy = event.economy
            )
        }

        bowlerPerformanceRepository.saveAll(updatedBowlerPerformances)
    }

    private fun saveBallEvent(currentBallEvent: BallEvent, matchId: Int, inningsNumber: Int) {
        val ballEventEntity = BallEventEntity(
            matchId = matchId,
            inningId = inningsNumber,
            batsmanId = currentBallEvent.batsmanId,
            bowlerId = currentBallEvent.bowlerId,
            batsmanRuns = currentBallEvent.runsScored,
            extraRuns = currentBallEvent.extraRuns,
            overNumber = currentBallEvent.overNumber,
            ballNumber = currentBallEvent.ballNumber,
            isWicket = currentBallEvent.isWicket,
            wicketType = currentBallEvent.wicketType,
            isWide = currentBallEvent.isWide,
            isNoBall = currentBallEvent.isNoBall,
            isBye = currentBallEvent.isBye,
            isLegBye = currentBallEvent.isLegBye,
            isPenalty = currentBallEvent.isPenalty,
            isSix = currentBallEvent.runsScored == 6 || currentBallEvent.extraRuns == 6,
            isFour = currentBallEvent.runsScored == 4 || currentBallEvent.extraRuns == 4
        )

        ballEventRepository.save(ballEventEntity)
    }


    fun findCurrentInnings(scoreCard: Hit11Scorecard): Innings? {
        if (scoreCard.status == "Complete") {
            return null
        }
        val inningsList = scoreCard.innings
//        val firstInningsComplete =
//            inningsList[0].wickets == 10 ||
//                    (inningsList[0].overs.toInt() == 20 && scoreCard.matchFormat == "T20")
//
//        return if (!firstInningsComplete) {
//            inningsList[0]
//        } else {
//            inningsList[1]
//        }
        return Innings()
    }


    private fun validateBallEvent(ballEvent: Hit11Scorecard) {
//        requireNotNull(ballEvent.matchId) { "Match ID is required" }
//        require(ballEvent.innings.size == 2) { "Inning ID List Size Should be 2" }
//        require(ballEvent.innings[0].battingPerformances.all { it != null }) { "All Batsman IDs are required" }
//        require(ballEvent.innings[0].bowlingPerformances.all { it != null }) { "All Bowling IDs are required" }
//        requireNotNull(ballEvent.overNumber) { "Over number is required" }
//        require(ballEvent.batsmanRuns in 0..6) { "Batsman runs must be between 0 and 6" }
//        require(ballEvent.extraRuns >= 0) { "Extra runs must be non-negative" }
//        require(ballEvent.wicketType in WicketType.entries.map { it.text }) { "Invalid wicket type" }

//        // cricket scoring validation
//        if (ballEvent.isWide) {
//            require(ballEvent.batsmanRuns == 0) { "Batsman Run should be 0 on wides" }
//        }
//        if (ballEvent.isBye) {
//            require(ballEvent.batsmanRuns == 0) { "Batsman Run should be 0 on byes" }
//        }
//        val extrasCount = listOf(
//            ballEvent.isWide,
//            ballEvent.isNoBall,
//            ballEvent.isBye,
//            ballEvent.isLegBye,
//            ballEvent.isPenalty
//        ).count { it }
//        require(extrasCount <= 1) { "Only one type of extra can be recorded per ball" }
    }

    private fun validateMatchAndInningState(
        match: Match,
        inning: Inning
    ) {
//        require(inning.id == match.currentInningId) { "Inning ID does not match current inning of the match" }
        require(inning.inningsNumber in 0..1) { "Invalid inning number" }
        // Additional validations for match and inning states can be added here
    }

    private fun validateBallOrder(
        match: Match,
        inning: Inning,
        currentBallEvent: BallEvent
    ): com.hit11.zeus.model.BallEventEntity? {
        val previousBall =
            ballEventRepository.findTopByMatchIdAndInningIdOrderByOverNumberDescBallNumberDesc(
                matchId = match.id,
                inningId = inning.inningsNumber
            )
        previousBall?.let {
            if (previousBall.overNumber == currentBallEvent.overNumber && previousBall.ballNumber == currentBallEvent.ballNumber) {
                throw IllegalArgumentException("[EventService] Ball event already processed")
            }
        }

        previousBall?.let {
            require(
                it.overNumber < currentBallEvent.overNumber ||
                        (it.overNumber == currentBallEvent.overNumber && it.ballNumber < currentBallEvent.ballNumber!!)
            ) {
                "Ball event must be processed in order"
            }
        }
        return previousBall
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


//    private fun savePartnerships(partnershipsData: Map<String, PartnershipData>, innings: Innings) {
//        partnershipsData.values.forEach { partnership ->
//            val partnershipEntity = Partnership(
//                inningsId = innings.inningsId,
//                runs = partnership.totalRuns,
//                balls = partnership.totalBalls,
//                player1Id = partnership.bat1Id,
//                player1Name = partnership.bat1Name,
//                player1Runs = partnership.bat1Runs,
//                player2Id = partnership.bat2Id,
//                player2Name = partnership.bat2Name,
//                player2Runs = partnership.bat2Runs
//            )
//            partnershipRepository.save(partnershipEntity)
//        }
//    }

//    private fun saveFallOfWickets(wicketsData: Map<String, WicketData>, innings: Innings) {
//        wicketsData.values.forEach { wicket ->
//            val fallOfWicket = FallOfWicket(
//                wicketNumber = wicket.wktNbr,
//                inningsId = innings.inningsId,
//                playerOut = wicket.batId,
//                playerName = wicket.batName,
//                runs = wicket.wktRuns,
//                overs = wicket.wktOver.toFloat()
//            )
//            fallOfWicketRepository.save(fallOfWicket)
//        }
//    }
}
