package com.hit11.zeus.model.response

import com.hit11.zeus.model.Match

data class GetMatchApiResponse(
    val id: Int = -1,  // Human-readable ID
    val matchGroup: String? = null,
    val team1: String = "",
    val team1ImageUrl: String? = "",
    val team2: String = "",
    val team2ImageUrl: String? = "",
    val city: String? = null,
    val stadium: String? = null,
    val country: String? = null,
    val tournamentName: String? = null,
    val matchType: String? = null,
    val matchStatus: String = "Scheduled",
    val matchLink: String? = null,
    val startDate: String = "",
    val team1ShortName: String? = null,
    val team2ShortName: String? = null,
    val endDate: String = "",

)

fun Match.toGetMatchApiResponse(): GetMatchApiResponse {
    return GetMatchApiResponse(
        id = id,
        matchGroup = matchGroup,
        team1 = team1Name,
        team1ImageUrl = team1ImageUrl,
        team2 = team2Name,
        team2ImageUrl = team2ImageUrl,
        city = city,
        stadium = stadium,
        country = country,
        tournamentName = tournamentName,
        matchType = matchType,
        matchStatus = status,
        matchLink = matchLink,
        startDate = startDate.toString(),
        team1ShortName = team1ShortName,
        team2ShortName = team2ShortName,
        endDate = endDate.toString()
    )
}