<div align="center">

# ✦ Tic-Tac-Toe
### Full-Stack Web Application

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

**A modern full-stack web application built with React + Spring Boot**  
*REST API backend · React frontend · Responsive dark-neon UI*

</div>

---

## 📸 Preview

| Game Board | Winner Modal |
|:---:|:---:|
| ![Board](https://placehold.co/420x380/0d1117/00f5ff?text=Game+Board) | ![Winner](https://placehold.co/420x380/0d1117/ffd700?text=Winner+Modal) |

> Replace placeholder images above with actual screenshots after running the app locally.

---

## 🚀 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 18, Vite, Bootstrap 5, Axios |
| **Backend** | Java 17, Spring Boot 3, Maven |
| **API Style** | REST — JSON responses |
| **Storage** | In-memory (no database) |

---

## ✨ Features

- 🎮 &nbsp;Two-player Tic-Tac-Toe (X vs O)
- 🏆 &nbsp;Win detection — rows, columns, diagonals
- 🤝 &nbsp;Draw detection when all 9 cells are filled
- 📊 &nbsp;Live scoreboard that persists across rounds
- 🔄 &nbsp;Restart (keeps score) and New Game (resets score)
- 💡 &nbsp;Winning cells highlighted with glow animation
- 📱 &nbsp;Fully responsive — works on mobile and desktop
- ⚡ &nbsp;All game logic lives in the backend REST API

---

## 🗂️ Project Structure

```
tic-tac-toe/
│
├── backend/                              # Spring Boot REST API
│   └── src/main/java/com/example/tictactoe/
│       ├── TicTacToeApplication.java     # Entry point
│       ├── config/
│       │   └── CorsConfig.java           # CORS for React dev server
│       ├── controller/
│       │   └── GameController.java       # REST endpoints
│       ├── service/
│       │   └── GameService.java          # Core game logic
│       ├── model/
│       │   ├── Board.java                # In-memory board state
│       │   ├── MoveRequest.java          # Request payload
│       │   └── GameResponse.java         # Unified JSON response
│       └── exception/
│           ├── InvalidMoveException.java
│           └── GlobalExceptionHandler.java
│
└── frontend/                             # React + Vite
    └── src/
        ├── components/
        │   ├── Board.jsx                 # 3×3 grid
        │   ├── Cell.jsx                  # Individual cell with animations
        │   ├── GameStatus.jsx            # Turn / winner / draw banner
        │   ├── Navbar.jsx
        │   ├── ScoreBoard.jsx
        │   └── WinnerModal.jsx           # Bootstrap end-game modal
        ├── pages/
        │   └── Home.jsx                  # State orchestration + API calls
        ├── services/
        │   └── gameApi.js                # Axios API client
        ├── styles/app.css
        ├── App.jsx
        └── main.jsx
```

---

## 🔌 REST API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/game` | Get current board state |
| `GET` | `/api/game/status` | Alias for GET /api/game |
| `POST` | `/api/game/new` | Start new game + reset scoreboard |
| `POST` | `/api/game/reset` | Restart game, keep score |
| `POST` | `/api/game/move` | Make a move `{ "row": 0–2, "col": 0–2 }` |

**Sample response:**

```json
{
  "board": [["X","",""],["","O",""],["","","X"]],
  "currentPlayer": "O",
  "gameOver": false,
  "winner": null,
  "draw": false,
  "scoreX": 1,
  "scoreO": 0,
  "winningCells": null,
  "message": "O's turn"
}
```

---

## ⚙️ Run Locally

**Prerequisites:** Java 17+, Maven 3.8+, Node.js 18+

```bash
# 1 — Clone
git clone https://github.com/your-username/tic-tac-toe.git
cd tic-tac-toe

# 2 — Start backend (port 8080)
cd backend
mvn spring-boot:run

# 3 — Start frontend (port 5173)  [new terminal]
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173** 🎉

---

<div align="center">

Made with ☕ Java + ⚛️ React

</div>
