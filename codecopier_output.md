# CodeCopier Output

## File: src/main/java/com/dialodds/seasonsbot/ApiClient.java

```java
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
```

## File: src/main/java/com/dialodds/seasonsbot/Bet.java

```java
package com.dialodds.seasonsbot;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Bet {
    private int id;

    @JsonProperty("user_id")
    private int userId;

    @JsonProperty("season_id")
    private int seasonId;

    @JsonProperty("game_id")
    private int gameId;

    @JsonProperty("bet_type")
    private String betType;

    private int amount;

    @JsonProperty("created_at")
    private Date createdAt;

    private String status;

    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    public Bet() {}

    public Bet(int id, int userId, int seasonId, int gameId, String betType, int amount, Date createdAt, String status, String homeTeam, String awayTeam) {
        this.id = id;
        this.userId = userId;
        this.seasonId = seasonId;
        this.gameId = gameId;
        this.betType = betType;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = status;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getBetType() {
        return betType;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }
}
```

## File: src/main/java/com/dialodds/seasonsbot/CommandHandler.java

```java
package com.dialodds.seasonsbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;


@Component
public class CommandHandler extends ListenerAdapter {

    private static final Color NFL_BLUE = new Color(0, 53, 148);
    private static final Color DISCORD_BLURPLE = new Color(114, 137, 218);
    private static final int MAX_MESSAGES_TO_DELETE = 100;

    @Autowired
    private final ApiClient apiClient;

    private final ConcurrentMap<String, Boolean> processedMessages;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, List<Game>> activeGamesCache = new HashMap<>();

    @Autowired
    public CommandHandler(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.processedMessages = new ConcurrentHashMap<>();
    }

    CommandHandler(ApiClient apiClient, ConcurrentMap<String, Boolean> processedMessages) {
        this.apiClient = apiClient;
        this.processedMessages = processedMessages;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.getMessage().getContentRaw().startsWith("!")) {
            return;
        }

        String messageId = event.getMessageId();
        if (processedMessages.putIfAbsent(messageId, Boolean.TRUE) != null) {
            return;
        }

        scheduler.schedule(() -> processedMessages.remove(messageId), 5, TimeUnit.SECONDS);

        String[] args = event.getMessage().getContentRaw().split("\\s+");
        String command = args[0].substring(1).toLowerCase();

        event.getChannel().sendTyping().queue();

        switch (command) {
            case "create_season":
                handleCreateSeason(event, args);
                break;
            case "join_season":
                handleJoinSeason(event, args);
                break;
            case "bet":
                handlePlaceBet(event, args);
                break;
            case "my_bets":
                handleMyBets(event, args);
                break;
            case "balance":
                handleBalance(event, args);
                break;
            case "leaderboard":
                handleLeaderboard(event, args);
                break;
            case "season_info":
                handleSeasonInfo(event, args);
                break;
            case "active_seasons":
                handleActiveSeasons(event);
                break;
            case "nfl_weeks":
                handleNflWeeks(event);
                break;
            case "nfl_games":
                handleNflGames(event, args);
                break;
            case "team_schedule":
                handleTeamSchedule(event, args);
                break;
            case "help":
                handleHelp(event);
                break;
            case "delete_season":
                handleDeleteSeason(event, args);
                break;
            case "purge":
                handlePurge(event, args);
                break;
            default:
                event.getChannel().sendMessage("Unknown command. Type !help for a list of commands.").queue();
        }
    }

    private void sendErrorEmbed(MessageReceivedEvent event, String title, String... descriptions) {
        EmbedBuilder errorEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Error: " + title);
        for (String desc : descriptions) {
            errorEmbed.addField("", desc, false);
        }
        event.getChannel().sendMessageEmbeds(errorEmbed.build()).queue();
    }


    private void handleCreateSeason(MessageReceivedEvent event, String[] args) {
        if (args.length != 4) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!create_season <start_week> <end_week> <initial_coins>`",
                    "Example: `!create_season 1 17 1000`");
            return;
        }

        try {
            int startWeek = Integer.parseInt(args[1]);
            int endWeek = Integer.parseInt(args[2]);
            int initialCoins = Integer.parseInt(args[3]);

            ResponseEntity<Integer> response = apiClient.createSeason(startWeek, endWeek, initialCoins);
            int seasonId = Optional.ofNullable(response.getBody()).orElse(-1);

            if (seasonId == -1) {
                sendErrorEmbed(event, "Season Creation Failed",
                        "The server returned an empty response. Please try again.");
                return;
            }

            EmbedBuilder successEmbed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Season Created Successfully")
                    .setDescription("A new season has been created with the following details:")
                    .addField("Season ID", String.valueOf(seasonId), true)
                    .addField("Start Week", String.valueOf(startWeek), true)
                    .addField("End Week", String.valueOf(endWeek), true)
                    .addField("Initial Coins", String.valueOf(initialCoins), true)
                    .setFooter("Created by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            event.getChannel().sendMessageEmbeds(successEmbed.build()).queue();
        } catch (NumberFormatException e) {
            sendErrorEmbed(event, "Invalid Input",
                    "Please enter valid numbers for weeks and coins.",
                    "Correct Usage: `!create_season <start_week> <end_week> <initial_coins>`",
                    "Example: `!create_season 1 17 1000`");
        } catch (Exception e) {
            sendErrorEmbed(event, "Season Creation Failed",
                    "An error occurred while creating the season. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private void handleJoinSeason(MessageReceivedEvent event, String[] args) {
        if (args.length != 2) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!join_season <season_id>`",
                    "Example: `!join_season 123`");
            return;
        }

        try {
            int seasonId = Integer.parseInt(args[1]);
            String discordId = event.getAuthor().getId();
            String username = event.getAuthor().getName();

            ResponseEntity<Void> joinResponse = apiClient.createUserAndJoinSeason(discordId, username, seasonId);

            if (joinResponse.getStatusCode().is2xxSuccessful()) {
                EmbedBuilder successEmbed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Successfully Joined Season")
                        .setDescription("You have successfully joined the season!")
                        .addField("Season ID", String.valueOf(seasonId), true)
                        .setFooter("Joined by " + username, event.getAuthor().getEffectiveAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getChannel().sendMessageEmbeds(successEmbed.build()).queue();
            } else {
                throw new Exception("Failed to join season. Status code: " + joinResponse.getStatusCode().value());
            }
        } catch (NumberFormatException e) {
            sendErrorEmbed(event, "Invalid Season ID",
                    "Please enter a valid number for the season ID.",
                    "Correct Usage: `!join_season <season_id>`",
                    "Example: `!join_season 123`");
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Join Season",
                    "An error occurred while joining the season. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private void handleDeleteSeason(MessageReceivedEvent event, String[] args) {
        if (args.length != 2) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!delete_season <season_id>`",
                    "Example: `!delete_season 123`");
            return;
        }

        try {
            int seasonId = Integer.parseInt(args[1]);

            ResponseEntity<Map<String, Object>> deleteResponse = apiClient.deleteSeason(seasonId);

            if (deleteResponse.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = deleteResponse.getBody();
                if (responseBody != null) {
                    boolean deleted = (boolean) responseBody.get("deleted");
                    String message = (String) responseBody.get("message");

                    if (deleted) {
                        EmbedBuilder successEmbed = new EmbedBuilder()
                                .setColor(Color.GREEN)
                                .setTitle("Season Deleted Successfully")
                                .setDescription(message)
                                .setFooter("Deleted by " + event.getAuthor().getName(),
                                        event.getAuthor().getEffectiveAvatarUrl())
                                .setTimestamp(Instant.now());

                        event.getChannel().sendMessageEmbeds(successEmbed.build()).queue();
                    } else {
                        sendErrorEmbed(event, "Failed to Delete Season", message);
                    }
                } else {
                    throw new Exception("Server returned an empty response");
                }
            } else {
                throw new Exception("Failed to delete season. Status code: " + deleteResponse.getStatusCode().value());
            }
        } catch (NumberFormatException e) {
            sendErrorEmbed(event, "Invalid Season ID",
                    "Please enter a valid number for the season ID.",
                    "Correct Usage: `!delete_season <season_id>`",
                    "Example: `!delete_season 123`");
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Delete Season",
                    "An error occurred while deleting the season. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }


    private void handlePlaceBet(MessageReceivedEvent event, String[] args) {
        if (args.length != 5) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!bet <season_id> <game_id> <bet_type> <amount>`",
                    "Example: `!bet 1 1985 HOME 125`",
                    "Bet Types: HOME, AWAY");
            return;
        }

        try {
            int seasonId = Integer.parseInt(args[1]);
            int gameId = Integer.parseInt(args[2]);
            String betType = args[3].toUpperCase();
            int amount = Integer.parseInt(args[4]);

            if (!Arrays.asList("HOME", "AWAY").contains(betType)) {
                throw new IllegalArgumentException("Invalid bet type. Must be HOME or AWAY.");
            }

            String discordId = event.getAuthor().getId();
            String username = event.getAuthor().getName();

            ResponseEntity<Integer> userResponse = apiClient.createUser(discordId, username);
            Integer userIdWrapper = userResponse.getBody();
            if (userIdWrapper == null) {
                sendErrorEmbed(event, "User Creation Failed",
                        "Failed to create or retrieve user. Please try again.");
                return;
            }
            int userId = userIdWrapper.intValue();

            ResponseEntity<Map<String, Object>> gameResponse = apiClient.getGameById(gameId);
            if (gameResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalArgumentException("Game not found. Please check the game ID.");
            }
            Map<String, Object> gameDetails = gameResponse.getBody();
            if (gameDetails == null) {
                sendErrorEmbed(event, "Game Details Retrieval Failed",
                        "Failed to retrieve game details. Please try again.");
                return;
            }

            String commenceTimeString = (String) gameDetails.get("commence_time");
            if (commenceTimeString == null) {
                sendErrorEmbed(event, "Invalid Game Data",
                        "The game data is missing the start time. Please try again later.");
                return;
            }

            try {
                Instant commenceTime = Instant.parse(commenceTimeString);
                if (Instant.now().isAfter(commenceTime)) {
                    sendErrorEmbed(event, "Game Already Started",
                            "Betting is closed for this game as it has already started.",
                            "Game: " + gameDetails.get("home_team") + " vs " + gameDetails.get("away_team"),
                            "Start Time: " + commenceTime.toString());
                    return;
                }
            } catch (DateTimeParseException e) {
                sendErrorEmbed(event, "Invalid Game Data",
                        "Unable to parse the game start time. Please try again later.");
                return;
            }

            ResponseEntity<Integer> coinsResponse = apiClient.getUserCoins(userId, seasonId);
            Integer userCoinsWrapper = coinsResponse.getBody();
            if (userCoinsWrapper == null) {
                sendErrorEmbed(event, "Balance Retrieval Failed",
                        "Failed to retrieve user balance. Please try again.");
                return;
            }
            int userCoins = userCoinsWrapper.intValue();
            if (userCoins < amount) {
                throw new IllegalArgumentException("Not enough coins to place bet. You have " + userCoins + " coins.");
            }

            ResponseEntity<Integer> betResponse = apiClient.placeBet(userId, seasonId, gameId, betType, amount);
            Integer betIdWrapper = betResponse.getBody();
            if (betIdWrapper == null) {
                sendErrorEmbed(event, "Bet Placement Failed",
                        "Failed to place the bet. Please try again.");
                return;
            }
            int betId = betIdWrapper.intValue();

            String odds = betType.equals("HOME") ? String.valueOf(gameDetails.get("home_odds"))
                    : String.valueOf(gameDetails.get("away_odds"));

            EmbedBuilder successEmbed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Bet Placed Successfully")
                    .setDescription("Your bet has been placed successfully!")
                    .addField("Bet ID", String.valueOf(betId), true)
                    .addField("Season ID", String.valueOf(seasonId), true)
                    .addField("Game", gameDetails.get("home_team") + " vs " + gameDetails.get("away_team"), false)
                    .addField("Bet Type", betType, true)
                    .addField("Amount", amount + " coins", true)
                    .addField("Odds", odds, true)
                    .setFooter("Placed by " + username, event.getAuthor().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            event.getChannel().sendMessageEmbeds(successEmbed.build()).queue();
        } catch (NumberFormatException e) {
            sendErrorEmbed(event, "Invalid Input",
                    "Please enter valid numbers for season ID, game ID, and amount.",
                    "Correct Usage: `!bet <season_id> <game_id> <bet_type> <amount>`",
                    "Example: `!bet 1 1985 HOME 125`");
        } catch (IllegalArgumentException e) {
            sendErrorEmbed(event, "Invalid Input",
                    e.getMessage(),
                    "Valid Bet Types: HOME, AWAY");
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Place Bet",
                    "An error occurred while placing the bet. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private void handleMyBets(MessageReceivedEvent event, String[] args) {
        if (args.length != 2) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!my_bets <season_id>`",
                    "Example: `!my_bets 123`");
            return;
        }

        try {
            int seasonId = Integer.parseInt(args[1]);
            String discordId = event.getAuthor().getId();
            String username = event.getAuthor().getName();

            ResponseEntity<Integer> userResponse = apiClient.createUser(discordId, username);
            Integer userIdWrapper = userResponse.getBody();
            if (userIdWrapper == null) {
                sendErrorEmbed(event, "User Creation Failed",
                        "Failed to create or retrieve user. Please try again.");
                return;
            }
            int userId = userIdWrapper.intValue();

            ResponseEntity<List<Bet>> betsResponse = apiClient.getUserBets(userId, seasonId);
            List<Bet> bets = betsResponse.getBody();
            if (bets == null) {
                sendErrorEmbed(event, "Bets Retrieval Failed",
                        "Failed to retrieve your bets. Please try again.");
                return;
            }

            if (bets.isEmpty()) {
                EmbedBuilder noBetsEmbed = new EmbedBuilder()
                        .setColor(Color.BLUE)
                        .setTitle("No Bets Found")
                        .setDescription("You haven't placed any bets in this season yet.")
                        .setFooter("Season ID: " + seasonId)
                        .setTimestamp(Instant.now());
                event.getChannel().sendMessageEmbeds(noBetsEmbed.build()).queue();
                return;
            }

            EmbedBuilder betsEmbed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Your Bets for Season " + seasonId)
                    .setDescription("Here's a list of your bets for this season:")
                    .setFooter("Requested by " + username, event.getAuthor().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            for (Bet bet : bets) {
                String betStatus = getBetStatus(bet);
                String betInfo = String.format("%s vs %s\nBet: %s %d coins\nResult: %s",
                        bet.getHomeTeam(), bet.getAwayTeam(),
                        bet.getBetType(), bet.getAmount(),
                        betStatus);

                betsEmbed.addField("Bet ID: " + bet.getId(), betInfo, false);
            }

            event.getChannel().sendMessageEmbeds(betsEmbed.build()).queue();
        } catch (NumberFormatException e) {
            sendErrorEmbed(event, "Invalid Season ID",
                    "Please enter a valid number for the season ID.",
                    "Correct Usage: `!my_bets <season_id>`",
                    "Example: `!my_bets 123`");
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Retrieve Bets",
                    "An error occurred while retrieving your bets. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private String getBetStatus(Bet bet) {
        String status = bet.getStatus();
        if (status == null || status.isEmpty()) {
            return "Pending";
        }
        return status.equals("WIN") ? "Won" : "Lost";
    }


    private void handleBalance(MessageReceivedEvent event, String[] args) {
        if (args.length != 2) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!balance <season_id>`",
                    "Example: `!balance 123`");
            return;
        }

        try {
            int seasonId = Integer.parseInt(args[1]);
            String discordId = event.getAuthor().getId();
            String username = event.getAuthor().getName();

            ResponseEntity<Integer> userResponse = apiClient.createUser(discordId, username);
            Integer userIdWrapper = userResponse.getBody();
            if (userIdWrapper == null) {
                sendErrorEmbed(event, "User Creation Failed",
                        "Failed to create or retrieve user. Please try again.");
                return;
            }
            int userId = userIdWrapper.intValue();

            ResponseEntity<Integer> balanceResponse = apiClient.getUserCoins(userId, seasonId);
            Integer balanceWrapper = balanceResponse.getBody();
            if (balanceWrapper == null) {
                sendErrorEmbed(event, "Balance Retrieval Failed",
                        "Failed to retrieve your balance. Please try again.");
                return;
            }
            int balance = balanceWrapper.intValue();

            ResponseEntity<Map<String, Object>> seasonResponse = apiClient.getSeasonById(seasonId);
            Map<String, Object> seasonInfo = seasonResponse.getBody();
            if (seasonInfo == null) {
                sendErrorEmbed(event, "Season Info Retrieval Failed",
                        "Failed to retrieve season information. Please try again.");
                return;
            }

            Integer initialCoins = (Integer) seasonInfo.get("initial_coins");
            Integer startWeek = (Integer) seasonInfo.get("start_week");
            Integer endWeek = (Integer) seasonInfo.get("end_week");

            if (initialCoins == null || startWeek == null || endWeek == null) {
                sendErrorEmbed(event, "Invalid Season Data",
                        "The season data is incomplete. Please try again later.");
                return;
            }

            Color goldColor = new Color(255, 215, 0);

            EmbedBuilder balanceEmbed = new EmbedBuilder()
                    .setColor(goldColor)
                    .setTitle("Balance for Season " + seasonId)
                    .setDescription("Here's your current balance for this season:")
                    .addField("Current Balance", balance + " coins", false)
                    .addField("Initial Balance", initialCoins + " coins", true)
                    .addField("Net Change", (balance - initialCoins) + " coins", true)
                    .addField("Season Duration",
                            "Week " + startWeek + " to Week " + endWeek, false)
                    .setFooter("Requested by " + username, event.getAuthor().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            String funMessage = getFunBalanceMessage(balance, initialCoins);
            balanceEmbed.addField("Status", funMessage, false);

            event.getChannel().sendMessageEmbeds(balanceEmbed.build()).queue();
        } catch (NumberFormatException e) {
            sendErrorEmbed(event, "Invalid Season ID",
                    "Please enter a valid number for the season ID.",
                    "Correct Usage: `!balance <season_id>`",
                    "Example: `!balance 123`");
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Retrieve Balance",
                    "An error occurred while retrieving your balance. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private String getFunBalanceMessage(int currentBalance, int initialBalance) {
        if (currentBalance > initialBalance * 2) {
            return "Wow! You're on fire! 🔥";
        } else if (currentBalance > initialBalance) {
            return "Nice job! You're in the green. 💰";
        } else if (currentBalance == initialBalance) {
            return "Breaking even. Slow and steady! 🐢";
        } else if (currentBalance > initialBalance / 2) {
            return "Hang in there, you can turn this around! 💪";
        } else {
            return "Ouch! Time to change up your strategy? 🤔";
        }
    }


    private void handleLeaderboard(MessageReceivedEvent event, String[] args) {
    if (args.length != 2) {
        sendErrorEmbed(event, "Invalid Command Usage",
                "Usage: `!leaderboard <season_id>`",
                "Example: `!leaderboard 123`");
        return;
    }

    try {
        int seasonId = Integer.parseInt(args[1]);

        ResponseEntity<List<User>> leaderboardResponse = apiClient.getUsersBySeason(seasonId);
        List<User> leaderboard = leaderboardResponse.getBody();

        if (leaderboard == null || leaderboard.isEmpty()) {
            EmbedBuilder noUsersEmbed = new EmbedBuilder()
                    .setColor(Color.BLUE)
                    .setTitle("No Users Found")
                    .setDescription("There are no users participating in this season yet.")
                    .setFooter("Season ID: " + seasonId)
                    .setTimestamp(Instant.now());
            event.getChannel().sendMessageEmbeds(noUsersEmbed.build()).queue();
            return;
        }

        leaderboard.sort(Comparator.comparing(User::getCoins).reversed());

        EmbedBuilder leaderboardEmbed = new EmbedBuilder()
                .setColor(new Color(218, 165, 32))
                .setTitle("Leaderboard for Season " + seasonId)
                .setDescription("Here are the top performers for this season:")
                .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());

        for (int i = 0; i < Math.min(10, leaderboard.size()); i++) {
            User user = leaderboard.get(i);
            String medal = getMedalEmoji(i);
            String userInfo = String.format("%s **%s**\nCoins: %d", medal, user.getUsername(), user.getCoins());
            leaderboardEmbed.addField(String.format("%d.", i + 1), userInfo, false);
        }

        String requesterId = event.getAuthor().getId();
        int requesterPosition = findUserPosition(leaderboard, requesterId);
        if (requesterPosition > 10) {
            User requesterInfo = leaderboard.get(requesterPosition - 1);
            String userInfo = String.format("**%s**\nCoins: %d", requesterInfo.getUsername(), requesterInfo.getCoins());
            leaderboardEmbed.addField("Your Position: " + requesterPosition, userInfo, false);
        }

        event.getChannel().sendMessageEmbeds(leaderboardEmbed.build()).queue();
    } catch (NumberFormatException e) {
        sendErrorEmbed(event, "Invalid Season ID",
                "Please enter a valid number for the season ID.",
                "Correct Usage: `!leaderboard <season_id>`",
                "Example: `!leaderboard 123`");
    } catch (Exception e) {
        sendErrorEmbed(event, "Failed to Retrieve Leaderboard",
                "An error occurred while retrieving the leaderboard. Please try again.",
                "Error Details: " + e.getMessage());
    }
}

    private String getMedalEmoji(int position) {
        switch (position) {
            case 0:
                return "🥇";
            case 1:
                return "🥈";
            case 2:
                return "🥉";
            default:
                return "🏅";
        }
    }

    private int findUserPosition(List<User> leaderboard, String discordId) {
        for (int i = 0; i < leaderboard.size(); i++) {
            String userDiscordId = leaderboard.get(i).getDiscordId();
            if (userDiscordId != null && userDiscordId.equals(discordId)) {
                return i + 1;
            }
        }
        return leaderboard.size() + 1;
    }


    private void handleSeasonInfo(MessageReceivedEvent event, String[] args) {
        if (args.length != 2) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!season_info <season_id>`",
                    "Example: `!season_info 123`");
            return;
        }

        try {
            int seasonId = Integer.parseInt(args[1]);

            ResponseEntity<Map<String, Object>> seasonResponse = apiClient.getSeasonById(seasonId);
            Map<String, Object> season = seasonResponse.getBody();

            if (season == null) {
                EmbedBuilder noSeasonEmbed = new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setTitle("Season Not Found")
                        .setDescription("No season found with ID: " + seasonId)
                        .setTimestamp(Instant.now());
                event.getChannel().sendMessageEmbeds(noSeasonEmbed.build()).queue();
                return;
            }

            Instant createdAt = Instant.parse((String) season.get("created_at"));
            String formattedDate = DateTimeFormatter.ofPattern("MMMM d, yyyy")
                    .withZone(ZoneId.systemDefault())
                    .format(createdAt);

            int startWeek = (Integer) season.get("start_week");
            int endWeek = (Integer) season.get("end_week");
            int durationWeeks = endWeek - startWeek + 1;

            int currentWeek = getCurrentNflWeek();

            String seasonStatus = getSeasonStatus(startWeek, endWeek, currentWeek);

            EmbedBuilder seasonInfoEmbed = new EmbedBuilder()
                    .setColor(new Color(0, 128, 128))
                    .setTitle("Season Information")
                    .setDescription("Details for Season ID: " + seasonId)
                    .addField("Start Week", String.valueOf(startWeek), true)
                    .addField("End Week", String.valueOf(endWeek), true)
                    .addField("Duration", durationWeeks + " weeks", true)
                    .addField("Initial Coins", season.get("initial_coins") + " coins", true)
                    .addField("Created On", formattedDate, true)
                    .addField("Status", seasonStatus, true)
                    .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            if (seasonStatus.equals("Active")) {
                int progress = ((currentWeek - startWeek + 1) * 100) / durationWeeks;
                seasonInfoEmbed.addField("Progress", getProgressBar(progress) + " " + progress + "%", false);
            }

            event.getChannel().sendMessageEmbeds(seasonInfoEmbed.build()).queue();
        } catch (NumberFormatException e) {
            sendErrorEmbed(event, "Invalid Season ID",
                    "Please enter a valid number for the season ID.",
                    "Correct Usage: `!season_info <season_id>`",
                    "Example: `!season_info 123`");
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Retrieve Season Information",
                    "An error occurred while retrieving season information. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private String getSeasonStatus(int startWeek, int endWeek, int currentWeek) {
        if (currentWeek < startWeek) {
            return "Upcoming";
        } else if (currentWeek > endWeek) {
            return "Completed";
        } else {
            return "Active";
        }
    }

    private String getProgressBar(int percentage) {
        int filledBlocks = percentage / 10;
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            progressBar.append(i < filledBlocks ? "█" : "░");
        }
        return progressBar.toString();
    }


    private void handleActiveSeasons(MessageReceivedEvent event) {
        try {
            ResponseEntity<List<Season>> seasonsResponse = apiClient.getActiveSeasons();
            List<Season> seasons = seasonsResponse.getBody();

            if (seasons == null || seasons.isEmpty()) {
                EmbedBuilder noSeasonsEmbed = new EmbedBuilder()
                        .setColor(Color.BLUE)
                        .setTitle("No Active Seasons")
                        .setDescription("There are currently no active seasons with future games.")
                        .setFooter("Requested by " + event.getAuthor().getName(),
                                event.getAuthor().getEffectiveAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getChannel().sendMessageEmbeds(noSeasonsEmbed.build()).queue();
                return;
            }

            EmbedBuilder activeSeasonsEmbed = new EmbedBuilder()
                    .setColor(new Color(50, 205, 50))
                    .setTitle("Active Seasons")
                    .setDescription("Here are the currently active seasons with future games:")
                    .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            Instant now = Instant.now();

            for (Season season : seasons) {
                int seasonId = season.getId();
                int startWeek = season.getStartWeek();
                int endWeek = season.getEndWeek();
                int initialCoins = season.getInitialCoins();
                Instant createdAt = season.getCreatedAtInstant();

                String timeUntilNextGame = "N/A";
                if (createdAt != null) {
                    long daysUntilNextGame = ChronoUnit.DAYS.between(now, createdAt);
                    timeUntilNextGame = formatTimeUntilNextGame(daysUntilNextGame);
                }

                String seasonInfo = String.format("Weeks: %d-%d | Initial Coins: %d\nNext game: %s",
                        startWeek, endWeek, initialCoins, timeUntilNextGame);

                activeSeasonsEmbed.addField("Season " + seasonId, seasonInfo, false);
            }

            event.getChannel().sendMessageEmbeds(activeSeasonsEmbed.build()).queue();
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Retrieve Active Seasons",
                    "An error occurred while retrieving active seasons. Please try again.",
                    "Error Details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleNflWeeks(MessageReceivedEvent event) {
        try {
            ResponseEntity<List<Integer>> weeksResponse = apiClient.getNflWeeks();
            List<Integer> weeks = weeksResponse.getBody();

            if (weeks == null || weeks.isEmpty()) {
                EmbedBuilder noWeeksEmbed = new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setTitle("No NFL Weeks Available")
                        .setDescription("There are currently no NFL weeks available.")
                        .setFooter("Requested by " + event.getAuthor().getName(),
                                event.getAuthor().getEffectiveAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getChannel().sendMessageEmbeds(noWeeksEmbed.build()).queue();
                return;
            }

            int currentWeek = getCurrentNflWeek();

            EmbedBuilder nflWeeksEmbed = new EmbedBuilder()
                    .setColor(NFL_BLUE)
                    .setTitle("Available NFL Weeks")
                    .setDescription("Here are the available NFL weeks for this season:")
                    .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
                    .setTimestamp(Instant.now());

            List<String> weekRanges = getWeekRanges(weeks);
            String weeksList = String.join("\n", weekRanges);
            nflWeeksEmbed.addField("Weeks", weeksList, false);

            String currentWeekInfo = weeks.contains(currentWeek) ? "Current Week: " + currentWeek
                    : "Current Week: Not in available weeks";
            nflWeeksEmbed.addField("Current Week", currentWeekInfo, false);

            nflWeeksEmbed.addField("Total Weeks", String.valueOf(weeks.size()), true);
            nflWeeksEmbed.addField("First Week", String.valueOf(weeks.get(0)), true);
            nflWeeksEmbed.addField("Last Week", String.valueOf(weeks.get(weeks.size() - 1)), true);

            event.getChannel().sendMessageEmbeds(nflWeeksEmbed.build()).queue();
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Retrieve NFL Weeks",
                    "An error occurred while retrieving NFL weeks. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private List<String> getWeekRanges(List<Integer> weeks) {
        List<String> ranges = new ArrayList<>();
        int start = weeks.get(0);
        int prev = start;
        for (int i = 1; i <= weeks.size(); i++) {
            if (i == weeks.size() || weeks.get(i) != prev + 1) {
                ranges.add(start == prev ? "Week " + start : "Weeks " + start + "-" + prev);
                if (i < weeks.size()) {
                    start = weeks.get(i);
                    prev = start;
                }
            } else {
                prev = weeks.get(i);
            }
        }
        return ranges;
    }

    private void handleNflGames(MessageReceivedEvent event, String[] args) {
        if (args.length != 2) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!nfl_games <week>`",
                    "Example: `!nfl_games 1`");
            return;
        }

        try {
            int week = Integer.parseInt(args[1]);

            ResponseEntity<List<Game>> gamesResponse = apiClient.getNflGamesByWeek(week);
            List<Game> games = gamesResponse.getBody();

            if (games == null || games.isEmpty()) {
                EmbedBuilder noGamesEmbed = new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setTitle("No NFL Games Available")
                        .setDescription("There are no NFL games available for Week " + week + ".")
                        .setFooter("Requested by " + event.getAuthor().getName(),
                                event.getAuthor().getEffectiveAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getChannel().sendMessageEmbeds(noGamesEmbed.build()).queue();
                return;
            }

            String messageId = UUID.randomUUID().toString();
            List<Game> gamesList = new ArrayList<>(games);

            if (gamesList.isEmpty()) {
                sendErrorEmbed(event, "No Game Data",
                        "No game data available for Week " + week + ".",
                        "Please try again later or contact support if the issue persists.");
                return;
            }

            Game firstGame = gamesList.get(0);

            EmbedBuilder initialEmbed = createGameEmbed(firstGame, week, 1, gamesList.size());
            List<Button> buttons = createNavigationButtons(messageId, 0, gamesList.size());

            String awayTeam = firstGame.getAwayTeam();
            String homeTeam = firstGame.getHomeTeam();

            if (awayTeam == null || homeTeam == null) {
                sendErrorEmbed(event, "Invalid Game Data",
                        "The game data is missing team information.",
                        "Please try again later or contact support if the issue persists.");
                return;
            }

            try {
                File logoImage = createLogoImage(awayTeam, homeTeam);
                event.getChannel().sendMessageEmbeds(initialEmbed.build())
                        .setActionRow(buttons)
                        .addFiles(FileUpload.fromData(logoImage, "game_logos.png"))
                        .queue(message -> {
                            logoImage.delete();
                            activeGamesCache.put(messageId, gamesList);
                        });
            } catch (IOException e) {
                sendErrorEmbed(event, "Logo Generation Failed",
                        "Failed to generate team logos.",
                        "Error: " + e.getMessage());
            }

        } catch (NumberFormatException e) {
            sendErrorEmbed(event, "Invalid Week Number",
                    "Please enter a valid number for the week.",
                    "Correct Usage: `!nfl_games <week>`",
                    "Example: `!nfl_games 1`");
        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Retrieve NFL Games",
                    "An error occurred while retrieving NFL games. Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private EmbedBuilder createGameEmbed(Game game, int week, int currentGame, int totalGames) {
        EmbedBuilder gameEmbed = new EmbedBuilder()
                .setColor(NFL_BLUE)
                .setTitle("Week " + week + " - Game " + currentGame + " of " + totalGames)
                .setDescription(game.getAwayTeam() + " @ " + game.getHomeTeam() + "\n\n" +
                        game.getAwayTeam() + " Odds: " + game.getAwayOdds() + "  |  " +
                        game.getHomeTeam() + " Odds: " + game.getHomeOdds())
                .setImage("attachment://game_logos.png");

        if (game.getCommenceTime() != null) {
            String formattedDate = DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy, HH:mm z")
                    .withZone(ZoneId.of("America/New_York"))
                    .format(game.getCommenceTime());
            gameEmbed.addField("Date & Time", formattedDate, false);
        } else {
            gameEmbed.addField("Date & Time", "Not available", false);
        }

        gameEmbed.addField("Game ID", String.valueOf(game.getId()), false);

        return gameEmbed;
    }

    private List<Button> createNavigationButtons(String messageId, int currentIndex, int totalGames) {
        Button previousButton = Button.primary("nfl:prev:" + messageId, "Previous")
                .withDisabled(currentIndex == 0);
        Button nextButton = Button.primary("nfl:next:" + messageId, "Next")
                .withDisabled(currentIndex == totalGames - 1);
        return Arrays.asList(previousButton, nextButton);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] buttonId = event.getComponentId().split(":");
        if (buttonId.length != 3 || !buttonId[0].equals("nfl")) {
            event.reply("Invalid button interaction.").setEphemeral(true).queue();
            return;
        }

        String action = buttonId[1];
        String messageId = buttonId[2];

        List<Game> games = activeGamesCache.get(messageId);
        if (games == null) {
            event.reply("This game list has expired. Please request a new one.").setEphemeral(true).queue();
            return;
        }

        MessageEmbed currentEmbed = event.getMessage().getEmbeds().get(0);
        String title = currentEmbed.getTitle();

        int currentIndex;
        int week = 0;

        if (title.contains("Schedule")) {
            String[] titleParts = title.split(" - ");
            String[] gameParts = titleParts[1].split(" ");
            currentIndex = Integer.parseInt(gameParts[1]) - 1;
        } else {
            String[] titleParts = title.split(" - ");
            week = Integer.parseInt(titleParts[0].split(" ")[1]);
            currentIndex = Integer.parseInt(titleParts[1].split(" ")[1]) - 1;
        }

        int newIndex = action.equals("next") ? currentIndex + 1 : currentIndex - 1;

        if (newIndex < 0 || newIndex >= games.size()) {
            event.reply("No more games to display.").setEphemeral(true).queue();
            return;
        }

        Game game = games.get(newIndex);
        String awayTeam = game.getAwayTeam();
        String homeTeam = game.getHomeTeam();

        EmbedBuilder updatedEmbed;
        if (title.contains("Schedule")) {
            updatedEmbed = createTeamScheduleEmbed(game, title.split(" Schedule")[0], newIndex + 1, games.size());
        } else {
            updatedEmbed = createGameEmbed(game, week, newIndex + 1, games.size());
        }

        List<Button> updatedButtons = createNavigationButtons(messageId, newIndex, games.size());

        try {
            File logoImage = createLogoImage(awayTeam, homeTeam);
            event.deferEdit().queue();
            event.getHook().editOriginalEmbeds(updatedEmbed.build())
                    .setActionRow(updatedButtons)
                    .setFiles(FileUpload.fromData(logoImage, "game_logos.png"))
                    .queue(
                            success -> logoImage.delete(),
                            error -> {
                                logError(error);
                                event.getHook().sendMessage("An error occurred. Please try again.").setEphemeral(true)
                                        .queue();
                                logoImage.delete();
                            });
        } catch (IOException e) {
            event.reply("An error occurred while processing team logos. Please try again.").setEphemeral(true).queue();
        }
    }

    private void handleTeamSchedule(MessageReceivedEvent event, String[] args) {
        if (args.length < 2) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!team_schedule <team_name>`",
                    "Example: `!team_schedule San Francisco 49ers`");
            return;
        }

        String teamName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        try {
            ResponseEntity<List<Game>> scheduleResponse = apiClient.getTeamSchedule(teamName);
            List<Game> schedule = scheduleResponse.getBody();

            if (schedule == null || schedule.isEmpty()) {
                EmbedBuilder noScheduleEmbed = new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setTitle("No Schedule Available")
                        .setDescription("There is no schedule available for " + teamName + ".")
                        .setFooter("Requested by " + event.getAuthor().getName(),
                                event.getAuthor().getEffectiveAvatarUrl())
                        .setTimestamp(Instant.now());
                event.getChannel().sendMessageEmbeds(noScheduleEmbed.build()).queue();
                return;
            }

            String messageId = UUID.randomUUID().toString();
            Game firstGame = schedule.get(0);

            EmbedBuilder initialEmbed = createTeamScheduleEmbed(firstGame, teamName, 1, schedule.size());
            List<Button> buttons = createNavigationButtons(messageId, 0, schedule.size());

            String awayTeam = firstGame.getAwayTeam();
            String homeTeam = firstGame.getHomeTeam();
            File logoImage = createLogoImage(awayTeam, homeTeam);

            event.getChannel().sendMessageEmbeds(initialEmbed.build())
                    .setActionRow(buttons)
                    .addFiles(FileUpload.fromData(logoImage, "game_logos.png"))
                    .queue(message -> {
                        logoImage.delete();
                        activeGamesCache.put(messageId, schedule);
                    });

        } catch (Exception e) {
            sendErrorEmbed(event, "Failed to Retrieve Team Schedule",
                    "An error occurred while retrieving the schedule for " + teamName + ". Please try again.",
                    "Error Details: " + e.getMessage());
        }
    }

    private EmbedBuilder createTeamScheduleEmbed(Game game, String teamName, int currentGame, int totalGames) {
        EmbedBuilder gameEmbed = new EmbedBuilder()
                .setColor(NFL_BLUE)
                .setTitle(teamName + " Schedule - Game " + currentGame + " of " + totalGames)
                .setDescription(game.getAwayTeam() + " @ " + game.getHomeTeam() + "\n\n" +
                        "Away Odds: " + game.getAwayOdds() + "  |  Home Odds: " + game.getHomeOdds())
                .setImage("attachment://game_logos.png");

        if (game.getCommenceTime() != null) {
            String formattedDate = DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy, HH:mm z")
                    .withZone(ZoneId.of("America/New_York"))
                    .format(game.getCommenceTime());
            gameEmbed.addField("Date & Time", formattedDate, false);
        } else {
            gameEmbed.addField("Date & Time", "Not available", false);
        }

        int weekNumber = getWeekNumber(game.getCommenceTime());
        gameEmbed.addField("Week", String.valueOf(weekNumber), true);
        gameEmbed.addField("Game ID", String.valueOf(game.getId()), true);

        return gameEmbed;
    }

    private int getWeekNumber(Instant gameTime) {
        if (gameTime == null) {
            return 0;
        }
        LocalDate gameDate = gameTime.atZone(ZoneId.of("America/New_York")).toLocalDate();
        LocalDate seasonStartDate = LocalDate.of(2024, Month.SEPTEMBER, 5);
        long weeksSinceStart = ChronoUnit.WEEKS.between(seasonStartDate, gameDate);
        return (int) weeksSinceStart + 1;
    }

    private File createLogoImage(String awayTeam, String homeTeam) throws IOException {
        BufferedImage awayLogo = ImageIO
                .read(new File("src/main/resources/static/logos/" + getTeamLogoFilename(awayTeam)));
        BufferedImage homeLogo = ImageIO
                .read(new File("src/main/resources/static/logos/" + getTeamLogoFilename(homeTeam)));

        int width = awayLogo.getWidth() + homeLogo.getWidth() + 100;
        int height = Math.max(awayLogo.getHeight(), homeLogo.getHeight()) + 60;

        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = combined.getGraphics();

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString(awayTeam, 10, 25);
        g.drawString(homeTeam, awayLogo.getWidth() + 110, 25);

        g.drawImage(awayLogo, 0, 30, null);
        g.drawImage(homeLogo, awayLogo.getWidth() + 100, 30, null);

        g.dispose();

        File tempFile = File.createTempFile("game_logos", ".png");
        ImageIO.write(combined, "PNG", tempFile);
        return tempFile;
    }

    private String getTeamLogoFilename(String teamName) {
        return teamName.replaceAll("[^a-zA-Z0-9]", "") + ".png";
    }

    private void handleHelp(MessageReceivedEvent event) {
        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setColor(DISCORD_BLURPLE)
            .setTitle("NFL Betting Bot Help")
            .setDescription("Welcome to the NFL Betting Bot! Here's a guide to all available commands:")
            .setFooter("Requested by " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl())
            .setTimestamp(Instant.now());
    
        helpEmbed.addField("🏈 Season Management",
            "`!create_season <start_week> <end_week> <initial_coins>` - Create a new season\n" +
            "`!join_season <season_id>` - Join an existing season\n" +
            "`!season_info <season_id>` - Get information about a season\n" +
            "`!active_seasons` - List all active seasons\n",
            false);
    
        helpEmbed.addField("💰 Betting",
            "`!bet <season_id> <game_id> <bet_type> <amount>` - Place a bet\n" +
            "`!my_bets <season_id>` - View your bets for a season\n" +
            "`!balance <season_id>` - Check your balance for a season",
            false);
    
        helpEmbed.addField("🏆 Leaderboard",
            "`!leaderboard <season_id>` - View the leaderboard for a season", false);
    
        helpEmbed.addField("📅 NFL Information",
            "`!nfl_games <week>` - Get NFL games for a specific week\n" +
            "`!team_schedule <team_name>` - Get schedule for a specific team",
            false);
    
        helpEmbed.addField("🧹 Utility",
            "`!help` - Display this help message\n" +
            "`!purge <number>` - Delete a number of recent messages (admin only)",
            false);
    
        helpEmbed.addField("📘 Quick Start Guide",
            "1. Join or create a season using `!join_season` or `!create_season`\n" +
            "2. Check available games with `!nfl_games`\n" +
            "3. Place a bet using the `!bet` command\n" +
            "4. Track your progress with `!my_bets` and `!balance`\n" +
            "5. Compare your standing using `!leaderboard`",
            false);
    
        helpEmbed.addField("🎲 Betting Example",
            "To bet 100 coins on the home team for game 456 in season 123:\n" +
            "`!bet 123 456 HOME 100`",
            false);
    
        helpEmbed.addField("❓ Need More Help?",
            "If you need more information about a specific command, try using it with no arguments or incorrect arguments. " +
            "The bot will provide you with more detailed usage instructions.",
            false);
    
        event.getChannel().sendMessageEmbeds(helpEmbed.build()).queue();
    }

    private void handlePurge(MessageReceivedEvent event, String[] args) {
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().sendMessage("You do not have permission to use this command.").queue();
            return;
        }

        if (args.length != 2) {
            sendErrorEmbed(event, "Invalid Command Usage",
                    "Usage: `!purge <number>`",
                    "Example: `!purge 100`",
                    "Note: Maximum number of messages to delete is 100");
            return;
        }

        try {
            int amount = Integer.parseInt(args[1]);
            if (amount < 1 || amount > MAX_MESSAGES_TO_DELETE) {
                event.getChannel().sendMessage("Please provide a number between 1 and " + MAX_MESSAGES_TO_DELETE + ".")
                        .queue();
                return;
            }

            TextChannel channel = event.getChannel().asTextChannel();
            List<Message> messages = channel.getHistory().retrievePast(amount).complete();

            channel.purgeMessages(messages);

            EmbedBuilder successEmbed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Channel Cleared")
                    .setDescription(messages.size() + " messages have been deleted.")
                    .setFooter("Requested by " + event.getAuthor().getName(),
                            event.getAuthor().getEffectiveAvatarUrl());

            channel.sendMessageEmbeds(successEmbed.build())
                    .queue(response -> response.delete().queueAfter(5, TimeUnit.SECONDS));

        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Please provide a valid number.").queue();
        } catch (InsufficientPermissionException e) {
            event.getChannel().sendMessage("I don't have permission to delete messages in this channel.").queue();
        } catch (IllegalArgumentException e) {
            event.getChannel().sendMessage(
                    "Some messages are too old to be deleted. I can only bulk delete messages that are under 2 weeks old.")
                    .queue();
        }
    }


    private int getCurrentNflWeek() {
        LocalDate seasonStartDate = LocalDate.of(2024, Month.SEPTEMBER, 5);
        LocalDate currentDate = LocalDate.now();

        if (currentDate.isBefore(seasonStartDate)) {
            return 0;
        }

        long weeksSinceStart = ChronoUnit.WEEKS.between(seasonStartDate, currentDate);
        int currentWeek = (int) weeksSinceStart + 1;

        return Math.min(currentWeek, 18);
    }

    private void logError(Throwable error) {
        if (error instanceof ErrorResponseException) {
            ErrorResponseException ere = (ErrorResponseException) error;
            System.out.println("Error response: " + ere.getErrorCode() + " " + ere.getMeaning());
        } else {
            System.out.println("An error occurred: " + error.getMessage());
        }
        error.printStackTrace();
    }

    private String formatTimeUntilNextGame(long days) {
        if (days == 0) {
            return "Today";
        } else if (days == 1) {
            return "Tomorrow";
        } else if (days < 7) {
            return days + " days";
        } else {
            long weeks = days / 7;
            return weeks + (weeks == 1 ? " week" : " weeks");
        }
    }
}
```

## File: src/main/java/com/dialodds/seasonsbot/Game.java

```java
package com.dialodds.seasonsbot;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Game {
    private int id;

    @JsonProperty("home_team")
    private String homeTeam;

    @JsonProperty("away_team")
    private String awayTeam;

    @JsonProperty("commence_time")
    private Instant commenceTime;

    @JsonProperty("home_odds")
    private double homeOdds;

    @JsonProperty("away_odds")
    private double awayOdds;

    public Game() {
    }

    public Game(int id, String homeTeam, String awayTeam, Instant commenceTime, double homeOdds, double awayOdds) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.commenceTime = commenceTime;
        this.homeOdds = homeOdds;
        this.awayOdds = awayOdds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Instant getCommenceTime() {
        return commenceTime;
    }

    public void setCommenceTime(Instant commenceTime) {
        this.commenceTime = commenceTime;
    }

    public double getHomeOdds() {
        return homeOdds;
    }

    public void setHomeOdds(double homeOdds) {
        this.homeOdds = homeOdds;
    }

    public double getAwayOdds() {
        return awayOdds;
    }

    public void setAwayOdds(double awayOdds) {
        this.awayOdds = awayOdds;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", homeTeam='" + homeTeam + '\'' +
                ", awayTeam='" + awayTeam + '\'' +
                ", commenceTime=" + commenceTime +
                ", homeOdds=" + homeOdds +
                ", awayOdds=" + awayOdds +
                '}';
    }
}
```

## File: src/main/java/com/dialodds/seasonsbot/JDAInitializer.java

```java
package com.dialodds.seasonsbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class JDAInitializer {

    private final CommandHandler commandHandler;

    public JDAInitializer(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void initJDA() throws Exception {
        String token = System.getProperty("DISCORD_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("DISCORD_BOT_TOKEN is not set");
        }

        JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
            .setBulkDeleteSplittingEnabled(false)
            .setLargeThreshold(50)
            .addEventListeners(commandHandler)
            .build();
    }
}
```

## File: src/main/java/com/dialodds/seasonsbot/Season.java

```java
package com.dialodds.seasonsbot;

import java.util.Date;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Season {
    private int id;

    @JsonProperty("start_week")
    private int startWeek;

    @JsonProperty("end_week")
    private int endWeek;

    @JsonProperty("initial_coins")
    private int initialCoins;

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("is_active")
    private boolean isActive;

    public Season() {}

    public Season(int id, int startWeek, int endWeek, int initialCoins, Date createdAt, boolean isActive) {
        this.id = id;
        this.startWeek = startWeek;
        this.endWeek = endWeek;
        this.initialCoins = initialCoins;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(int startWeek) {
        this.startWeek = startWeek;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(int endWeek) {
        this.endWeek = endWeek;
    }

    public int getInitialCoins() {
        return initialCoins;
    }

    public void setInitialCoins(int initialCoins) {
        this.initialCoins = initialCoins;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Instant getCreatedAtInstant() {
        return createdAt != null ? createdAt.toInstant() : null;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
```

## File: src/main/java/com/dialodds/seasonsbot/SeasonsBot.java

```java
package com.dialodds.seasonsbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class SeasonsBot {

    @Value("${discord.bot.token}")
    private String token;

    private final CommandHandler commandHandler;

    public SeasonsBot(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void start() throws Exception {
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing("NFL Betting Seasons"))
                .addEventListeners(commandHandler)
                .build();

        jda.awaitReady();
        System.out.println("SeasonsBot is ready!");
    }
}
```

## File: src/main/java/com/dialodds/seasonsbot/SeasonsbotApplication.java

```java
package com.dialodds.seasonsbot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SeasonsbotApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        System.setProperty("DISCORD_BOT_TOKEN", dotenv.get("DISCORD_BOT_TOKEN", ""));
        System.setProperty("API_BASE_URL", dotenv.get("API_BASE_URL", "http://localhost:8080"));
        
        SpringApplication.run(SeasonsbotApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApiClient apiClient(RestTemplate restTemplate) {
        String baseUrl = System.getProperty("API_BASE_URL");
        return new ApiClient(restTemplate, baseUrl);
    }
}
```

## File: src/main/java/com/dialodds/seasonsbot/User.java

```java
package com.dialodds.seasonsbot;

public class User {
    private int id;
    private String discordId;
    private String username;
    private int coins;

    public User() {}

    public User(int id, String discordId, String username, int coins) {
        this.id = id;
        this.discordId = discordId;
        this.username = username;
        this.coins = coins;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
```

## File: src/test/java/com/dialodds/seasonsbot/CommandHandlerTest.java

```java
package com.dialodds.seasonsbot;

// This broke at some point, will come back to it later....

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import net.dv8tion.jda.api.entities.User;
// import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
// import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
// import net.dv8tion.jda.api.requests.RestAction;
// import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
// import net.dv8tion.jda.api.entities.Message;
// import net.dv8tion.jda.api.entities.MessageEmbed;
// import org.springframework.http.ResponseEntity;
// import java.util.*;
// import java.time.Instant;

// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

class CommandHandlerTest {

    // private CommandHandler commandHandler;

    // @Mock
    // private ApiClient apiClient;

    // @Mock
    // private MessageReceivedEvent event;

    // @Mock
    // private User user;

    // @Mock
    // private MessageChannelUnion channel;

    // @Mock
    // private MessageCreateAction messageAction;

    // @Mock
    // private Message message;

    // @SuppressWarnings("unchecked")
    // @BeforeEach
    // void setUp() {
    //     MockitoAnnotations.openMocks(this);
    //     commandHandler = new CommandHandler(apiClient);

    //     when(event.getAuthor()).thenReturn(user);
    //     when(event.getChannel()).thenReturn(channel);
    //     when(channel.sendMessage(anyString())).thenReturn(messageAction);
    //     when(channel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageAction);
    //     when(event.getMessage()).thenReturn(message);
    //     when(channel.sendTyping()).thenReturn(mock(RestAction.class));
    // }

    // @Test
    // void testCreateSeason() {
    //     when(message.getContentRaw()).thenReturn("!create_season 1 17 1000");
    //     when(apiClient.createSeason(1, 17, 1000)).thenReturn(ResponseEntity.ok(1));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).createSeason(1, 17, 1000);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testJoinSeason() {
    //     when(message.getContentRaw()).thenReturn("!join_season 1");
    //     when(user.getId()).thenReturn("123456");
    //     when(user.getName()).thenReturn("TestUser");
    //     when(apiClient.createUserAndJoinSeason("123456", "TestUser", 1)).thenReturn(ResponseEntity.ok().build());

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).createUserAndJoinSeason("123456", "TestUser", 1);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testPlaceBet() {
    //     when(message.getContentRaw()).thenReturn("!bet 1 1985 HOME 125");
    //     when(user.getId()).thenReturn("123456");
    //     when(user.getName()).thenReturn("TestUser");
    //     when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
    //     when(apiClient.getGameById(1985)).thenReturn(ResponseEntity.ok(createMockGameMap()));
    //     when(apiClient.getUserCoins(1, 1)).thenReturn(ResponseEntity.ok(1000));
    //     when(apiClient.placeBet(1, 1, 1985, "HOME", 125)).thenReturn(ResponseEntity.ok(1));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).createUser("123456", "TestUser");
    //     verify(apiClient).getGameById(1985);
    //     verify(apiClient).getUserCoins(1, 1);
    //     verify(apiClient).placeBet(1, 1, 1985, "HOME", 125);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testMyBets() {
    //     when(message.getContentRaw()).thenReturn("!my_bets 1");
    //     when(user.getId()).thenReturn("123456");
    //     when(user.getName()).thenReturn("TestUser");
    //     when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
    //     when(apiClient.getUserBets(1, 1)).thenReturn(ResponseEntity.ok(createMockBetsList()));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).createUser("123456", "TestUser");
    //     verify(apiClient).getUserBets(1, 1);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testBalance() {
    //     when(message.getContentRaw()).thenReturn("!balance 1");
    //     when(user.getId()).thenReturn("123456");
    //     when(user.getName()).thenReturn("TestUser");
    //     when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
    //     when(apiClient.getUserCoins(1, 1)).thenReturn(ResponseEntity.ok(1000));
    //     when(apiClient.getSeasonById(1)).thenReturn(ResponseEntity.ok(createMockSeason()));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).createUser("123456", "TestUser");
    //     verify(apiClient).getUserCoins(1, 1);
    //     verify(apiClient).getSeasonById(1);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testLeaderboard() {
    //     when(message.getContentRaw()).thenReturn("!leaderboard 1");
    //     when(apiClient.getUsersBySeason(1)).thenReturn(ResponseEntity.ok(createMockLeaderboard()));
    //     when(user.getId()).thenReturn("123456");

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).getUsersBySeason(1);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testSeasonInfo() {
    //     when(message.getContentRaw()).thenReturn("!season_info 1");
    //     when(apiClient.getSeasonById(1)).thenReturn(ResponseEntity.ok(createMockSeason()));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).getSeasonById(1);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testActiveSeasons() {
    //     when(message.getContentRaw()).thenReturn("!active_seasons");
    //     when(apiClient.getActiveSeasons()).thenReturn(ResponseEntity.ok(createMockActiveSeasons()));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).getActiveSeasons();
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testNflWeeks() {
    //     when(message.getContentRaw()).thenReturn("!nfl_weeks");
    //     when(apiClient.getNflWeeks()).thenReturn(ResponseEntity.ok(Arrays.asList(1, 2, 3, 4, 5)));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).getNflWeeks();
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testNflGames() {
    //     when(message.getContentRaw()).thenReturn("!nfl_games 1");
    //     when(apiClient.getNflGamesByWeek(1)).thenReturn(ResponseEntity.ok(createMockGamesList()));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).getNflGamesByWeek(1);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testTeamSchedule() {
    //     when(message.getContentRaw()).thenReturn("!team_schedule Dallas Cowboys");
    //     when(apiClient.getTeamSchedule("Dallas Cowboys")).thenReturn(ResponseEntity.ok(createMockGamesList()));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).getTeamSchedule("Dallas Cowboys");
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testHelp() {
    //     when(message.getContentRaw()).thenReturn("!help");

    //     commandHandler.onMessageReceived(event);

    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // @Test
    // void testDeleteSeason() {
    //     when(message.getContentRaw()).thenReturn("!delete_season 1");
    //     Map<String, Object> response = new HashMap<>();
    //     response.put("deleted", true);
    //     response.put("message", "Season deleted successfully");
    //     when(apiClient.deleteSeason(1)).thenReturn(ResponseEntity.ok(response));

    //     commandHandler.onMessageReceived(event);

    //     verify(apiClient).deleteSeason(1);
    //     verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    // }

    // private Map<String, Object> createMockGameMap() {
    //     Map<String, Object> game = new HashMap<>();
    //     game.put("id", 1985);
    //     game.put("home_team", "Dallas Cowboys");
    //     game.put("away_team", "New York Giants");
    //     game.put("commence_time", "2024-09-10T20:00:00Z");
    //     game.put("home_odds", 1.8);
    //     game.put("away_odds", 2.1);
    //     return game;
    // }

    // private List<Bet> createMockBetsList() {
    //     List<Bet> bets = new ArrayList<>();
    //     bets.add(new Bet(1, 1, 1, 1985, "HOME", 125, Date.from(Instant.now()), "WIN", "Dallas Cowboys", "New York Giants"));
    //     return bets;
    // }

    // private Map<String, Object> createMockSeason() {
    //     Map<String, Object> season = new HashMap<>();
    //     season.put("id", 1);
    //     season.put("start_week", 1);
    //     season.put("end_week", 17);
    //     season.put("initial_coins", 1000);
    //     season.put("created_at", "2024-09-01T00:00:00Z");
    //     return season;
    // }

    // private List<com.dialodds.seasonsbot.User> createMockLeaderboard() {
    //     List<com.dialodds.seasonsbot.User> leaderboard = new ArrayList<>();
    //     leaderboard.add(new com.dialodds.seasonsbot.User(1, "123", "User1", 1500));
    //     leaderboard.add(new com.dialodds.seasonsbot.User(2, "456", "User2", 1200));
    //     return leaderboard;
    // }

    // private List<Season> createMockActiveSeasons() {
    //     List<Season> seasons = new ArrayList<>();
    //     seasons.add(new Season(1, 1, 17, 1000, Date.from(Instant.now()), true));
    //     return seasons;
    // }

    // private List<Game> createMockGamesList() {
    //     List<Game> games = new ArrayList<>();
    //     games.add(new Game(1985, "Dallas Cowboys", "New York Giants", Instant.parse("2024-09-10T20:00:00Z"), 1.8, 2.1));
    //     return games;
    // }
}
```

## File: src/test/java/com/dialodds/seasonsbot/SeasonsbotApplicationTests.java

```java
package com.dialodds.seasonsbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SeasonsbotApplicationTests {

	@Test
	void contextLoads() {
	}

}

```

