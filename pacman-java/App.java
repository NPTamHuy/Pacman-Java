import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.image.BufferedImage; 
import javax.swing.table.DefaultTableCellRenderer;

public class App {
    private static int difficultyLevel = 1; // 1: DỄ, 2: TRUNG BÌNH, 3: KHÓ
    private static final String[] DIFFICULTY_FILES = {"", "score_easy.txt", "score_medium.txt", "score_hard.txt"};
    private static final String[] DIFFICULTY_NAMES = {"", "DỄ", "TRUNG BÌNH", "KHÓ"};
    private static final int MAX_HIGH_SCORES = 10;

    public static void main(String[] args) {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pac-Man");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showMainMenu(frame, boardWidth, boardHeight);
    }

    // Hiển thị menu chính
    private static void showMainMenu(JFrame frame, int width, int height) {
        // Tạo panel với background gradient
        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Tạo gradient từ đen sang xanh đậm
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(0, 0, 0), 
                    0, height, new Color(0, 0, 70)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
                
                // Vẽ các đốm tròn nhỏ giống thức ăn Pacman trang trí
                g2d.setColor(Color.WHITE);
                for (int i = 0; i < 50; i++) {
                    int x = (int)(Math.random() * width);
                    int y = (int)(Math.random() * height);
                    g2d.fillOval(x, y, 4, 4);
                }
            }
        };
        
        menuPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // Logo pacman (thay thế hoặc tải hình ảnh thực tế)
        ImageIcon pacmanIcon = createPacmanIcon(100);
        JLabel logoLabel = new JLabel(pacmanIcon);
        menuPanel.add(logoLabel, gbc);

        // Tiêu đề
        gbc.gridy++;
        JLabel titleLabel = new JLabel("PAC-MAN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 80));
        titleLabel.setForeground(Color.YELLOW);
        menuPanel.add(titleLabel, gbc);

        // Nút độ khó: DỄ
        gbc.gridy++;
        JButton easyButton = createMenuButton("DỄ", Color.GREEN);
        menuPanel.add(easyButton, gbc);

        // Nút độ khó: TRUNG BÌNH
        gbc.gridy++;
        JButton mediumButton = createMenuButton("TRUNG BÌNH", Color.YELLOW);
        menuPanel.add(mediumButton, gbc);

        // Nút độ khó: KHÓ
        gbc.gridy++;
        JButton hardButton = createMenuButton("KHÓ", Color.RED);
        menuPanel.add(hardButton, gbc);

        // Nút bảng điểm
        gbc.gridy++;
        JButton scoreboardButton = createMenuButton("Bảng Điểm", Color.CYAN);
        menuPanel.add(scoreboardButton, gbc);
        
        // Nút hướng dẫn
        gbc.gridy++;
        JButton guideButton = createMenuButton("Hướng Dẫn", Color.WHITE);
        menuPanel.add(guideButton, gbc);

        // Nút thoát
        gbc.gridy++;
        JButton exitButton = createMenuButton("Thoát", new Color(255, 80, 80));
        menuPanel.add(exitButton, gbc);

        frame.getContentPane().removeAll();
        frame.add(menuPanel);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);

        // Xử lý sự kiện cho các nút
        easyButton.addActionListener(e -> startGame(frame, 1));
        mediumButton.addActionListener(e -> startGame(frame, 2));
        hardButton.addActionListener(e -> startGame(frame, 3));
        scoreboardButton.addActionListener(e -> showScoreboardMenu(frame, width, height));
        guideButton.addActionListener(e -> showGuide());
        exitButton.addActionListener(e -> System.exit(0));
    }

    // Tạo icon pacman đơn giản
    private static ImageIcon createPacmanIcon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Vẽ hình tròn màu vàng
        g2d.setColor(Color.YELLOW);
        g2d.fillArc(0, 0, size, size, 45, 270);
        
        g2d.dispose();
        return new ImageIcon(image);
    }

    // Menu chọn bảng điểm theo độ khó
    private static void showScoreboardMenu(JFrame frame, int width, int height) {
        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(0, 0, 50), 
                    0, height, new Color(0, 0, 100)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
            }
        };
        
        menuPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // Tiêu đề
        JLabel titleLabel = new JLabel("CHỌN BẢNG ĐIỂM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.YELLOW);
        menuPanel.add(titleLabel, gbc);

        // Nút Bảng điểm độ khó DỄ
        gbc.gridy++;
        JButton easyButton = createMenuButton("Bảng Điểm DỄ", Color.GREEN);
        menuPanel.add(easyButton, gbc);

        // Nút Bảng điểm độ khó TRUNG BÌNH
        gbc.gridy++;
        JButton mediumButton = createMenuButton("Bảng Điểm TRUNG BÌNH", Color.YELLOW);
        menuPanel.add(mediumButton, gbc);

        // Nút Bảng điểm độ khó KHÓ
        gbc.gridy++;
        JButton hardButton = createMenuButton("Bảng Điểm KHÓ", Color.RED);
        menuPanel.add(hardButton, gbc);

        // Nút Quay lại
        gbc.gridy++;
        JButton backButton = createMenuButton("Quay Lại", Color.WHITE);
        menuPanel.add(backButton, gbc);

        frame.getContentPane().removeAll();
        frame.add(menuPanel);
        frame.revalidate();
        frame.repaint();

        // Xử lý sự kiện
        easyButton.addActionListener(e -> showScoreboard(1));
        mediumButton.addActionListener(e -> showScoreboard(2));
        hardButton.addActionListener(e -> showScoreboard(3));
        backButton.addActionListener(e -> showMainMenu(frame, width, height));
    }

    // Phương thức khởi động game với độ khó
    private static void startGame(JFrame frame, int difficulty) {
        difficultyLevel = difficulty;
        
        frame.getContentPane().removeAll();
        PacMan pacmanGame = new PacMan(difficulty);
        pacmanGame.setScoreCallback(new PacMan.ScoreCallback() {
            @Override
            public void onGameOver(int finalScore) {
                checkHighScore(frame, difficulty, finalScore);
            }
        });
        
        frame.add(pacmanGame);
        frame.pack();
        pacmanGame.requestFocus();
        frame.setVisible(true);
        frame.revalidate();
    }

    // Kiểm tra xem điểm có phải là điểm cao không
    private static void checkHighScore(JFrame frame, int difficulty, int score) {
        ArrayList<ScoreEntry> highScores = readHighScores(difficulty);
        
        // Kiểm tra xem điểm có đủ cao để vào top không
        boolean isHighScore = highScores.size() < MAX_HIGH_SCORES || score > highScores.get(highScores.size() - 1).getScore();
        
        if (isHighScore) {
            String playerName = promptForName("Chúc mừng! Bạn đã đạt điểm cao.\nNhập tên của bạn:");
            if (playerName != null && !playerName.trim().isEmpty()) {
                // Thêm điểm mới và lưu
                highScores.add(new ScoreEntry(playerName, score));
                Collections.sort(highScores);
                
                // Giới hạn số lượng điểm cao
                if (highScores.size() > MAX_HIGH_SCORES) {
                    highScores = new ArrayList<>(highScores.subList(0, MAX_HIGH_SCORES));
                }
                
                saveHighScores(difficulty, highScores);
                
                // Hiển thị bảng điểm sau khi lưu
                showScoreboard(difficulty);
            }
        }
        
        // Sau khi xử lý điểm cao, quay lại menu chính
        showMainMenu(frame, frame.getWidth(), frame.getHeight());
    }

    // Hiện hộp thoại yêu cầu nhập tên
    private static String promptForName(String message) {
        return JOptionPane.showInputDialog(null, message, "Điểm Cao Mới", JOptionPane.INFORMATION_MESSAGE);
    }

    // Hiển thị bảng điểm
    private static void showScoreboard(int difficulty) {
        ArrayList<ScoreEntry> highScores = readHighScores(difficulty);
        
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        scorePanel.setBackground(Color.BLACK);
        scorePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Tiêu đề với độ khó
        JLabel titleLabel = new JLabel("BẢNG ĐIỂM CAO - " + DIFFICULTY_NAMES[difficulty], SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scorePanel.add(titleLabel);
        scorePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        if (highScores.isEmpty()) {
            JLabel noScoreLabel = new JLabel("Chưa có điểm nào được ghi nhận", SwingConstants.CENTER);
            noScoreLabel.setFont(new Font("Arial", Font.PLAIN, 18));
            noScoreLabel.setForeground(Color.WHITE);
            noScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            scorePanel.add(noScoreLabel);
        } else {
            // Tạo bảng điểm
            String[] columnNames = {"Hạng", "Tên", "Điểm"};
            Object[][] data = new Object[highScores.size()][3];
            
            for (int i = 0; i < highScores.size(); i++) {
                ScoreEntry entry = highScores.get(i);
                data[i][0] = i + 1;
                data[i][1] = entry.getName();
                data[i][2] = entry.getScore();
            }
            
            JTable scoreTable = new JTable(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            // Cấu hình bảng
            scoreTable.setBackground(Color.BLACK);
            scoreTable.setForeground(Color.WHITE);
            scoreTable.setGridColor(new Color(50, 50, 50));
            scoreTable.setRowHeight(30);
            scoreTable.getTableHeader().setBackground(new Color(30, 30, 100));
            scoreTable.getTableHeader().setForeground(Color.YELLOW);
            scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
            
            // Canh giữa các cột
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);
            for (int i = 0; i < scoreTable.getColumnCount(); i++) {
                scoreTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
            
            // Chỉnh kích thước cột
            scoreTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            scoreTable.getColumnModel().getColumn(1).setPreferredWidth(200);
            scoreTable.getColumnModel().getColumn(2).setPreferredWidth(100);
            
            JScrollPane scrollPane = new JScrollPane(scoreTable);
            scrollPane.setPreferredSize(new Dimension(350, Math.min(highScores.size() * 30 + 30, 300)));
            scrollPane.setBackground(Color.BLACK);
            scrollPane.getViewport().setBackground(Color.BLACK);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 150), 2));
            
            scorePanel.add(scrollPane);
        }

        JOptionPane.showMessageDialog(
            null, 
            scorePanel, 
            "Bảng Điểm Cao - " + DIFFICULTY_NAMES[difficulty], 
            JOptionPane.PLAIN_MESSAGE
        );
    }

    // Đọc điểm cao từ file theo độ khó
    private static ArrayList<ScoreEntry> readHighScores(int difficulty) {
        ArrayList<ScoreEntry> scores = new ArrayList<>();
        
        try {
            File file = new File(DIFFICULTY_FILES[difficulty]);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    try {
                        String line = scanner.nextLine();
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            String name = parts[0];
                            int score = Integer.parseInt(parts[1]);
                            scores.add(new ScoreEntry(name, score));
                        }
                    } catch (Exception e) {
                        // Bỏ qua dòng không hợp lệ
                    }
                }
                scanner.close();
                
                // Sắp xếp giảm dần
                Collections.sort(scores);
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi đọc file điểm: " + e.getMessage());
        }
        
        return scores;
    }
    
    // Lưu điểm cao vào file theo độ khó
    private static void saveHighScores(int difficulty, ArrayList<ScoreEntry> scores) {
        try {
            File file = new File(DIFFICULTY_FILES[difficulty]);
            PrintWriter writer = new PrintWriter(file);
            
            for (ScoreEntry entry : scores) {
                writer.println(entry.getName() + ":" + entry.getScore());
            }
            
            writer.close();
        } catch (Exception e) {
            System.out.println("Lỗi khi ghi file điểm: " + e.getMessage());
        }
    }

    // Hiển thị hướng dẫn chơi Pac-Man
    private static void showGuide() {
        JPanel guidePanel = new JPanel();
        guidePanel.setLayout(new BoxLayout(guidePanel, BoxLayout.Y_AXIS));
        guidePanel.setBackground(Color.BLACK);
        guidePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề
        JLabel titleLabel = new JLabel("HƯỚNG DẪN CHƠI PAC-MAN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        guidePanel.add(titleLabel);
        guidePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Nội dung hướng dẫn với định dạng màu sắc
        String[] guideContent = {
            "<html><b><font color='yellow'>Mục tiêu:</font></b> Điều khiển Pac-Man ăn hết tất cả các chấm trắng trên màn hình</html>",
            "<html>và tránh va chạm với các ma.</html>",
            "",
            "<html><b><font color='yellow'>Điều khiển:</font></b></html>",
            "<html>- Mũi tên <font color='cyan'>LÊN</font>: Di chuyển lên trên</html>",
            "<html>- Mũi tên <font color='cyan'>XUỐNG</font>: Di chuyển xuống dưới</html>",
            "<html>- Mũi tên <font color='cyan'>TRÁI</font>: Di chuyển sang trái</html>",
            "<html>- Mũi tên <font color='cyan'>PHẢI</font>: Di chuyển sang phải</html>",
            "",
            "<html><b><font color='yellow'>Các phím tắt khi đang chơi:</font></b></html>",
            "<html>- Phím <font color='green'>1</font>: Chuyển sang độ khó DỄ</html>",
            "<html>- Phím <font color='yellow'>2</font>: Chuyển sang độ khó TRUNG BÌNH</html>",
            "<html>- Phím <font color='red'>3</font>: Chuyển sang độ khó KHÓ</html>",
            "<html>- Phím <font color='white'>ESC</font>: Thoát game</html>",
            "",
            "<html><font color='pink'>Khi thua cuộc, nhấn phím bất kỳ để chơi lại.</font></html>"
        };

        for (String content : guideContent) {
            JLabel contentLabel = new JLabel(content);
            contentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            contentLabel.setForeground(Color.WHITE);
            contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            guidePanel.add(contentLabel);
            guidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JOptionPane.showMessageDialog(
            null, 
            guidePanel, 
            "Hướng Dẫn Chơi", 
            JOptionPane.PLAIN_MESSAGE
        );
    }

    // Phương thức tạo nút với giao diện đẹp
    private static JButton createMenuButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ hiệu ứng ánh sáng
                if (getModel().isPressed()) {
                    g2.setColor(color.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                // Vẽ viền sáng
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawRoundRect(3, 3, getWidth()-6, getHeight()-6, 30, 30);
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(280, 60));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        // Hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        return button;
    }
    
    // Lớp lưu trữ thông tin điểm số
    private static class ScoreEntry implements Comparable<ScoreEntry> {
        private String name;
        private int score;
        
        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
        
        public String getName() {
            return name;
        }
        
        public int getScore() {
            return score;
        }
        
        @Override
        public int compareTo(ScoreEntry other) {
            // Sắp xếp giảm dần theo điểm
            return Integer.compare(other.score, this.score);
        }
    }
}