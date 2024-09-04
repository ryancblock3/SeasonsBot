package com.dialodds.seasonsbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.http.ResponseEntity;
import java.util.*;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CommandHandlerTest {

    private CommandHandler commandHandler;

    @Mock
    private ApiClient apiClient;

    @Mock
    private MessageReceivedEvent event;

    @Mock
    private User user;

    @Mock
    private MessageChannelUnion channel;

    @Mock
    private MessageCreateAction messageAction;

    @Mock
    private Message message;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commandHandler = new CommandHandler(apiClient);

        when(event.getAuthor()).thenReturn(user);
        when(event.getChannel()).thenReturn(channel);
        when(channel.sendMessage(anyString())).thenReturn(messageAction);
        when(channel.sendMessageEmbeds(any(MessageEmbed.class))).thenReturn(messageAction);
        when(event.getMessage()).thenReturn(message);
        when(channel.sendTyping()).thenReturn(mock(RestAction.class));
    }

    @Test
    void testCreateSeason() {
        when(message.getContentRaw()).thenReturn("!create_season 1 17 1000");
        when(apiClient.createSeason(1, 17, 1000)).thenReturn(ResponseEntity.ok(1));

        commandHandler.onMessageReceived(event);

        verify(apiClient).createSeason(1, 17, 1000);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testJoinSeason() {
        when(message.getContentRaw()).thenReturn("!join_season 1");
        when(user.getId()).thenReturn("123456");
        when(user.getName()).thenReturn("TestUser");
        when(apiClient.createUserAndJoinSeason("123456", "TestUser", 1)).thenReturn(ResponseEntity.ok().build());

        commandHandler.onMessageReceived(event);

        verify(apiClient).createUserAndJoinSeason("123456", "TestUser", 1);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testPlaceBet() {
        when(message.getContentRaw()).thenReturn("!bet 1 1985 HOME 125");
        when(user.getId()).thenReturn("123456");
        when(user.getName()).thenReturn("TestUser");
        when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
        when(apiClient.getGameById(1985)).thenReturn(ResponseEntity.ok(createMockGameMap()));
        when(apiClient.getUserCoins(1, 1)).thenReturn(ResponseEntity.ok(1000));
        when(apiClient.placeBet(1, 1, 1985, "HOME", 125)).thenReturn(ResponseEntity.ok(1));

        commandHandler.onMessageReceived(event);

        verify(apiClient).createUser("123456", "TestUser");
        verify(apiClient).getGameById(1985);
        verify(apiClient).getUserCoins(1, 1);
        verify(apiClient).placeBet(1, 1, 1985, "HOME", 125);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testMyBets() {
        when(message.getContentRaw()).thenReturn("!my_bets 1");
        when(user.getId()).thenReturn("123456");
        when(user.getName()).thenReturn("TestUser");
        when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
        when(apiClient.getUserBets(1, 1)).thenReturn(ResponseEntity.ok(createMockBetsList()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).createUser("123456", "TestUser");
        verify(apiClient).getUserBets(1, 1);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testBalance() {
        when(message.getContentRaw()).thenReturn("!balance 1");
        when(user.getId()).thenReturn("123456");
        when(user.getName()).thenReturn("TestUser");
        when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
        when(apiClient.getUserCoins(1, 1)).thenReturn(ResponseEntity.ok(1000));
        when(apiClient.getSeasonById(1)).thenReturn(ResponseEntity.ok(createMockSeason()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).createUser("123456", "TestUser");
        verify(apiClient).getUserCoins(1, 1);
        verify(apiClient).getSeasonById(1);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testLeaderboard() {
        when(message.getContentRaw()).thenReturn("!leaderboard 1");
        when(apiClient.getUsersBySeason(1)).thenReturn(ResponseEntity.ok(createMockLeaderboard()));
        when(user.getId()).thenReturn("123456");

        commandHandler.onMessageReceived(event);

        verify(apiClient).getUsersBySeason(1);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testSeasonInfo() {
        when(message.getContentRaw()).thenReturn("!season_info 1");
        when(apiClient.getSeasonById(1)).thenReturn(ResponseEntity.ok(createMockSeason()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getSeasonById(1);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testActiveSeasons() {
        when(message.getContentRaw()).thenReturn("!active_seasons");
        when(apiClient.getActiveSeasons()).thenReturn(ResponseEntity.ok(createMockActiveSeasons()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getActiveSeasons();
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testNflWeeks() {
        when(message.getContentRaw()).thenReturn("!nfl_weeks");
        when(apiClient.getNflWeeks()).thenReturn(ResponseEntity.ok(Arrays.asList(1, 2, 3, 4, 5)));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getNflWeeks();
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testNflGames() {
        when(message.getContentRaw()).thenReturn("!nfl_games 1");
        when(apiClient.getNflGamesByWeek(1)).thenReturn(ResponseEntity.ok(createMockGamesList()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getNflGamesByWeek(1);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testTeamSchedule() {
        when(message.getContentRaw()).thenReturn("!team_schedule Dallas Cowboys");
        when(apiClient.getTeamSchedule("Dallas Cowboys")).thenReturn(ResponseEntity.ok(createMockGamesList()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getTeamSchedule("Dallas Cowboys");
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testHelp() {
        when(message.getContentRaw()).thenReturn("!help");

        commandHandler.onMessageReceived(event);

        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    @Test
    void testDeleteSeason() {
        when(message.getContentRaw()).thenReturn("!delete_season 1");
        Map<String, Object> response = new HashMap<>();
        response.put("deleted", true);
        response.put("message", "Season deleted successfully");
        when(apiClient.deleteSeason(1)).thenReturn(ResponseEntity.ok(response));

        commandHandler.onMessageReceived(event);

        verify(apiClient).deleteSeason(1);
        verify(channel).sendMessageEmbeds(any(MessageEmbed.class));
    }

    // Helper methods to create mock data

    private Map<String, Object> createMockGameMap() {
        Map<String, Object> game = new HashMap<>();
        game.put("id", 1985);
        game.put("home_team", "Dallas Cowboys");
        game.put("away_team", "New York Giants");
        game.put("commence_time", "2024-09-10T20:00:00Z");
        game.put("home_odds", 1.8);
        game.put("away_odds", 2.1);
        return game;
    }

    private List<Bet> createMockBetsList() {
        List<Bet> bets = new ArrayList<>();
        bets.add(new Bet(1, 1, 1, 1985, "HOME", 125, Date.from(Instant.now()), "WIN", "Dallas Cowboys", "New York Giants"));
        return bets;
    }

    private Map<String, Object> createMockSeason() {
        Map<String, Object> season = new HashMap<>();
        season.put("id", 1);
        season.put("start_week", 1);
        season.put("end_week", 17);
        season.put("initial_coins", 1000);
        season.put("created_at", "2024-09-01T00:00:00Z");
        return season;
    }

    private List<com.dialodds.seasonsbot.User> createMockLeaderboard() {
        List<com.dialodds.seasonsbot.User> leaderboard = new ArrayList<>();
        leaderboard.add(new com.dialodds.seasonsbot.User(1, "123", "User1", 1500));
        leaderboard.add(new com.dialodds.seasonsbot.User(2, "456", "User2", 1200));
        return leaderboard;
    }

    private List<Season> createMockActiveSeasons() {
        List<Season> seasons = new ArrayList<>();
        seasons.add(new Season(1, 1, 17, 1000, Date.from(Instant.now()), true));
        return seasons;
    }

    private List<Game> createMockGamesList() {
        List<Game> games = new ArrayList<>();
        games.add(new Game(1985, "Dallas Cowboys", "New York Giants", Instant.parse("2024-09-10T20:00:00Z"), 1.8, 2.1));
        return games;
    }
}