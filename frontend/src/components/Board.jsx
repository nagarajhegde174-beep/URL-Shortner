import Cell from './Cell.jsx';


export default function Board({ board, winningCells, gameOver, draw, loading, onCellClick }) {
  
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
