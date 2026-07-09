import axios from 'axios';

/**
 * Axios instance pre-configured for the Spring Boot backend.
 * The Vite proxy forwards /api/* to http://localhost:8080 during development.
 */
const api = axios.create({
  baseURL: '/api/game',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});

/** GET /api/game — fetch current board state */
export const getGame = () => api.get('/');

/** GET /api/game/status — alias for getGame */
export const getStatus = () => api.get('/status');

/** POST /api/game/new — start a new game (resets score) */
export const newGame = () => api.post('/new');

/** POST /api/game/reset — restart without resetting score */
export const resetGame = () => api.post('/reset');

/**
 * POST /api/game/move — make a move
 * @param {number} row  0-2
 * @param {number} col  0-2
 */
export const makeMove = (row, col) => api.post('/move', { row, col });
