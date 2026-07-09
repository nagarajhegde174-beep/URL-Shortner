import { useEffect, useRef } from 'react';
import { Modal } from 'bootstrap';

/**
 * Bootstrap modal that appears when the game ends (winner or draw).
 *
 * @param {{ show: boolean, winner: string|null, draw: boolean,
 *           onNewGame: () => void, onReset: () => void }} props
 */
export default function WinnerModal({ show, winner, draw, onNewGame, onReset }) {
  const modalRef = useRef(null);
  const bsModal  = useRef(null);

  // Initialise Bootstrap Modal instance once
  useEffect(() => {
    if (modalRef.current) {
      bsModal.current = new Modal(modalRef.current, { backdrop: 'static', keyboard: false });
    }
    return () => bsModal.current?.dispose();
  }, []);

  // Show/hide in response to prop
  useEffect(() => {
    if (!bsModal.current) return;
    if (show) {
      bsModal.current.show();
    } else {
      bsModal.current.hide();
    }
  }, [show]);

  const icon   = winner ? '🏆' : '🤝';
  const title  = winner ? `${winner} Wins!` : "It's a Draw!";
  const message = winner
    ? `Congratulations, Player ${winner}! Well played.`
    : 'A perfect match — nobody wins this round.';

  return (
    <div
      className="modal fade"
      id="winnerModal"
      tabIndex={-1}
      aria-labelledby="winnerModalLabel"
      aria-modal="true"
      ref={modalRef}
    >
      <div className="modal-dialog modal-dialog-centered">
        <div className="modal-content">

          <div className="modal-header justify-content-center border-0 pb-0">
            <div className="winner-icon" aria-hidden="true">{icon}</div>
          </div>

          <div className="modal-body text-center py-3">
            <h4 className="fw-bold mb-2" id="winnerModalLabel"
                style={{ color: winner ? 'var(--neon-gold)' : 'var(--neon-gold)' }}>
              {title}
            </h4>
            <p className="text-secondary mb-0">{message}</p>
          </div>

          <div className="modal-footer justify-content-center gap-2">
            <button
              type="button"
              className="btn btn-reset"
              onClick={onReset}
              aria-label="Play again with same score"
            >
              <i className="bi bi-arrow-counterclockwise me-1" />
              Play Again
            </button>
            <button
              type="button"
              className="btn btn-new-game"
              onClick={onNewGame}
              aria-label="Start a new game and reset score"
            >
              <i className="bi bi-plus-circle me-1" />
              New Game
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}
