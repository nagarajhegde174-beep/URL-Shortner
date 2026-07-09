import Cell from './Cell.jsx';

/**
 * Renders the 3×3 game board using Cell components.
 *
 * @param {{ board: string[][], winningCells: number[][]|null,
 *           gameOver: boolean, draw: boolean, loading: boolean,
 *           onCellClick: (row, col) => void }} props
 */
export default function Board({ board, winningCells, gameOver, draw, loading, onCellClick }) {
  /**
   * Returns true if [r, c] is part of the winning combination.
   * Mirrors the winningCells array returned by the backend.
   */
  const isWinningCell = (r, c) => {
    if (!winningCells) return false;
    return winningCells.some(([wr, wc]) => wr === r && wc === c);
  };

  return (
    <div className="board-wrapper" role="grid" aria-label="Tic-Tac-Toe board">
      <div className="board-grid">
        {board.map((row, r) =>
          row.map((cell, c) => (
            <Cell
              key={`${r}-${c}`}
              value={cell}
              row={r}
              col={c}
              isWinning={isWinningCell(r, c)}
              isDraw={draw && gameOver}
              gameOver={gameOver}
              loading={loading}
              onClick={onCellClick}
            />
          ))
        )}
      </div>
    </div>
  );
}
