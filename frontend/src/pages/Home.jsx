import { useState, useEffect, useCallback } from 'react';
import Board       from '../components/Board.jsx';
import GameStatus  from '../components/GameStatus.jsx';
import ScoreBoard  from '../components/ScoreBoard.jsx';
import WinnerModal from '../components/WinnerModal.jsx';
import { getGame, newGame, resetGame, makeMove } from '../services/gameApi.js';


const EMPTY_BOARD = [
  ['', '', ''],
  ['', '', ''],
  ['', '', ''],
];


export default function Home() {
  const [board,         setBoard]         = useState(EMPTY_BOARD);
  const [currentPlayer, setCurrentPlayer] = useState('X');
  const [gameOver,      setGameOver]      = useState(false);
  const [winner,        setWinner]        = useState(null);
  const [draw,          setDraw]          = useState(false);
  const [scoreX,        setScoreX]        = useState(0);
  const [scoreO,        setScoreO]        = useState(0);
  const [winningCells,  setWinningCells]  = useState(null);
  const [loading,       setLoading]       = useState(false);
  const [error,         setError]         = useState(null);
  const [showModal,     setShowModal]     = useState(false);

  
  const applyResponse = useCallback((data) => {
    setBoard(data.board ?? EMPTY_BOARD);
    setCurrentPlayer(data.currentPlayer ?? 'X');
    setGameOver(data.gameOver ?? false);
    setWinner(data.winner ?? null);
    setDraw(data.draw ?? false);
    setScoreX(data.scoreX ?? 0);
    setScoreO(data.scoreO ?? 0);
    setWinningCells(data.winningCells ?? null);
    
    if (data.gameOver) {
      setTimeout(() => setShowModal(true), 600);
    } else {
      setShowModal(false);
    }
  }, []);

  
  const callApi = useCallback(async (apiFn) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await apiFn();
      applyResponse(data);
    } catch (err) {
      const message =
        err.response?.data?.error ??
        err.message ??
        'Something went wrong. Is the backend running?';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, [applyResponse]);

  
  useEffect(() => {
    callApi(getGame);
  }, [callApi]);

  
  useEffect(() => {
    if (!error) return;
    const timer = setTimeout(() => setError(null), 4000);
    return () => clearTimeout(timer);
  }, [error]);

  const handleCellClick = (row, col) => {
    if (gameOver || loading) return;
    callApi(() => makeMove(row, col));
  };

  const handleReset = () => {
    setShowModal(false);
    callApi(resetGame);
  };

  const handleNewGame = () => {
    setShowModal(false);
    callApi(newGame);
  };

  return (
    <main className="container py-4" style={{ maxWidth: '480px' }}>
      <ScoreBoard scoreX={scoreX} scoreO={scoreO} />

      <GameStatus
        gameOver={gameOver}
        winner={winner}
        draw={draw}
        currentPlayer={currentPlayer}
        loading={loading}
      />

      <Board
        board={board}
        winningCells={winningCells}
        gameOver={gameOver}
        draw={draw}
        loading={loading}
        onCellClick={handleCellClick}
      />

      {}
      <div className="d-flex justify-content-center gap-3 mt-2">
        <button
          className="btn btn-reset"
          onClick={handleReset}
          disabled={loading}
          aria-label="Restart the current game"
        >
          <i className="bi bi-arrow-counterclockwise me-1" />
          Restart
        </button>
        <button
          className="btn btn-new-game"
          onClick={handleNewGame}
          disabled={loading}
          aria-label="Start a new game and reset score"
        >
          <i className="bi bi-plus-circle me-1" />
          New Game
        </button>
      </div>

      {}
      <WinnerModal
        show={showModal}
        winner={winner}
        draw={draw}
        onNewGame={handleNewGame}
        onReset={handleReset}
      />

      {}
      {error && (
        <div
          className="error-toast alert alert-danger d-flex align-items-center"
          role="alert"
          aria-live="assertive"
        >
          <i className="bi bi-exclamation-triangle-fill me-2 flex-shrink-0" />
          <span>{error}</span>
          <button
            type="button"
            className="btn-close btn-close-white ms-auto"
            aria-label="Dismiss error"
            onClick={() => setError(null)}
          />
        </div>
      )}
    </main>
  );
}
