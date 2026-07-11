
export default function ScoreBoard({ scoreX, scoreO }) {
  return (
    <div className="score-board" role="region" aria-label="Score board">
      <div className="score-item">
        <span className="score-label">Player X</span>
        <span className="score-value score-x" aria-label={`X score: ${scoreX}`}>
          {scoreX}
        </span>
      </div>

      <div className="score-item">
        <span className="score-label">vs</span>
        <span className="score-value score-tie">⚔</span>
      </div>

      <div className="score-item">
        <span className="score-label">Player O</span>
        <span className="score-value score-o" aria-label={`O score: ${scoreO}`}>
          {scoreO}
        </span>
      </div>
    </div>
  );
}
