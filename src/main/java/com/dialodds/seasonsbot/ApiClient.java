package com.dialodds.seasonsbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.List;
import java.util.logging.Logger;

@Component
public class ApiClient {

    private static final Logger logger = Logger.getLogger(ApiClient.class.getName());
    private final RestTemplate restTemplate;
    private final String apiBaseUrl;

    public ApiClient(RestTemplate restTemplate, @Value("${api.base.url}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
    }

    private String buildUrl(String endpoint) {
        return apiBaseUrl + endpoint;
    }

    private <T> ResponseEntity<T> makeGetRequest(String url, ParameterizedTypeReference<T> responseType, Object... uriVariables) {
        logger.info("Making GET request to: " + url);
        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType, uriVariables);
        logger.info("Response body: " + response.getBody());
        return response;
    }

    private <T> ResponseEntity<T> makePostRequest(String url, Object body, Class<T> responseType, Object... uriVariables) {
        logger.info("Making POST request to: " + url);
        return restTemplate.postForEntity(url, body, responseType, uriVariables);
    }

    public ResponseEntity<Integer> createSeason(int startWeek, int endWeek, int initialCoins) {
        String url = buildUrl("/api/seasons?startWeek={startWeek}&endWeek={endWeek}&initialCoins={initialCoins}");
        return makePostRequest(url, null, Integer.class, startWeek, endWeek, initialCoins);
    }

    public ResponseEntity<List<Season>> getActiveSeasons() {
        String url = buildUrl("/api/seasons/active");
        return makeGetRequest(url, new ParameterizedTypeReference<List<Season>>() {});
    }

    public ResponseEntity<Map<String, Object>> getSeasonById(int seasonId) {
        String url = buildUrl("/api/seasons/{seasonId}");
        return makeGetRequest(url, new ParameterizedTypeReference<Map<String, Object>>() {}, seasonId);
    }

    public ResponseEntity<Integer> createUser(String discordId, String username) {
        String url = buildUrl("/api/users?discordId={discordId}&username={username}");
        return makePostRequest(url, null, Integer.class, discordId, username);
    }

    public ResponseEntity<Void> addUserToSeason(int userId, int seasonId) {
        String url = buildUrl("/api/users/{userId}/seasons/{seasonId}");
        return makePostRequest(url, null, Void.class, userId, seasonId);
    }

    public ResponseEntity<Void> createUserAndJoinSeason(String discordId, String username, int seasonId) {
        String url = buildUrl("/api/users/join-season?discordId={discordId}&username={username}&seasonId={seasonId}");
        return makePostRequest(url, null, Void.class, discordId, username, seasonId);
    }

    public ResponseEntity<List<User>> getUsersBySeason(int seasonId) {
        String url = buildUrl("/api/users/seasons/{seasonId}");
        return makeGetRequest(url, new ParameterizedTypeReference<List<User>>() {}, seasonId);
    }

    public ResponseEntity<Integer> getUserCoins(int userId, int seasonId) {
        String url = buildUrl("/api/users/{userId}/seasons/{seasonId}/coins");
        return makeGetRequest(url, new ParameterizedTypeReference<Integer>() {}, userId, seasonId);
    }

    public ResponseEntity<Integer> placeBet(int userId, int seasonId, int gameId, String betType, int amount) {
        String url = buildUrl("/api/bets?userId={userId}&seasonId={seasonId}&gameId={gameId}&betType={betType}&amount={amount}");
        return makePostRequest(url, null, Integer.class, userId, seasonId, gameId, betType, amount);
    }

    public ResponseEntity<List<Bet>> getUserBets(int userId, int seasonId) {
        String url = buildUrl("/api/bets/users/{userId}/seasons/{seasonId}");
        return makeGetRequest(url, new ParameterizedTypeReference<List<Bet>>() {}, userId, seasonId);
    }

    public ResponseEntity<List<Integer>> getNflWeeks() {
        String url = buildUrl("/api/nfl/weeks");
        return makeGetRequest(url, new ParameterizedTypeReference<List<Integer>>() {});
    }

    public ResponseEntity<List<Game>> getNflGamesByWeek(int week) {
        String url = buildUrl("/api/nfl/games/{week}");
        ResponseEntity<List<Game>> response = makeGetRequest(url, new ParameterizedTypeReference<List<Game>>() {}, week);
        List<Game> games = response.getBody();
        System.out.println("Parsed games: " + games);
        return response;
    }

    public ResponseEntity<List<Game>> getTeamSchedule(String team) {
        String url = buildUrl("/api/nfl/schedule/{team}");
        return makeGetRequest(url, new ParameterizedTypeReference<List<Game>>() {}, team);
    }

    public ResponseEntity<Map<String, Object>> getGameDetails(int gameId) {
        String url = buildUrl("/api/nfl/games/{gameId}");
        return makeGetRequest(url, new ParameterizedTypeReference<Map<String, Object>>() {}, gameId);
    }

    public ResponseEntity<Map<String, Object>> getGameById(int gameId) {
        String url = buildUrl("/api/nfl/games/id/{gameId}");
        return makeGetRequest(url, new ParameterizedTypeReference<Map<String, Object>>() {}, gameId);
    }

    public ResponseEntity<Map<String, Object>> deleteSeason(int seasonId) {
        String url = buildUrl("/api/seasons/" + seasonId);
        try {
            return restTemplate.exchange(url, HttpMethod.DELETE, null, new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (HttpStatusCodeException e) {
            logger.warning("Error deleting season: " + e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "deleted", false,
                "message", "Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString()
            ));
        }
    }
}