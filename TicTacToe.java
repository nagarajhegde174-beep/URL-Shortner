import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

public class TicTacToe {
    int boardWidth = 700;
    int boardHeight = 800;

    JFrame frame = new JFrame("✦ Tic-Tac-Toe ✦");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel footerPanel = new JPanel();
    JButton restartButton;

    JButton[][] board = new JButton[3][3];
    String playerX = "X";
    String playerO = "O";
    String currentPlayer = playerX;

    boolean gameOver = false;
    int turns = 0;
    int scoreX = 0;
    int scoreO = 0;

    // === PREMIUM COLOR PALETTE ===
    Color bgDeep = new Color(13, 17, 23);
    Color bgCard = new Color(22, 27, 34);
    Color bgTile = new Color(30, 37, 48);
    Color bgTileHover = new Color(40, 50, 65);
    Color neonCyan = new Color(0, 245, 255);
    Color neonPink = new Color(255, 0, 128);
    Color neonPurple = new Color(168, 85, 247);
    Color neonGold = new Color(255, 215, 0);
    Color neonGreen = new Color(0, 255, 136);
    Color textWhite = new Color(230, 237, 243);
    Color textDim = new Color(139, 148, 158);
    Color winGlow = new Color(0, 255, 136, 80);
    Color tieGlow = new Color(255, 165, 0, 80);
    Color borderColor = new Color(48, 54, 61);

