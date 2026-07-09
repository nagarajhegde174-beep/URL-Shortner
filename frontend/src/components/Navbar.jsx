/**
 * Top navigation bar.
 */
export default function Navbar() {
  return (
    <nav className="navbar app-navbar px-3">
      <span className="navbar-brand mb-0 h1">
        <i className="bi bi-grid-3x3-gap-fill me-2" />
        Tic-Tac-Toe
      </span>
      <span className="text-secondary small">React + Spring Boot</span>
    </nav>
  );
}
