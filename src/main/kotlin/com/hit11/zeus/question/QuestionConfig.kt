package com.hit11.zeus.question

import com.hit11.zeus.model.QuestionType
import com.hit11.zeus.question.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuestionConfig {

    // Trigger Conditions
    @Bean
    fun teamRunsInMatchTrigger() = TeamRunsInMatchTrigger(5)

    @Bean
    fun sixesByPlayerTriggerCondition() = SixesByPlayerTriggerCondition()

    @Bean
    fun runsScoredByBatsmanTriggerCondition() = RunsScoredByBatsmanTriggerCondition()

    @Bean
    fun wicketsByBowlerTriggerCondition() = WicketsByBowlerTriggerCondition(2)

    @Bean
    fun winByRunsMarginTriggerCondition() = WinByRunsMarginTriggerCondition()

    @Bean
    fun matchWinnerTriggerCondition() = MatchWinnerTriggerCondition()

    @Bean
    fun widesByBowlerTriggerCondition() = WidesByBowlerTriggerCondition(2)

    @Bean
    fun tossWinnerTriggerCondition() = TossWinnerTriggerCondition()

    @Bean
    fun tossDecisionTriggerCondition() = TossDecisionTriggerCondition()

    @Bean
    fun wicketsInOverTriggerCondition() = WicketsInOverTriggerCondition()

    // Parameter Generators
    @Bean
    fun teamRunsInMatchParameterGenerator() = TeamRunsInMatchParameterGenerator()

    @Bean
    fun sixesByPlayerParameterGenerator() = SixesByPlayerParameterGenerator()

    @Bean
    fun runsScoredByBatsmanParameterGenerator() = RunsScoredByBatsmanParameterGenerator()

    @Bean
    fun wicketsByBowlerParameterGenerator() = WicketsByBowlerParameterGenerator()

    @Bean
    fun winByRunsMarginParameterGenerator() = WinByRunsMarginParameterGenerator()

    @Bean
    fun matchWinnerParameterGenerator() = MatchWinnerParameterGenerator()

    @Bean
    fun widesByBowlerParameterGenerator() = WidesByBowlerParameterGenerator()

    @Bean
    fun tossWinnerParameterGenerator() = TossWinnerParameterGenerator()

    @Bean
    fun tossDecisionParameterGenerator() = TossDecisionParameterGenerator()

    @Bean
    fun wicketsInOverParameterGenerator() = WicketsInOverParameterGenerator()

    // Validators
    @Bean
    fun teamRunsInMatchQuestionValidator() = TeamRunsInMatchQuestionValidator()

    @Bean
    fun sixesByPlayerQuestionValidator() = SixesByPlayerQuestionValidator()

    @Bean
    fun runsScoredByBatsmanQuestionValidator() = RunsScoredByBatsmanQuestionValidator()

    @Bean
    fun wicketsByBowlerQuestionValidator() = WicketsByBowlerQuestionValidator()

    @Bean
    fun winByRunsMarginQuestionValidator() = WinByRunsMarginQuestionValidator()

    @Bean
    fun matchWinnerQuestionValidator() = MatchWinnerQuestionValidator()

    @Bean
    fun widesByBowlerQuestionValidator() = WidesByBowlerQuestionValidator()

    @Bean
    fun tossWinnerQuestionValidator() = TossWinnerQuestionValidator()

    @Bean
    fun tossDecisionQuestionValidator() = TossDecisionQuestionValidator()

    @Bean
    fun wicketsInOverQuestionValidator() = WicketsInOverQuestionValidator()

    // Question Generators
    @Bean
    fun questionGenerators(
        teamRunsInMatchTrigger: TeamRunsInMatchTrigger,
        teamRunsInMatchParameterGenerator: TeamRunsInMatchParameterGenerator,
        teamRunsInMatchQuestionValidator: TeamRunsInMatchQuestionValidator,
        sixesByPlayerTriggerCondition: SixesByPlayerTriggerCondition,
        sixesByPlayerParameterGenerator: SixesByPlayerParameterGenerator,
        sixesByPlayerQuestionValidator: SixesByPlayerQuestionValidator,
        runsScoredByBatsmanTriggerCondition: RunsScoredByBatsmanTriggerCondition,
        runsScoredByBatsmanParameterGenerator: RunsScoredByBatsmanParameterGenerator,
        runsScoredByBatsmanQuestionValidator: RunsScoredByBatsmanQuestionValidator,
        wicketsByBowlerTriggerCondition: WicketsByBowlerTriggerCondition,
        wicketsByBowlerParameterGenerator: WicketsByBowlerParameterGenerator,
        wicketsByBowlerQuestionValidator: WicketsByBowlerQuestionValidator,
        winByRunsMarginTriggerCondition: WinByRunsMarginTriggerCondition,
        winByRunsMarginParameterGenerator: WinByRunsMarginParameterGenerator,
        winByRunsMarginQuestionValidator: WinByRunsMarginQuestionValidator,
        matchWinnerTriggerCondition: MatchWinnerTriggerCondition,
        matchWinnerParameterGenerator: MatchWinnerParameterGenerator,
        matchWinnerQuestionValidator: MatchWinnerQuestionValidator,
        widesByBowlerTriggerCondition: WidesByBowlerTriggerCondition,
        widesByBowlerParameterGenerator: WidesByBowlerParameterGenerator,
        widesByBowlerQuestionValidator: WidesByBowlerQuestionValidator,
        tossWinnerTriggerCondition: TossWinnerTriggerCondition,
        tossWinnerParameterGenerator: TossWinnerParameterGenerator,
        tossWinnerQuestionValidator: TossWinnerQuestionValidator,
        tossDecisionTriggerCondition: TossDecisionTriggerCondition,
        tossDecisionParameterGenerator: TossDecisionParameterGenerator,
        tossDecisionQuestionValidator: TossDecisionQuestionValidator,
        wicketsInOverTriggerCondition: WicketsInOverTriggerCondition,
        wicketsInOverParameterGenerator: WicketsInOverParameterGenerator,
        wicketsInOverQuestionValidator: WicketsInOverQuestionValidator
    ) = listOf(
        TeamRunsInMatchQuestionGenerator(
            triggerCondition = teamRunsInMatchTrigger,
            parameterGenerator = teamRunsInMatchParameterGenerator,
            validator = teamRunsInMatchQuestionValidator
        ),
        SixesByPlayerQuestionGenerator(
            triggerCondition = sixesByPlayerTriggerCondition,
            parameterGenerator = sixesByPlayerParameterGenerator,
            validator = sixesByPlayerQuestionValidator
        ),
        RunsScoredByBatsmanQuestionGenerator(
            triggerCondition = runsScoredByBatsmanTriggerCondition,
            parameterGenerator = runsScoredByBatsmanParameterGenerator,
            validator = runsScoredByBatsmanQuestionValidator
        ),
        WicketsByBowlerQuestionGenerator(
            triggerCondition = wicketsByBowlerTriggerCondition,
            parameterGenerator = wicketsByBowlerParameterGenerator,
            validator = wicketsByBowlerQuestionValidator
        ),
        WinByRunsMarginQuestionGenerator(
            triggerCondition = winByRunsMarginTriggerCondition,
            parameterGenerator = winByRunsMarginParameterGenerator,
            validator = winByRunsMarginQuestionValidator
        ),
        MatchWinnerQuestionGenerator(
            triggerCondition = matchWinnerTriggerCondition,
            parameterGenerator = matchWinnerParameterGenerator,
            validator = matchWinnerQuestionValidator
        ),
        WidesByBowlerQuestionGenerator(
            triggerCondition = widesByBowlerTriggerCondition,
            parameterGenerator = widesByBowlerParameterGenerator,
            validator = widesByBowlerQuestionValidator
        ),
        TossWinnerQuestionGenerator(
            triggerCondition = tossWinnerTriggerCondition,
            parameterGenerator = tossWinnerParameterGenerator,
            validator = tossWinnerQuestionValidator
        ),
        TossDecisionQuestionGenerator(
            triggerCondition = tossDecisionTriggerCondition,
            parameterGenerator = tossDecisionParameterGenerator,
            validator = tossDecisionQuestionValidator
        ),
        WicketsInOverQuestionGenerator(
            triggerCondition = wicketsInOverTriggerCondition,
            parameterGenerator = wicketsInOverParameterGenerator,
            validator = wicketsInOverQuestionValidator
        )
    )

    // Resolution Strategies
    @Bean
    fun resolutionStrategies(): Map<QuestionType, ResolutionStrategy> = mapOf(
        QuestionType.TEAM_RUNS_IN_MATCH to TeamRunsInMatchResolutionStrategy(),
        QuestionType.SIX_BY_PLAYER to SixesByPlayerResolutionStrategy(),
        QuestionType.RUNS_SCORED_BY_BATSMAN to RunsScoredByBatsmanResolutionStrategy(),
        QuestionType.WICKETS_BY_BOWLER to WicketsByBowlerResolutionStrategy(),
        QuestionType.WIN_BY_RUNS_MARGIN to WinByRunsMarginResolutionStrategy(),
        QuestionType.MATCH_WINNER to MatchWinnerResolutionStrategy(),
        QuestionType.WIDES_BY_BOWLER to WidesByBowlerResolutionStrategy(),
        QuestionType.TOSS_WINNER to TossWinnerResolutionStrategy(),
        QuestionType.TOSS_DECISION to TossDecisionResolutionStrategy(),
        QuestionType.WICKETS_IN_OVER to WicketsInOverResolutionStrategy()
    )
}