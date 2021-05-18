# PoroBot-Backend
Backend for a Discord bot with a integrated Website.
Uses a Database, Webcrawler and Java-Spring webserver to create a unique Bot-software.

This program acts as a Discord bot handling user commands.
The idea is to return statistics and links about a League of Legends match to user.
Many of the commands respond with statistic website links etc. info about the game.
The main functionality however is the game lobby tracking and player reporting (explained later).

The user experience is made easier by a automated login with DiscordID which is stored in a local Database.
User can connect their game account to said ID for automated statistic about their current matches.

We can track the user and their account with the help of game developers API https://developer.riotgames.com/.
Usage of this api however is limited so we parse wanted match data from online sources.

The program gets the said data by using Selenium-Webdriver as a website crawler, returning found data from loaded game-statistic websites and their html-code.
This data is given a unique ID and stored with the user data in local Database.

The idea of the web-server is to return a GUI experience with a ReactJS Made website instead of string of data in Discord chat.
Web-Server uses Java-Spring to respond to React-frontend and the user is provided with a link to said website

Frontend of this project aka. the website is available at: https://github.com/lphaap/PoroBot-Frontend

On top of this the bot provides a external reporting system using the local database.
After games users can report the players they played with to the system.
These reports show up as warnings when the reported player shows up in the any of the users game.
This way the user can decide before the game if they want to play with said player.
