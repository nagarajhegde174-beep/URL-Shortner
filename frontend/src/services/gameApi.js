import axios from 'axios';


const api = axios.create({
  baseURL: '/api/game',
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
});


export const getGame = () => api.get('/');


export const getStatus = () => api.get('/status');


export const newGame = () => api.post('/new');


export const resetGame = () => api.post('/reset');


export const makeMove = (row, col) => api.post('/move', { row, col });
