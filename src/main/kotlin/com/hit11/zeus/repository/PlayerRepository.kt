package com.hit11.zeus.repository

import com.hit11.zeus.model.Player
import com.hit11.zeus.model.PlayerRole
import org.springframework.stereotype.Repository


@Repository
class PlayerRepository {

    fun getPlayerList(): List<Player> {
        // Mock data for demonstration
        return listOf(
            Player(
                playerId = 1,
                name = "2044FD40",
                stat = 63,
                selectPerc = "95%",
                credits = 2,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.WICKETKEEPER
            ),
            Player(
                playerId = 2,
                name = "BA720925",
                stat = 3,
                selectPerc = "34%",
                credits = 2,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.WICKETKEEPER
            ),
            Player(
                playerId = 3,
                name = "3A32A873",
                stat = 45,
                selectPerc = "12%",
                credits = 4,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.BATTER
            ),
            Player(
                playerId = 4,
                name = "D3C1956E",
                stat = 22,
                selectPerc = "51%",
                credits = 6,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.BATTER
            ),
            Player(
                playerId = 5,
                name = "A1A9BCD4",
                stat = 99,
                selectPerc = "40%",
                credits = 4,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.BOWLER
            ),
            Player(
                playerId = 6,
                name = "ABD864F4",
                stat = 98,
                selectPerc = "23%",
                credits = 6,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.BOWLER
            ),
            Player(
                playerId = 7,
                name = "93DB0705",
                stat = 35,
                selectPerc = "92%",
                credits = 3,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.ALL_ROUNDER
            ),
            Player(
                playerId = 8,
                name = "F2924180",
                stat = 27,
                selectPerc = "91%",
                credits = 6,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.ALL_ROUNDER
            ),
            Player(
                playerId = 9,
                name = "FE0A34A1",
                stat = 77,
                selectPerc = "77%",
                credits = 5,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.BATTER
            ),
            Player(
                playerId = 10,
                name = "F06A4829",
                stat = 21,
                selectPerc = "26%",
                credits = 8,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.BOWLER
            ),
            Player(
                playerId = 11,
                name = "DDDD1265",
                stat = 88,
                selectPerc = "36%",
                credits = 8,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "BLR",
                role = PlayerRole.BOWLER
            ),
            Player(
                playerId = 12,
                name = "48EA186D",
                stat = 5,
                selectPerc = "74%",
                credits = 6,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.WICKETKEEPER
            ),
            Player(
                playerId = 13,
                name = "5E69EAA4",
                stat = 7,
                selectPerc = "50%",
                credits = 1,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.WICKETKEEPER
            ),
            Player(
                playerId = 14,
                name = "B6BFC15B",
                stat = 45,
                selectPerc = "79%",
                credits = 5,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.BATTER
            ),
            Player(
                playerId = 15,
                name = "B5EEC82F",
                stat = 96,
                selectPerc = "98%",
                credits = 1,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.BATTER
            ),
            Player(
                playerId = 16,
                name = "FE03CDAB",
                stat = 23,
                selectPerc = "15%",
                credits = 2,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.BOWLER
            ),
            Player(
                playerId = 17,
                name = "7673A15B",
                stat = 87,
                selectPerc = "48%",
                credits = 5,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.BOWLER
            ),
            Player(
                playerId = 18,
                name = "45EB0D36",
                stat = 14,
                selectPerc = "97%",
                credits = 3,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.ALL_ROUNDER
            ),
            Player(
                playerId = 19,
                name = "5735AF6E",
                stat = 78,
                selectPerc = "35%",
                credits = 7,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.ALL_ROUNDER
            ),
            Player(
                playerId = 20,
                name = "70B60ABF",
                stat = 81,
                selectPerc = "45%",
                credits = 8,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.BATTER
            ),
            Player(
                playerId = 21,
                name = "05DA756D",
                stat = 2,
                selectPerc = "68%",
                credits = 2,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.BOWLER
            ),
            Player(
                playerId = 22,
                name = "896A4721",
                stat = 44,
                selectPerc = "1%",
                credits = 6,
                iconUrl = "https//documents.iplt20.com/ipl/IPLHeadshot2024/549.png",
                teamName = "DEL",
                role = PlayerRole.BOWLER
            )
        )
    }
}

