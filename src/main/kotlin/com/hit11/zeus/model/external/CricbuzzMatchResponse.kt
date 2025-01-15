package com.hit11.zeus.model.external// CricbuzzMatchResponse.kt

data class CricbuzzMatchResponse(
    val typeMatches: List<TypeMatch>,
//    val filters: Filters,
//    val appIndex: AppIndex,
    val responseLastUpdated: String
)

data class TypeMatch(
    val matchType: String,
    val seriesMatches: List<SeriesMatch>
)

data class SeriesMatch(
    val seriesAdWrapper: SeriesAdWrapper?
)

data class SeriesAdWrapper(
    val seriesId: Int,
    val seriesName: String,
    val matches: List<CricbuzzMatch>
)

data class CricbuzzMatch(
    val matchInfo: CricbuzzMatchInfo,
//    val matchScore: MatchScore?
)

data class CricbuzzMatchInfo(
    val matchId: Int,
    val seriesId: Int,
    val seriesName: String,
    val matchDesc: String,
    val matchFormat: String,
    val startDate: String,
    val endDate: String,
    val state: String,
    val status: String,
    val team1: CricbuzzTeam,
    val team2: CricbuzzTeam,
    val venueInfo: VenueInfo,
    val seriesStartDt: String,
    val seriesEndDt: String,
    val isTimeAnnounced: Boolean = false,
    val stateTitle: String
)

data class CricbuzzTeam(
    val teamId: Int,
    val teamName: String,
    val teamSName: String,
    val imageId: Int?
)

data class VenueInfo(
    val ground: String,
    val city: String,
    val timezone: String
)