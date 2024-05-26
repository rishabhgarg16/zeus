package com.hit11.zeus.repository

import com.hit11.zeus.model.Match
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MatchRepository {

    fun getUpcomingMatches(): List<Match> {
        // Mock data for demonstration
        return listOf(
            Match(
                id = 1,
                title = "IPL 2024",
                team1 = "BLR",
                team2 = "GUJ",
                time = Calendar.getInstance().timeInMillis + 3600000,  // 1 hour from now
                location = "Bengaluru",
                prize = "Win ₹1.5 Crores + Mahindra THAR",
                matchType = "T20"
            ),
            Match(
                id = 2,
                title = "EPL 2024",
                team1 = "BE-CC",
                team2 = "FNC",
                time = Calendar.getInstance().timeInMillis + 5400000,  // 1.5 hours from now
                location = "Italy",
                prize = "Win iPhone + ₹1 Lakh",
                matchType = "T20"
            ),
            Match(
                3,
                "Rwanda Women's Series",
                "Charity CC",
                "CHA",
                time = Calendar.getInstance().timeInMillis + 7200000,
                location = "MUHAN",
                prize = "Win iPhone + ₹1 Lakh",
                matchType = "T20"
            ),
            Match(
                4,
                "Emirates D50 Championship",
                "Team A",
                "TEA",
                time = Calendar.getInstance().timeInMillis + 7200000,
                location = "TEB",
                prize = "Win iPhone + ₹1 Lakh",
                matchType = "T20"
            ),
            Match(
                id = 5,
                title = "IPL 2024",
                team1 = "BLR",
                team2 = "GUJ",
                time = Calendar.getInstance().timeInMillis + 3600000,  // 1 hour from now
                location = "Bengaluru",
                prize = "Win ₹1.5 Crores + Mahindra THAR",
                matchType = "T20"
            ),
            Match(
                id = 6,
                title = "IPL 2024",
                team1 = "BLR",
                team2 = "GUJ",
                time = Calendar.getInstance().timeInMillis + 3600000,  // 1 hour from now
                location = "Bengaluru",
                prize = "Win ₹1.5 Crores + Mahindra THAR",
                matchType = "T20"
            ),
        )
    }
}
