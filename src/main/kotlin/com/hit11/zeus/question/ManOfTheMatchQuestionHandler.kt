package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository

data class ManOfTheMatchParameter(val targetPlayerId: Int) : QuestionParameter()

class ManOfTheMatchTriggerCondition : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        // TODO this can generate multiple duplicate questions
        return currentState.liveScorecard.state == CricbuzzMatchPlayingState.IN_PROGRESS
    }
}

class ManOfTheMatchParameterGenerator : QuestionParameterGenerator<ManOfTheMatchParameter> {
    override fun generateParameters(
        currentState: MatchState,
        previousState: MatchState?
    ): List<ManOfTheMatchParameter> {
        // Get top 3 batsmen
        val topBatsmen = currentState.liveScorecard.innings
            .flatMap { it.battingPerformances }
            .sortedByDescending { it.runs }
            .take(2)
            .map { it.playerId }

        // Get top 3 bowlers
        val topBowlers = currentState.liveScorecard.innings
            .flatMap { it.bowlingPerformances }
            .sortedByDescending { it.wickets }
            .take(2)
            .map { it.playerId }

        // Combine top performers and remove duplicates
        val topPerformers = (topBatsmen + topBowlers).distinct()

        return topPerformers.map { playerId ->
            ManOfTheMatchParameter(playerId)
        }
    }
}


class ManOfTheMatchQuestionGenerator(
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<ManOfTheMatchParameter>,
    override val validator: ManOfTheMatchQuestionValidator
) : BaseQuestionGenerator<ManOfTheMatchParameter>(questionRepository) {
    override val type = QuestionType.MAN_OF_THE_MATCH

    override fun questionExists(param: ManOfTheMatchParameter, state: MatchState): Boolean {
        // TOOD we are using batsman id instead od player id, man of the match can be bowler as well
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetBatsmanId(
            state.liveScorecard.matchId,
            QuestionType.MATCH_WINNER,
            param.targetPlayerId
        )
    }
    override fun createQuestion(param: ManOfTheMatchParameter, state: MatchState): Question? {
        val battingPlayers = state.liveScorecard.innings.flatMap { it.battingPerformances }
        val player = battingPlayers.find { it.playerId == param.targetPlayerId }
        if (player != null) {
            return createDefaultQuestion(
                matchId = state.liveScorecard.matchId,
                pulseQuestion = "Will ${player.playerName} be the Man of the Match?",
                optionA = PulseOption.Yes.name,
                optionB = PulseOption.No.name,
                category = listOf("Match Award"),
                questionType = QuestionType.MAN_OF_THE_MATCH,
                targetBatsmanId = param.targetPlayerId,
                param = param,
                state = state
            )
        }

        val bowlingPlayers = state.liveScorecard.innings.flatMap { it.bowlingPerformances }
        val bowlingplayer = bowlingPlayers.find { it.playerId == param.targetPlayerId } ?: return null

        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${bowlingplayer.playerName} be the Man of the Match?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Match Award"),
            questionType = QuestionType.MAN_OF_THE_MATCH,
            targetBatsmanId = param.targetPlayerId,
            param = param,
            state = state
        )


    }
}

class ManOfTheMatchQuestionValidator : QuestionValidator {
    override fun validateParameters(matchState: MatchState): Boolean {
        // Check if the match is in a valid state for Man of the Match questions
        return matchState.liveScorecard.state == CricbuzzMatchPlayingState.IN_PROGRESS
    }

    override fun validateQuestion(question: Question): Boolean {
        if (question.questionType != QuestionType.MAN_OF_THE_MATCH) {
            return false
        }
        if (question.targetBatsmanId == null || question.targetBatsmanId == 0) {
            throw QuestionValidationException("Target player must be specified for Man of the Match question")
        }
        return true
    }
}

class ManOfTheMatchResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: Question, matchState: MatchState): Boolean =
        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE && matchState.liveScorecard.playerOfTheMatch != null

    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val playerOfTheMatch = matchState.liveScorecard.playerOfTheMatch
        val result = if (playerOfTheMatch?.id == question.targetBatsmanId) PulseResult.Yes else PulseResult.No
        return QuestionResolution(true, result)
    }
}