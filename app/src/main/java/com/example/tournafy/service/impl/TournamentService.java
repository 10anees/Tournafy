package com.example.tournafy.service.impl;

import com.example.tournafy.data.repository.offline.MatchFirestoreRepository;
import com.example.tournafy.data.repository.offline.TournamentFirestoreRepository;
import com.example.tournafy.domain.models.team.TournamentTeam;
import com.example.tournafy.domain.models.tournament.Tournament;
import com.example.tournafy.domain.models.tournament.TournamentMatch;
import com.example.tournafy.service.interfaces.ITournamentService;
import com.example.tournafy.service.strategies.tournament.IBracketGenerationStrategy;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TournamentService implements ITournamentService {

    private final TournamentFirestoreRepository tournamentRepository;
    private final MatchFirestoreRepository matchRepository;

    @Inject
    public TournamentService(TournamentFirestoreRepository tournamentRepository,
                             MatchFirestoreRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    public void generateBrackets(Tournament tournament, IBracketGenerationStrategy strategy, TournamentCallback<Tournament> callback) {
        try {
            // Ensure teams list is not null
            List<TournamentTeam> teams = tournament.getTeams() != null ? tournament.getTeams() : new ArrayList<>();

            // Use the Strategy to generate matches
            List<TournamentMatch> matches = strategy.generate(teams);

            // TODO: Assign 'matches' to a specific stage in the tournament here
            // e.g., tournament.getCurrentStage().setMatches(matches);

            // Update in Firestore
            tournamentRepository.update(tournament)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(tournament))
                    .addOnFailureListener(callback::onError);

        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void updateStandings(String tournamentId, String completedMatchId, TournamentCallback<Void> callback) {
        // Implementation placeholder
        System.out.println("Updating standings for tournament: " + tournamentId);
        callback.onSuccess(null);
    }

    @Override
    public void getTournamentTeams(String tournamentId, TournamentCallback<List<TournamentTeam>> callback) {
        // Note: observeForever is used here because we are in a Service (no LifecycleOwner).
        // Ideally, repositories should return Task<Data> for one-shot requests in Services.
        // IMPORTANT: Remove observer after getting data to prevent memory leaks
        androidx.lifecycle.Observer<Tournament> teamsObserver = new androidx.lifecycle.Observer<Tournament>() {
            @Override
            public void onChanged(Tournament tournament) {
                if (tournament != null) {
                    // Now getTeams() will resolve correctly
                    callback.onSuccess(tournament.getTeams());
                } else {
                    callback.onError(new Exception("Tournament not found"));
                }
                // Remove observer after execution to prevent infinite loop and memory leaks
                tournamentRepository.getById(tournamentId).removeObserver(this);
            }
        };
        tournamentRepository.getById(tournamentId).observeForever(teamsObserver);
    }

    @Override
    public void advanceKnockoutTeams(String tournamentId, String stageId, TournamentCallback<Void> callback) {
        // Implementation placeholder
        System.out.println("Advancing teams for stage: " + stageId);
        callback.onSuccess(null);
    }
}