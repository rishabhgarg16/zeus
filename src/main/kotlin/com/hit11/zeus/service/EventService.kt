import com.hit11.zeus.model.BallEvent
import com.hit11.zeus.model.BatsmanPerformance
import com.hit11.zeus.model.BowlerPerformance
import com.hit11.zeus.model.Inning
import com.hit11.zeus.repository.*
import com.hit11.zeus.service.QuestionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class EventService @Autowired constructor(
    private val matchRepository: MatchRepository,
    private val inningRepository: InningRepository,
    private val batsmanPerformanceRepository: BatsmanPerformanceRepository,
    private val bowlerPerformanceRepository: BowlerPerformanceRepository,
    private val scoreRepository: ScoreRepository,
    private val questionService: QuestionService
) {

    @Transactional
    fun processBallEvent(ballEvent: BallEvent) {
        val inning = inningRepository.findById(ballEvent.inningId)
                .orElseThrow { ResourceNotFoundException("Inning not found") }

        val match = matchRepository.findById(inning.matchId)
                .orElseThrow { ResourceNotFoundException("Match not found") }

        // Update Batsman Performance
        val batsmanPerformance = batsmanPerformanceRepository
                .findByInningIdAndPlayerId(inning.id, ballEvent.batsmanId)
                .firstOrNull() ?: BatsmanPerformance(inningId = inning.id, playerId = ballEvent.batsmanId)

        batsmanPerformance.ballsFaced += 1
        batsmanPerformance.runsScored += ballEvent.runsScored
        if (ballEvent.runsScored == 4L) batsmanPerformance.fours += 1
        if (ballEvent.runsScored == 6L) batsmanPerformance.sixes += 1
        if (ballEvent.isWicket) {
            batsmanPerformance.howOut = ballEvent.wicketType
            batsmanPerformance.bowlerId = ballEvent.bowlerId
        }
        batsmanPerformanceRepository.save(batsmanPerformance)

        // Update Bowler Performance
        val bowlerPerformance = bowlerPerformanceRepository
                .findByInningIdAndPlayerId(inning.id, ballEvent.bowlerId)
                .firstOrNull() ?: BowlerPerformance(inningId = inning.id, playerId = ballEvent.bowlerId)

        bowlerPerformance.oversBowled += 0.1 // Assuming each ball increases overs by 0.1
        bowlerPerformance.runsConceded += ballEvent.runsScored
        if (ballEvent.isWicket) {
            bowlerPerformance.wicketsTaken += 1
        }
        if (ballEvent.isWide) bowlerPerformance.wides += 1
        if (ballEvent.isNoBall) bowlerPerformance.noBalls += 1
        bowlerPerformanceRepository.save(bowlerPerformance)

        // Update Score
        val score = scoreRepository.findByInningIdAndOverNumber(inning.id, ballEvent.overNumber)
                .firstOrNull() ?: Score(inningId = inning.id, overNumber = ballEvent.overNumber)

        score.runs += ballEvent.runsScored
        if (ballEvent.isWicket) score.wickets += 1
        if (ballEvent.isWide) score.wides += 1
        if (ballEvent.isNoBall) score.noBalls += 1
        scoreRepository.save(score)

        // Update Match Scores
        if (inning.inningNumber == 1) {
            match.team1Score += ballEvent.runsScored
            if (ballEvent.isWicket) match.team1Wickets += 1
        } else if (inning.inningNumber == 2) {
            match.team2Score += ballEvent.runsScored
            if (ballEvent.isWicket) match.team2Wickets += 1
        }
        matchRepository.save(match)

        // Call QuestionService to update questions based on the ball event
        questionService.updateQuestions(ballEvent)
    }

    fun startNextInning(matchId: Long, teamId: Long) {
        val match = matchRepository.findById(matchId)
                .orElseThrow { ResourceNotFoundException("Match not found") }

        val inningNumber = if (match.currentInningId == null) 1 else 2
        val inning = Inning(matchId = match.id, teamId = teamId, inningNumber = inningNumber)
        inningRepository.save(inning)

        match.currentInningId = inning.id
        matchRepository.save(match)
    }
}
