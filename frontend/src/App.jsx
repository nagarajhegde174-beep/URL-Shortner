import Navbar from './components/Navbar.jsx';
import Home   from './pages/Home.jsx';

/**
 * Root application component.
 * Renders the navbar and the main game page.
 */
export default function App() {
  return (
    <>
      <Navbar />
      <Home />
    </>
  );
}