    TicTacToe() {
        // === FRAME SETUP ===
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(bgDeep);

        // === HEADER PANEL ===
        textPanel.setLayout(new BorderLayout());
        textPanel.setBackground(bgDeep);
        textPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        textPanel.setPreferredSize(new Dimension(boardWidth, 100));

        textLabel.setBackground(bgDeep);
        textLabel.setForeground(neonCyan);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("⚡ X's Turn ⚡");
        textLabel.setOpaque(true);
        textLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        textPanel.add(textLabel, BorderLayout.CENTER);

        // Score panel
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 5));
        scorePanel.setBackground(bgDeep);
        JLabel scoreXLabel = new JLabel("✕ Player X: 0");
        scoreXLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        scoreXLabel.setForeground(neonCyan);
        scoreXLabel.setName("scoreX");
        JLabel scoreOLabel = new JLabel("◯ Player O: 0");
        scoreOLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        scoreOLabel.setForeground(neonPink);
        scoreOLabel.setName("scoreO");
        scorePanel.add(scoreXLabel);
        scorePanel.add(scoreOLabel);
        textPanel.add(scorePanel, BorderLayout.SOUTH);

        frame.add(textPanel, BorderLayout.NORTH);

        // === BOARD WRAPPER (for padding/glow) ===
        JPanel boardWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Outer glow around board
                int pad = 18;
                g2.setColor(new Color(neonPurple.getRed(), neonPurple.getGreen(), neonPurple.getBlue(), 30));
                g2.fillRoundRect(pad - 6, pad - 6, getWidth() - 2 * pad + 12, getHeight() - 2 * pad + 12, 30, 30);
                g2.setColor(new Color(neonPurple.getRed(), neonPurple.getGreen(), neonPurple.getBlue(), 15));
                g2.fillRoundRect(pad - 12, pad - 12, getWidth() - 2 * pad + 24, getHeight() - 2 * pad + 24, 36, 36);
                g2.dispose();
            }
        };
        boardWrapper.setBackground(bgDeep);
        boardWrapper.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // === BOARD PANEL ===
        boardPanel.setLayout(new GridLayout(3, 3, 8, 8));
        boardPanel.setBackground(borderColor);
        boardPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(neonPurple.getRed(), neonPurple.getGreen(), neonPurple.getBlue(), 100), 2, true),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        boardWrapper.add(boardPanel, BorderLayout.CENTER);
        frame.add(boardWrapper, BorderLayout.CENTER);

        // === CREATE TILES ===
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                JButton tile = createStyledTile();
                board[r][c] = tile;
                boardPanel.add(tile);

                tile.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (gameOver) return;
                        JButton tile = (JButton) e.getSource();
                        if (tile.getText().equals("")) {
                            tile.setText(currentPlayer);
                            if (currentPlayer.equals(playerX)) {
                                tile.setForeground(neonCyan);
                            } else {
                                tile.setForeground(neonPink);
                            }
                            turns++;
                            checkWinner();
                            if (!gameOver) {
                                currentPlayer = currentPlayer.equals(playerX) ? playerO : playerX;
                                if (currentPlayer.equals(playerX)) {
                                    textLabel.setText("⚡ X's Turn ⚡");
                                    textLabel.setForeground(neonCyan);
                                } else {
                                    textLabel.setText("⚡ O's Turn ⚡");
                                    textLabel.setForeground(neonPink);
                                }
                            }
                        }
                    }
                });

                // Hover effects
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        JButton btn = (JButton) e.getSource();
                        if (btn.getText().equals("") && !gameOver) {
                            btn.setBackground(bgTileHover);
                            btn.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(new Color(neonPurple.getRed(), neonPurple.getGreen(), neonPurple.getBlue(), 150), 2, true),
                                BorderFactory.createEmptyBorder(4, 4, 4, 4)
                            ));
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        JButton btn = (JButton) e.getSource();
                        if (btn.getText().equals("")) {
                            btn.setBackground(bgTile);
                            btn.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(borderColor, 1, true),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)
                            ));
                        }
                    }
                });
            }
        }

        // === FOOTER / RESTART ===
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));
        footerPanel.setBackground(bgDeep);
        footerPanel.setPreferredSize(new Dimension(boardWidth, 80));

        restartButton = new JButton("⟳  NEW GAME") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, neonPurple, getWidth(), getHeight(), neonPink);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);
                g2.dispose();
            }
        };
        restartButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        restartButton.setForeground(Color.WHITE);
        restartButton.setPreferredSize(new Dimension(220, 50));
        restartButton.setFocusable(false);
        restartButton.setContentAreaFilled(false);
        restartButton.setBorderPainted(false);
        restartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        restartButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                restartButton.setFont(new Font("Segoe UI", Font.BOLD, 19));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                restartButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
            }
        });

        restartButton.addActionListener(e -> restartGame());
        footerPanel.add(restartButton);
        frame.add(footerPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    JButton createStyledTile() {
        JButton tile = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 3D background with subtle gradient
                Color bg = getBackground();
                GradientPaint bgGrad = new GradientPaint(0, 0,
                    new Color(Math.min(bg.getRed() + 15, 255), Math.min(bg.getGreen() + 15, 255), Math.min(bg.getBlue() + 15, 255)),
                    w, h, bg);
                g2.setPaint(bgGrad);
                g2.fillRoundRect(0, 0, w, h, 18, 18);

                // Inner highlight (3D bevel top-left)
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(1, 1, w - 2, h / 2, 18, 18);

                // Bottom shadow (3D depth)
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(2, h - 8, w - 4, 8, 12, 12);

                // Draw the symbol
                String text = getText();
                if (!text.isEmpty()) {
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int textX = (w - fm.stringWidth(text)) / 2;
                    int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

                    Color fg = getForeground();

                    // Outer glow
                    g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 40));
                    for (int i = -3; i <= 3; i++) {
                        for (int j = -3; j <= 3; j++) {
                            g2.drawString(text, textX + i, textY + j);
                        }
                    }

                    // Inner glow
                    g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 80));
                    for (int i = -1; i <= 1; i++) {
                        for (int j = -1; j <= 1; j++) {
                            g2.drawString(text, textX + i, textY + j);
                        }
                    }

                    // Drop shadow
                    g2.setColor(new Color(0, 0, 0, 100));
                    g2.drawString(text, textX + 2, textY + 3);

                    // Main text
                    g2.setColor(fg);
                    g2.drawString(text, textX, textY);

                    // Bright highlight
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.drawString(text, textX, textY - 1);
                }

                g2.dispose();
            }
        };

        tile.setBackground(bgTile);
        tile.setForeground(textWhite);
        tile.setFont(new Font("Segoe UI", Font.BOLD, 100));
        tile.setFocusable(false);
        tile.setContentAreaFilled(false);
        tile.setBorderPainted(true);
        tile.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderColor, 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tile.setText("");

        return tile;
    }

    void checkWinner() {
        // horizontal
        for (int r = 0; r < 3; r++) {
            if (board[r][0].getText().equals("")) continue;

            if (board[r][0].getText().equals(board[r][1].getText()) &&
                board[r][1].getText().equals(board[r][2].getText())) {
                for (int i = 0; i < 3; i++) {
                    setWinner(board[r][i]);
                }
                gameOver = true;
                updateScore();
                return;
            }
        }

        // vertical
        for (int c = 0; c < 3; c++) {
            if (board[0][c].getText().equals("")) continue;

            if (board[0][c].getText().equals(board[1][c].getText()) &&
                board[1][c].getText().equals(board[2][c].getText())) {
                for (int i = 0; i < 3; i++) {
                    setWinner(board[i][c]);
                }
                gameOver = true;
                updateScore();
                return;
            }
        }

        // diagonally
        if (board[0][0].getText().equals(board[1][1].getText()) &&
            board[1][1].getText().equals(board[2][2].getText()) &&
            !board[0][0].getText().equals("")) {
            for (int i = 0; i < 3; i++) {
                setWinner(board[i][i]);
            }
            gameOver = true;
            updateScore();
            return;
        }

        // anti-diagonally
        if (board[0][2].getText().equals(board[1][1].getText()) &&
            board[1][1].getText().equals(board[2][0].getText()) &&
            !board[0][2].getText().equals("")) {
            setWinner(board[0][2]);
            setWinner(board[1][1]);
            setWinner(board[2][0]);
            gameOver = true;
            updateScore();
            return;
        }

        if (turns == 9) {
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    setTie(board[r][c]);
                }
            }
            gameOver = true;
        }
    }

    void setWinner(JButton tile) {
        tile.setForeground(neonGreen);
        tile.setBackground(new Color(0, 255, 136, 25));
        tile.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(neonGreen, 2, true),
            BorderFactory.createEmptyBorder(3, 3, 3, 3)
        ));
        textLabel.setText("🏆 " + currentPlayer + " Wins! 🏆");
        textLabel.setForeground(neonGold);
    }

    void setTie(JButton tile) {
        tile.setForeground(neonGold);
        tile.setBackground(new Color(255, 165, 0, 20));
        tile.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 165, 0, 100), 1, true),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        textLabel.setText("🤝 It's a Tie! 🤝");
        textLabel.setForeground(neonGold);
    }

    void updateScore() {
        if (currentPlayer.equals(playerX)) {
            scoreX++;
        } else {
            scoreO++;
        }
        updateScoreLabels();
    }

    void updateScoreLabels() {
        for (Component comp : ((JPanel) textPanel.getComponent(1)).getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if ("scoreX".equals(label.getName())) {
                    label.setText("✕ Player X: " + scoreX);
                } else if ("scoreO".equals(label.getName())) {
                    label.setText("◯ Player O: " + scoreO);
                }
            }
        }
    }

    void restartGame() {
        gameOver = false;
        turns = 0;
        currentPlayer = playerX;
        textLabel.setText("⚡ X's Turn ⚡");
        textLabel.setForeground(neonCyan);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                board[r][c].setText("");
                board[r][c].setBackground(bgTile);
                board[r][c].setForeground(textWhite);
                board[r][c].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(borderColor, 1, true),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        }
    }
}