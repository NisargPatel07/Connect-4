# Connect Four Multiplayer Game (Java)

## Overview
This project is a multiplayer Connect Four game built using Java. It follows a client–server model where players connect to a central server and play against each other in real time. The server controls the game logic, while the client provides a simple graphical interface for players.

## Technologies Used
- Java  
- Java Sockets (Networking)  
- Multithreading  
- Java Swing (GUI)

## How It Works
- The server waits for players to connect.
- Once two players join, a game session starts.
- The server handles turns, checks for wins, and sends updates.
- The client displays the board and allows players to make moves.

## Project Structure
- ConnectFourServer.java – Starts the server and accepts clients  
- ClientHandler.java – Manages communication with each client  
- GameSession.java – Controls game logic and rules  
- ConnectFourClient.java – Client-side game interface  
- ServerGUI.java – Displays server activity

## How to Run
1. Compile all .java files  
2. Run ConnectFourServer first  
3. Run ConnectFourClient on two different terminals or machines  
4. Start playing once both players connect

## Learning Outcomes
- Client–server communication  
- Multithreaded programming  
- Real-time game logic  
- Team-based software development

## Team Project
Developed as a team project with a focus on collaboration and reliable networking.
