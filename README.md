# SeasonsBot

## Overview

SeasonsBot is a Discord bot designed to interact with the DialOdds API, providing a user-friendly interface for NFL betting seasons. It allows users to manage their bets, view game information, and participate in betting seasons directly through Discord.

## Features

- Create and join betting seasons
- Place bets on NFL games
- View personal bet history and balance
- Check leaderboards for active seasons
- Get information about NFL games and team schedules
- Admin commands for season management

## Technologies Used

- Java 17
- Spring Boot
- JDA (Java Discord API)
- Spring RestTemplate for API communication

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or later
- Maven
- A Discord Bot Token
- DialOdds API running and accessible

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/seasonsbot.git
   ```

2. Navigate to the project directory:
   ```
   cd seasonsbot
   ```

3. Create a `.env` file in the root directory with the following contents:
   ```
   DISCORD_BOT_TOKEN=your_discord_bot_token
   API_BASE_URL=http://localhost:8080
   ```

4. Build the project:
   ```
   mvn clean install
   ```

5. Run the application:
   ```
   java -jar target/seasonsbot-0.0.1-SNAPSHOT.jar
   ```

## Commands

- `!create_season <start_week> <end_week> <initial_coins>`: Create a new betting season
- `!join_season <season_id>`: Join an existing season
- `!bet <season_id> <game_id> <bet_type> <amount>`: Place a bet
- `!my_bets <season_id>`: View your bets for a season
- `!balance <season_id>`: Check your balance for a season
- `!leaderboard <season_id>`: View the leaderboard for a season
- `!season_info <season_id>`: Get information about a season
- `!active_seasons`: List all active seasons
- `!nfl_weeks`: Get available NFL weeks
- `!nfl_games <week>`: Get NFL games for a specific week
- `!team_schedule <team_name>`: Get schedule for a specific team
- `!help`: Display help information

## Configuration

The bot uses environment variables for configuration. Make sure to set the following:

- `DISCORD_BOT_TOKEN`: Your Discord bot token
- `API_BASE_URL`: The base URL of the DialOdds API (e.g., `http://localhost:8080`)

## Integration with DialOdds API

SeasonsBot communicates with the DialOdds API to fetch game data, manage bets, and retrieve user information. Ensure that the DialOdds API is running and accessible at the specified `API_BASE_URL`.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

## Acknowledgments

- JDA (Java Discord API) for Discord integration
- DialOdds API for providing the backend infrastructure
- The Discord community for inspiration and support