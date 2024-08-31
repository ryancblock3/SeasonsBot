package com.dialodds.seasonsbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.http.ResponseEntity;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this);
    commandHandler = new CommandHandler(apiClient);

    when(event.getAuthor()).thenReturn(user);
    when(event.getChannel()).thenReturn(channel);
    when(channel.sendMessage(anyString())).thenReturn(messageAction);
    when(channel.sendMessageEmbeds(any())).thenReturn(messageAction);
    verify(messageAction).queue();
    when(event.getMessage()).thenReturn(message);
}

    @Test
    void testCreateSeason() {
        when(message.getContentRaw()).thenReturn("!create_season 1 17 1000");
        when(apiClient.createSeason(1, 17, 1000)).thenReturn(ResponseEntity.ok(1));

        commandHandler.onMessageReceived(event);

        verify(apiClient).createSeason(1, 17, 1000);
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testJoinSeason() {
        when(message.getContentRaw()).thenReturn("!join_season 1");
        when(user.getId()).thenReturn("123456");
        when(user.getName()).thenReturn("TestUser");
        when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
        when(apiClient.addUserToSeason(1, 1)).thenReturn(ResponseEntity.ok().build());

        commandHandler.onMessageReceived(event);

        verify(apiClient).createUser("123456", "TestUser");
        verify(apiClient).addUserToSeason(1, 1);
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testPlaceBet() {
        when(event.getMessage().getContentRaw()).thenReturn("!bet 1 1985 HOME 125");
        when(user.getId()).thenReturn("123456");
        when(user.getName()).thenReturn("TestUser");
        when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
        when(apiClient.getGameById(1985)).thenReturn(ResponseEntity.ok(createMockGame()));
        when(apiClient.getUserCoins(1, 1)).thenReturn(ResponseEntity.ok(1000));
        when(apiClient.placeBet(1, 1, 1985, "HOME", 125)).thenReturn(ResponseEntity.ok(1));

        commandHandler.onMessageReceived(event);

        verify(apiClient).createUser("123456", "TestUser");
        verify(apiClient).getGameById(1985);
        verify(apiClient).getUserCoins(1, 1);
        verify(apiClient).placeBet(1, 1, 1985, "HOME", 125);
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testMyBets() {
        when(event.getMessage().getContentRaw()).thenReturn("!my_bets 1");
        when(user.getId()).thenReturn("123456");
        when(user.getName()).thenReturn("TestUser");
        when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
        when(apiClient.getUserBets(1, 1)).thenReturn(ResponseEntity.ok(createMockBetsList()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).createUser("123456", "TestUser");
        verify(apiClient).getUserBets(1, 1);
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testBalance() {
        when(event.getMessage().getContentRaw()).thenReturn("!balance 1");
        when(user.getId()).thenReturn("123456");
        when(user.getName()).thenReturn("TestUser");
        when(apiClient.createUser("123456", "TestUser")).thenReturn(ResponseEntity.ok(1));
        when(apiClient.getUserCoins(1, 1)).thenReturn(ResponseEntity.ok(1000));
        when(apiClient.getSeasonById(1)).thenReturn(ResponseEntity.ok(createMockSeason()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).createUser("123456", "TestUser");
        verify(apiClient).getUserCoins(1, 1);
        verify(apiClient).getSeasonById(1);
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testLeaderboard() {
        when(event.getMessage().getContentRaw()).thenReturn("!leaderboard 1");
        when(apiClient.getUsersBySeason(1)).thenReturn(ResponseEntity.ok(createMockLeaderboard()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getUsersBySeason(1);
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testSeasonInfo() {
        when(event.getMessage().getContentRaw()).thenReturn("!season_info 1");
        when(apiClient.getSeasonById(1)).thenReturn(ResponseEntity.ok(createMockSeason()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getSeasonById(1);
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testActiveSeasons() {
        when(event.getMessage().getContentRaw()).thenReturn("!active_seasons");
        when(apiClient.getActiveSeasons()).thenReturn(ResponseEntity.ok(createMockActiveSeasons()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getActiveSeasons();
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testNflWeeks() {
        when(event.getMessage().getContentRaw()).thenReturn("!nfl_weeks");
        when(apiClient.getNflWeeks()).thenReturn(ResponseEntity.ok(Arrays.asList(1, 2, 3, 4, 5)));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getNflWeeks();
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testNflGames() {
        when(event.getMessage().getContentRaw()).thenReturn("!nfl_games 1");
        when(apiClient.getNflGamesByWeek(1)).thenReturn(ResponseEntity.ok(createMockGamesList()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getNflGamesByWeek(1);
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testTeamSchedule() {
        when(event.getMessage().getContentRaw()).thenReturn("!team_schedule Dallas Cowboys");
        when(apiClient.getTeamSchedule("Dallas Cowboys")).thenReturn(ResponseEntity.ok(createMockGamesList()));

        commandHandler.onMessageReceived(event);

        verify(apiClient).getTeamSchedule("Dallas Cowboys");
        verify(channel).sendMessageEmbeds(any());
    }

    @Test
    void testHelp() {
        when(event.getMessage().getContentRaw()).thenReturn("!help");

        commandHandler.onMessageReceived(event);

        verify(channel).sendMessageEmbeds(any());
    }

    // Helper methods to create mock data

    private Map<String, Object> createMockGame() {
        Map<String, Object> game = new HashMap<>();
        game.put("id", 1985);
        game.put("home_team", "Dallas Cowboys");
        game.put("away_team", "New York Giants");
        game.put("home_odds", 1.8);
        game.put("away_odds", 2.1);
        game.put("commence_time", "2024-09-10T20:00:00Z");
        return game;
    }

    private List<Map<String, Object>> createMockBetsList() {
        List<Map<String, Object>> bets = new ArrayList<>();
        Map<String, Object> bet = new HashMap<>();
        bet.put("id", 1);
        bet.put("home_team", "Dallas Cowboys");
        bet.put("away_team", "New York Giants");
        bet.put("bet_type", "HOME");
        bet.put("amount", 125);
        bet.put("result", "WIN");
        bets.add(bet);
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

    private List<Map<String, Object>> createMockLeaderboard() {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        Map<String, Object> user1 = new HashMap<>();
        user1.put("username", "User1");
        user1.put("coins", 1500);
        Map<String, Object> user2 = new HashMap<>();
        user2.put("username", "User2");
        user2.put("coins", 1200);
        leaderboard.add(user1);
        leaderboard.add(user2);
        return leaderboard;
    }

    private List<Map<String, Object>> createMockActiveSeasons() {
        List<Map<String, Object>> seasons = new ArrayList<>();
        seasons.add(createMockSeason());
        return seasons;
    }

    private List<Map<String, Object>> createMockGamesList() {
        List<Map<String, Object>> games = new ArrayList<>();
        games.add(createMockGame());
        return games;
    }
}