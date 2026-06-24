package com.sportcasterpro.app.feature.match.domain.usecase

import com.sportcasterpro.app.core.domain.model.Match
import com.sportcasterpro.app.core.domain.model.MatchStatus
import com.sportcasterpro.app.core.domain.model.Sport
import com.sportcasterpro.app.core.domain.model.Team
import com.sportcasterpro.app.core.domain.util.Resource
import com.sportcasterpro.app.feature.match.domain.MatchRepository
import com.sportcasterpro.app.feature.match.domain.TeamRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GetMatchesUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
) {
    operator fun invoke(): Flow<List<Match>> = matchRepository.observeMatches()
}

class ObserveMatchUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
) {
    operator fun invoke(matchId: String): Flow<Match?> = matchRepository.observeMatch(matchId)
}

class CreateMatchUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository,
) {
    suspend operator fun invoke(
        sport: Sport,
        homeTeamName: String,
        awayTeamName: String,
        location: String,
        dateTimeEpochMillis: Long,
    ): Resource<Match> {
        if (homeTeamName.isBlank() || awayTeamName.isBlank()) {
            return Resource.Error("Both team names are required")
        }

        val homeTeam = Team(id = UUID.randomUUID().toString(), name = homeTeamName.trim(), colorHex = "#FF6A1A")
        val awayTeam = Team(id = UUID.randomUUID().toString(), name = awayTeamName.trim(), colorHex = "#1A6AFF")
        teamRepository.upsertTeam(homeTeam)
        teamRepository.upsertTeam(awayTeam)

        val match = Match(
            id = UUID.randomUUID().toString(),
            sport = sport,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            dateTimeEpochMillis = dateTimeEpochMillis,
            location = location.trim(),
            status = MatchStatus.SCHEDULED,
        )
        matchRepository.createMatch(match)
        return Resource.Success(match)
    }
}

class UpdateMatchStatusUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(matchId: String, status: MatchStatus) =
        matchRepository.updateStatus(matchId, status)
}
