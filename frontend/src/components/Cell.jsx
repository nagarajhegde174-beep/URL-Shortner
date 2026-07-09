/**
 * A single cell on the Tic-Tac-Toe board.
 *
 * @param {{ value: string, row: number, col: number,
 *           isWinning: boolean, isDraw: boolean,
 *           gameOver: boolean, loading: boolean,
 *           onClick: (row, col) => void }} props
 */
export default function Cell({ value, row, col, isWinning, isDraw, gameOver, loading, onClick }) {
  const isEmpty    = value === '' || value == null;
  const isOccupied = !isEmpty;
  const isDisabled = isOccupied || gameOver || loading;

  const classNames = [
    'cell',
    isOccupied  ? 'occupied' : '',
    isDisabled  ? 'disabled' : '',
    isWinning   ? 'winning'  : '',
    isDraw      ? 'draw-cell': '',
    value === 'X' ? 'mark-x' : '',
    value === 'O' ? 'mark-o' : '',
  ].filter(Boolean).join(' ');

  const handleClick = () => {
    if (!isDisabled) onClick(row, col);
  };

  const handleKeyDown = (e) => {
    if ((e.key === 'Enter' || e.key === ' ') && !isDisabled) {
      e.preventDefault();
      onClick(row, col);
    }
  };

  return (
    <div
      className={classNames}
      role="button"
      tabIndex={isDisabled ? -1 : 0}
      aria-label={`Cell ${row},${col}: ${value || 'empty'}`}
      aria-disabled={isDisabled}
      onClick={handleClick}
      onKeyDown={handleKeyDown}
    >
      {value && <span className="mark">{value}</span>}
    </div>
  );
}
