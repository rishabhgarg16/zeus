package com.hit11.zeus.repository

import com.hit11.zeus.model.Contest
import org.springframework.stereotype.Repository


@Repository
class ContestRepository {
    fun getContestForMatch(matchId: Int): List<Contest> {
        return listOf(
            Contest(
                id = 1,
                title = "Mega Contest",
                prize = "₹12.52 Crore",
                originalAmount = "₹49",
                discountedAmount = "₹1",
                discountText = "₹49 ₹1",
                discountEndsIn = "Discount ends in 57m 1s",
                detail = "₹1.35 Crore",
                spotsLeft = "26,25,766",
                totalSpots = "36,51,040",
                specialPrizes = "Rank 2-3: Tata SUV | 4-10: Bike"
            ),
            Contest(
                id = 2,
                title = "The 99 Contest",
                prize = "₹32.96 Lakhs",
                originalAmount = "₹49",
                discountedAmount = "₹1",
                discountEndsIn = "Discount ends in 57m 1s",
                discountText = "₹99",
                detail = "₹5 Lakhs",
                spotsLeft = "34,863",
                totalSpots = "44,550",
                specialPrizes = "Royal Enfield Bike"
            ),
            Contest(
                id = 3,
                title = "The 99 Contest",
                prize = "₹32.96 Lakhs",
                originalAmount = "₹49",
                discountedAmount = "₹1",
                discountEndsIn = "Discount ends in 57m 1s",
                discountText = "₹99",
                detail = "₹5 Lakhs",
                spotsLeft = "34,863",
                totalSpots = "44,550",
                specialPrizes = "Royal Enfield Bike"
            ),
            Contest(
                id = 4,
                title = "The 99 Contest",
                prize = "₹32.96 Lakhs",
                originalAmount = "₹49",
                discountedAmount = "₹1",
                discountEndsIn = "Discount ends in 57m 1s",
                discountText = "₹99",
                detail = "₹5 Lakhs",
                spotsLeft = "34,863",
                totalSpots = "44,550",
                specialPrizes = "Royal Enfield Bike"
            )


        )
    }
}

