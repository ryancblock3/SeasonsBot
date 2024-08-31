package com.dialodds.seasonsbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.List;

@Component
public class ApiClient {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public ApiClient(RestTemplate restTemplate, @Value("${api.base.url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    public ResponseEntity<Integer> createSeason(int startWeek, int endWeek, int initialCoins) {
        String url = apiBaseUrl + "/api/seasons";
        return restTemplate.postForEntity(url + "?startWeek={startWeek}&endWeek={endWeek}&initialCoins={initialCoins}", 
                                          null, Integer.class, startWeek, endWeek, initialCoins);
    }

    public ResponseEntity<List> getActiveSeasons() {
        String url = apiBaseUrl + "/api/seasons/active";
        return restTemplate.getForEntity(url, List.class);
    }

    public ResponseEntity<Map> getSeasonById(int seasonId) {
        String url = apiBaseUrl + "/api/seasons/{seasonId}";
        return restTemplate.getForEntity(url, Map.class, seasonId);
    }

    public ResponseEntity<Integer> createUser(String discordId, String username) {
        String url = apiBaseUrl + "/api/users";
        return restTemplate.postForEntity(url + "?discordId={discordId}&username={username}", 
                                          null, Integer.class, discordId, username);
    }

    public ResponseEntity<Void> addUserToSeason(int userId, int seasonId) {
        String url = apiBaseUrl + "/api/users/{userId}/seasons/{seasonId}";
        return restTemplate.postForEntity(url, null, Void.class, userId, seasonId);
    }

    public ResponseEntity<Void> createUserAndJoinSeason(String discordId, String username, int seasonId) {
        String url = apiBaseUrl + "/api/users/join-season";
        return restTemplate.postForEntity(url + "?discordId={discordId}&username={username}&seasonId={seasonId}", 
                                          null, Void.class, discordId, username, seasonId);
    }

    public ResponseEntity<List> getUsersBySeason(int seasonId) {
        String url = apiBaseUrl + "/api/users/seasons/{seasonId}";
        return restTemplate.getForEntity(url, List.class, seasonId);
    }

    public ResponseEntity<Integer> getUserCoins(int userId, int seasonId) {
        String url = apiBaseUrl + "/api/users/{userId}/seasons/{seasonId}/coins";
        return restTemplate.getForEntity(url, Integer.class, userId, seasonId);
    }

    public ResponseEntity<Integer> placeBet(int userId, int seasonId, int gameId, String betType, int amount) {
        String url = apiBaseUrl + "/api/bets";
        return restTemplate.postForEntity(url + "?userId={userId}&seasonId={seasonId}&gameId={gameId}&betType={betType}&amount={amount}", 
                                          null, Integer.class, userId, seasonId, gameId, betType, amount);
    }

    public ResponseEntity<List> getUserBets(int userId, int seasonId) {
        String url = apiBaseUrl + "/api/bets/users/{userId}/seasons/{seasonId}";
        return restTemplate.getForEntity(url, List.class, userId, seasonId);
    }

    public ResponseEntity<List> getNflWeeks() {
        String url = apiBaseUrl + "/api/nfl/weeks";
        return restTemplate.getForEntity(url, List.class);
    }

    public ResponseEntity<List> getNflGamesByWeek(int week) {
        String url = apiBaseUrl + "/api/nfl/games/{week}";
        return restTemplate.getForEntity(url, List.class, week);
    }

    public ResponseEntity<List> getTeamSchedule(String team) {
        String url = apiBaseUrl + "/api/nfl/schedule/{team}";
        return restTemplate.getForEntity(url, List.class, team);
    }

    public ResponseEntity<Map> getGameDetails(int gameId) {
        String url = apiBaseUrl + "/api/nfl/games/{gameId}";
        return restTemplate.getForEntity(url, Map.class, gameId);
    }

    public ResponseEntity<Map> getGameById(int gameId) {
        String url = apiBaseUrl + "/api/nfl/games/id/{gameId}";
        return restTemplate.getForEntity(url, Map.class, gameId);
    }

    public ResponseEntity<Map> deleteSeason(int seasonId) {
        String url = apiBaseUrl + "/api/seasons/" + seasonId;
        try {
            return restTemplate.exchange(url, HttpMethod.DELETE, null, Map.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode()).body(Map.of(
                "deleted", false,
                "message", "Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString()
            ));
        }
    }
}