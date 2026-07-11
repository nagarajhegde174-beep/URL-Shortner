
export default function GameStatus({ gameOver, winner, draw, currentPlayer, loading }) {
  const getStatusClass = () => {
    if (winner) return 'status-panel winner';
    if (draw)   return 'status-panel draw';
    return `status-panel turn-${currentPlayer?.toLowerCase()}`;
  };

  const getStatusContent = () => {
    if (loading) {
      return (
        <>
          <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
          Loading…
        </>
      );
    }
    if (winner) {
      return (
        <>
          <i className="bi bi-trophy-fill me-2" />
          {winner} wins!
        </>
      );
    }
    if (draw) {
      return (
        <>
          <i className="bi bi-handshake-fill me-2" />
          It's a draw!
        </>
      );
    }
    return (
      <>
        <i className={`bi bi-${currentPlayer === 'X' ? 'x-lg' : 'circle'} me-2`} />
        {currentPlayer}'s turn
      </>
    );
  };

  return (
    <div
      className={getStatusClass()}
      role="status"
      aria-live="polite"
      aria-atomic="true"
    >
      {getStatusContent()}
    </div>
  );
}
