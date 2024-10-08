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
import java.io.FileNotFoundException;
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
import java.util.stream.Collectors;

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

            String awayTeam = (String) gameDetails.get("away_team");
            String homeTeam = (String) gameDetails.get("home_team");
            String teamBetOn = betType.equals("HOME") ? homeTeam : awayTeam;
            String odds = betType.equals("HOME") ? String.valueOf(gameDetails.get("home_odds"))
                    : String.valueOf(gameDetails.get("away_odds"));

            EmbedBuilder successEmbed = new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("Bet Placed Successfully")
                    .setDescription("Your bet on the " + teamBetOn + " has been placed successfully!")
                    .addField("Bet Details", String.format("Bet ID: %d | Season ID: %d", betId, seasonId), false)
                    .addField("Game", awayTeam + " vs " + homeTeam, false)
                    .addField("Bet Amount", amount + " coins", true)
                    .addField("Bet Type", betType, true)
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
                String betOnTeam = bet.getBetType().equalsIgnoreCase("HOME") ? bet.getHomeTeam() : bet.getAwayTeam();
                String betInfo = String.format("%s vs %s\nBet: %d coins on the %s\nResult: %s",
                        bet.getHomeTeam(), bet.getAwayTeam(),
                        bet.getAmount(), betOnTeam,
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
        String result = bet.getResult();
        if (result == null || result.isEmpty()) {
            return "Pending";
        }
        return result.equalsIgnoreCase("won") ? "Won" : "Lost";
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
                String userInfo = String.format("**%s**\nCoins: %d", requesterInfo.getUsername(),
                        requesterInfo.getCoins());
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
            List<Game> allGames = gamesResponse.getBody();

            if (allGames == null || allGames.isEmpty()) {
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

            // Filter games for the requested week
            List<Game> weekGames = allGames.stream()
                    .filter(game -> getWeekNumber(game.getCommenceTime()) == week)
                    .collect(Collectors.toList());

            if (weekGames.isEmpty()) {
                sendErrorEmbed(event, "No Games Found",
                        "No games found for Week " + week + ".",
                        "Please try a different week number.");
                return;
            }

            String messageId = UUID.randomUUID().toString();
            Game firstGame = weekGames.get(0);

            EmbedBuilder initialEmbed = createGameEmbed(firstGame, week, 1, weekGames.size());
            List<Button> buttons = createNavigationButtons(messageId, 0, weekGames.size());

            String awayTeam = firstGame.getAwayTeam();
            String homeTeam = firstGame.getHomeTeam();

            try {
                File logoImage = createLogoImage(awayTeam, homeTeam);
                event.getChannel().sendMessageEmbeds(initialEmbed.build())
                        .setActionRow(buttons)
                        .addFiles(FileUpload.fromData(logoImage, "game_logos.png"))
                        .queue(message -> {
                            logoImage.delete();
                            activeGamesCache.put(messageId, weekGames);
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
        if (gameDate.getYear() > seasonStartDate.getYear()) {
            // Handle games in the following year (e.g., Week 18)
            return (int) (ChronoUnit.WEEKS.between(seasonStartDate, gameDate.withYear(seasonStartDate.getYear())) + 1);
        }
        long weeksSinceStart = ChronoUnit.WEEKS.between(seasonStartDate, gameDate);
        return (int) weeksSinceStart + 1;
    }

    private File createLogoImage(String awayTeam, String homeTeam) throws IOException {
        String[] possiblePaths = {
                System.getenv("LOGO_PATH"),
                "/app/logos",
                "src/main/resources/static/logos",
                "logos"
        };

        String logoPath = null;
        for (String path : possiblePaths) {
            if (path != null && new File(path).exists()) {
                logoPath = path;
                break;
            }
        }

        if (logoPath == null) {
            throw new FileNotFoundException("Could not find logos directory");
        }

        BufferedImage awayLogo = ImageIO.read(new File(logoPath, getTeamLogoFilename(awayTeam)));
        BufferedImage homeLogo = ImageIO.read(new File(logoPath, getTeamLogoFilename(homeTeam)));

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
                "If you need more information about a specific command, try using it with no arguments or incorrect arguments. "
                        +
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