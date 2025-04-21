import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    // Interface callback cho điểm số
    public interface ScoreCallback {
        void onGameOver(int finalScore);
    }
    
    private ScoreCallback scoreCallback;
    
    // Thiết lập callback
    public void setScoreCallback(ScoreCallback callback) {
        this.scoreCallback = callback;
    }

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        char lastDirection = 'U'; // Lưu trữ hướng đi trước đó
        int directionChangeCounter = 0; // Đếm số bước đi theo cùng một hướng
        private final int MAX_SAME_DIRECTION = 10; // Số lượng bước tối đa theo cùng một hướng
        
        int velocityX = 0;
        int velocityY = 0;

        public enum Difficulty { EASY, MEDIUM, HARD }
        private Difficulty difficulty = Difficulty.EASY; // Mặc định EASY
        private double speedMultiplier = 1.0;


        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;

            // Kiểm tra nếu ghost đi cùng hướng quá lâu
            if (this.direction == lastDirection) {
                directionChangeCounter++;
                if (directionChangeCounter > MAX_SAME_DIRECTION) {
                    // Chọn hướng ngẫu nhiên khác với hướng hiện tại
                    char[] possibleDirections = getPossibleDirections();
                    if (possibleDirections.length > 0) {
                        this.direction = possibleDirections[new Random().nextInt(possibleDirections.length)];
                        directionChangeCounter = 0;
                    }
                }
            } else {
                lastDirection = this.direction;
                directionChangeCounter = 0;
            }
            
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            
            // Kiểm tra va chạm với tường
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        // Trả về các hướng có thể đi (khác hướng hiện tại và hướng đối diện)
        private char[] getPossibleDirections() {
            ArrayList<Character> possibleDirs = new ArrayList<>();
            char[] allDirs = {'U', 'D', 'L', 'R'};
            
            for (char dir : allDirs) {
                if (dir != direction && dir != getOppositeDirection(direction)) {
                    possibleDirs.add(dir);
                }
            }
            
            char[] result = new char[possibleDirs.size()];
            for (int i = 0; i < possibleDirs.size(); i++) {
                result[i] = possibleDirs.get(i);
            }
            return result;
        }

        // Trả về hướng đối diện
        private char getOppositeDirection(char dir) {
            switch (dir) {
                case 'U': return 'D';
                case 'D': return 'U';
                case 'L': return 'R';
                case 'R': return 'L';
                default: return dir;
            }
        }

        public void setDifficulty(Difficulty newDifficulty) {
            difficulty = newDifficulty;
            switch (difficulty) {
                case EASY:
                    speedMultiplier = 1.0; 
                    break;
                case MEDIUM:
                    speedMultiplier = 1.1;
                    break;
                case HARD:
                    speedMultiplier = 1.2; 
                    break;
            }
        }
        
        void updateVelocity() {
            // Tốc độ cơ bản là 1/5 kích thước ô thay vì 1/8
            double baseSpeed = tileSize / 5.0;
            int speed = (int) Math.round(baseSpeed * speedMultiplier);
            
            // Đảm bảo tốc độ tối thiểu là 1
            if (speed < 1) speed = 1;
            
            switch (this.direction) {
                case 'U': velocityX = 0; velocityY = -speed; break;
                case 'D': velocityX = 0; velocityY = speed; break;
                case 'L': velocityX = -speed; velocityY = 0; break;
                case 'R': velocityX = speed; velocityY = 0; break;
            }
        }
        
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;
    private int difficultyLevel = 1; // 1: DỄ, 2: TRUNG BÌNH, 3: KHÓ
    
    // Tên file lưu điểm tương ứng với độ khó
    private static final String[] DIFFICULTY_FILES = {"", "score_easy.txt", "score_medium.txt", "score_hard.txt"};

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    javax.swing.Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    // Constructor với tham số độ khó
    PacMan(int difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        
        // Thiết lập độ khó cho ma dựa trên tham số
        Block.Difficulty ghostDifficulty;
        switch(difficultyLevel) {
            case 1: ghostDifficulty = Block.Difficulty.EASY; break;
            case 2: ghostDifficulty = Block.Difficulty.MEDIUM; break;
            case 3: ghostDifficulty = Block.Difficulty.HARD; break;
            default: ghostDifficulty = Block.Difficulty.EASY;
        }
        
        for (Block ghost : ghosts) {
            ghost.setDifficulty(ghostDifficulty);
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new javax.swing.Timer(50, this); //20fps (1000/50)
        gameLoop.start();
    }
    
    // Constructor không tham số cho tương thích ngược
    PacMan() {
        this(1); // Mặc định độ khó DỄ
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    int offset = (tileSize - tileSize) / 2;
                    pacman = new Block(pacmanRightImage, x + offset, y + offset, tileSize, tileSize);
                }                
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Vẽ gradient nền
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(0, 0, 20), 
            0, boardHeight, new Color(0, 0, 60)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, boardWidth, boardHeight);
        
        // Vẽ các thành phần game
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        
        g.setColor(new Color(255, 255, 255, 200)); // Thức ăn màu trắng với độ trong suốt
        for (Block food : foods) {
            g.fillOval(food.x, food.y, food.width, food.height);
        }
        
        // Vẽ hiệu ứng ánh sáng xung quanh Pac-Man
        int glowSize = 20;
        int glowX = pacman.x - glowSize/2 + pacman.width/2;
        int glowY = pacman.y - glowSize/2 + pacman.height/2;
        
        // Vẽ hiệu ứng phát sáng
        RadialGradientPaint pacGlow = new RadialGradientPaint(
            pacman.x + pacman.width/2, 
            pacman.y + pacman.height/2, 
            glowSize*2,
            new float[]{0.0f, 1.0f},
            new Color[]{new Color(255, 255, 0, 90), new Color(255, 255, 0, 0)}
        );
        g2d.setPaint(pacGlow);
        g2d.fillOval(glowX - glowSize/2, glowY - glowSize/2, 
                  pacman.width + glowSize, pacman.height + glowSize);

        // Vẽ Pac-Man và ma
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Hiển thị thông tin trò chơi với giao diện đẹp hơn
        g.setColor(new Color(0, 0, 0, 180)); // Nền tối mờ
        g.fillRect(0, 0, boardWidth, 32);
        
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        
        // Vẽ biểu tượng mạng sống
        for (int i = 0; i < lives; i++) {
            g.drawImage(pacmanRightImage, 10 + i*25, 8, 20, 20, null);
        }
        
        // Hiển thị điểm số
        String scoreText = "SCORE: " + score;
        FontMetrics fm = g.getFontMetrics();
        int scoreWidth = fm.stringWidth(scoreText);
        g.drawString(scoreText, boardWidth - scoreWidth - 20, 24);
        
        // Hiển thị độ khó hiện tại
        String difficultText = "";
        switch(difficultyLevel) {
            case 1: difficultText = "EASY"; g.setColor(Color.GREEN); break;
            case 2: difficultText = "MEDIUM"; g.setColor(Color.YELLOW); break;
            case 3: difficultText = "HARD"; g.setColor(Color.RED); break;
        }
        g.drawString(difficultText, boardWidth / 2 - fm.stringWidth(difficultText) / 2, 24);
        
        // Hiển thị màn hình kết thúc trò chơi
        if (gameOver) {
            drawGameOver(g);
        }
    }
    
    // Phương thức vẽ màn hình kết thúc
    private void drawGameOver(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Vẽ lớp mờ nền
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, boardWidth, boardHeight);
        
        // Viền khung thông báo
        int boxWidth = 400;
        int boxHeight = 200;
        int boxX = (boardWidth - boxWidth) / 2;
        int boxY = (boardHeight - boxHeight) / 2;
        
        // Vẽ khung với hiệu ứng ánh sáng
        GradientPaint gradientBorder = new GradientPaint(
            boxX, boxY, new Color(255, 255, 0), 
            boxX + boxWidth, boxY + boxHeight, new Color(255, 140, 0)
        );
        g2d.setPaint(gradientBorder);
        g2d.fillRoundRect(boxX - 5, boxY - 5, boxWidth + 10, boxHeight + 10, 30, 30);
        
        // Nền của khung thông báo
        g2d.setColor(new Color(30, 30, 70));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        
        // Vẽ "GAME OVER" với hiệu ứng bóng đổ
        Font gameOverFont = new Font("Arial", Font.BOLD, 48);
        g2d.setFont(gameOverFont);
        FontMetrics fm = g2d.getFontMetrics();
        
        // Bóng đổ
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString("GAME OVER", 
                     boxX + (boxWidth - fm.stringWidth("GAME OVER")) / 2 + 3, 
                     boxY + 70 + 3);
        
        // Văn bản chính
        g2d.setColor(Color.RED);
        g2d.drawString("GAME OVER", 
                     boxX + (boxWidth - fm.stringWidth("GAME OVER")) / 2, 
                     boxY + 70);
        
        // Hiển thị điểm số
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        fm = g2d.getFontMetrics();
        String finalScore = "Score: " + score;
        
        g2d.setColor(Color.WHITE);
        g2d.drawString(finalScore, 
                     boxX + (boxWidth - fm.stringWidth(finalScore)) / 2, 
                     boxY + 130);
        
        // Thông báo tiếp tục
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g2d.getFontMetrics();
        String continueText = "Press any key to continue";
        
        g2d.setColor(Color.YELLOW);
        g2d.drawString(continueText, 
                     boxX + (boxWidth - fm.stringWidth(continueText)) / 2, 
                     boxY + 180);
    }

    // Thêm vào lớp PacMan một phương thức mới để kiểm tra số đường đi có thể từ một vị trí
    private int countAvailablePaths(int x, int y) {
        int tileX = x / tileSize;
        int tileY = y / tileSize;
        int count = 0;
        
        // Kiểm tra bốn hướng xung quanh
        if (tileY > 0 && !isWallAt(tileX, tileY - 1)) count++; // Up
        if (tileY < rowCount - 1 && !isWallAt(tileX, tileY + 1)) count++; // Down
        if (tileX > 0 && !isWallAt(tileX - 1, tileY)) count++; // Left
        if (tileX < columnCount - 1 && !isWallAt(tileX + 1, tileY)) count++; // Right
        
        return count;
    }

    // Kiểm tra xem có tường ở vị trí cụ thể không
    private boolean isWallAt(int tileX, int tileY) {
        if (tileX < 0 || tileX >= columnCount || tileY < 0 || tileY >= rowCount) {
            return true; // Ngoài giới hạn bản đồ
        }
        return tileMap[tileY].charAt(tileX) == 'X';
    }

    // Thêm phương thức để xác định các hướng có thể đi từ một vị trí
    private ArrayList<Character> getAvailableDirections(int x, int y, char currentDirection) {
        int tileX = x / tileSize;
        int tileY = y / tileSize;
        ArrayList<Character> availableDirections = new ArrayList<>();
        
        // Không quay lại (tránh đi ngược hướng hiện tại)
        char oppositeDir = getOppositeDirection(currentDirection);
        
        // Kiểm tra bốn hướng
        if (tileY > 0 && !isWallAt(tileX, tileY - 1) && 'U' != oppositeDir) 
            availableDirections.add('U');
        if (tileY < rowCount - 1 && !isWallAt(tileX, tileY + 1) && 'D' != oppositeDir) 
            availableDirections.add('D');
        if (tileX > 0 && !isWallAt(tileX - 1, tileY) && 'L' != oppositeDir) 
            availableDirections.add('L');
        if (tileX < columnCount - 1 && !isWallAt(tileX + 1, tileY) && 'R' != oppositeDir) 
            availableDirections.add('R');
        
        return availableDirections;
    }

    // Cập nhật phương thức move() trong lớp PacMan
    public void move() {
        int remainderX = pacman.x % tileSize;
        int remainderY = pacman.y % tileSize;

        if (Math.abs(remainderX) < 4 && Math.abs(remainderY) < 4) {
            pacman.x = Math.round(pacman.x / (float)tileSize) * tileSize;
            pacman.y = Math.round(pacman.y / (float)tileSize) * tileSize;
        }
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        if (pacman.x < 0) {
            pacman.x = boardWidth - tileSize; 
        } else if (pacman.x >= boardWidth) {
            pacman.x = 0;
        }

        if (pacman.y < 0) {
            pacman.y = boardHeight - tileSize; 
        } else if (pacman.y >= boardHeight) {
            pacman.y = 0; 
        }

        // Kiểm tra va chạm với tường
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Kiểm tra va chạm với ghost
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    // Gọi callback điểm số thay vì lưu trực tiếp
                    if (scoreCallback != null) {
                        scoreCallback.onGameOver(score);
                    }
                    return;
                }
                resetPositions();
            }

            // Di chuyển ghost và xử lý logic ngã rẽ
            moveGhostWithPathDetection(ghost);
        }

        // Kiểm tra va chạm với thức ăn
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
                
                // Hiệu ứng âm thanh (bằng cách sử dụng Toolkit)
                Toolkit.getDefaultToolkit().beep();
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            // Hoàn thành màn chơi, tăng điểm thưởng
            score += 50 * difficultyLevel; // Thưởng thêm dựa trên độ khó
            loadMap();
            resetPositions();
        }
    }
    
    // Phương thức mới để xử lý di chuyển của ghost với khả năng nhận biết ngã rẽ
    private void moveGhostWithPathDetection(Block ghost) {
        // Lưu vị trí hiện tại
        int oldX = ghost.x;
        int oldY = ghost.y;
        
        // Di chuyển ghost theo hướng hiện tại
        ghost.x += ghost.velocityX;
        ghost.y += ghost.velocityY;
        
        // Thêm cơ chế wrap-around cho ghost
        if (ghost.x < 0) {
            ghost.x = boardWidth - tileSize;
        } else if (ghost.x >= boardWidth) {
            ghost.x = 0;
        }
    
        if (ghost.y < 0) {
            ghost.y = boardHeight - tileSize;
        } else if (ghost.y >= boardHeight) {
            ghost.y = 0;
        }
    
        // Kiểm tra va chạm với tường
        boolean hitWall = false;
        for (Block wall : walls) {
            if (collision(ghost, wall)) {
                // Quay lại vị trí cũ và đổi hướng
                ghost.x = oldX;
                ghost.y = oldY;
                hitWall = true;
                
                ArrayList<Character> availableDirections = getAvailableDirections(ghost.x, ghost.y, ghost.direction);
                if (!availableDirections.isEmpty()) {
                    char newDirection = availableDirections.get(random.nextInt(availableDirections.size()));
                    ghost.updateDirection(newDirection);
                } else {
                    // Nếu không có hướng khác, quay lại
                    ghost.updateDirection(getOppositeDirection(ghost.direction));
                }
                break;
            }
        }
        
        // Nếu không đụng tường, kiểm tra xem ghost có ở ngã rẽ không
        if (!hitWall) {
            // Kiểm tra căn chỉnh với ô - nới lỏng điều kiện để tránh đơ
            boolean isAlignedWithTile = 
                (ghost.x % tileSize <= tileSize/3 || ghost.x % tileSize >= 2*tileSize/3) && 
                (ghost.y % tileSize <= tileSize/3 || ghost.y % tileSize >= 2*tileSize/3);
            
            if (isAlignedWithTile) {
                // Đếm số đường đi có thể
                int pathCount = countAvailablePaths(ghost.x, ghost.y);
                
                // Nếu là ngã rẽ (có nhiều hơn 1 đường đi)
                if (pathCount > 1) {
                    // 20% cơ hội đổi hướng tại ngã rẽ (giảm từ 30%)
                    if (random.nextInt(100) < 20) {
                        ArrayList<Character> availableDirections = getAvailableDirections(ghost.x, ghost.y, ghost.direction);
                        if (!availableDirections.isEmpty()) {
                            char newDirection = availableDirections.get(random.nextInt(availableDirections.size()));
                            ghost.updateDirection(newDirection);
                            ghost.directionChangeCounter = 0;
                        }
                    }
                }
            }
            
            // Tăng bộ đếm số bước đi theo cùng một hướng
            ghost.directionChangeCounter++;
            
            // Nếu đã đi quá lâu theo cùng một hướng
            if (ghost.directionChangeCounter > ghost.MAX_SAME_DIRECTION) {
                // 20% cơ hội đổi hướng (giảm từ 25%)
                if (random.nextInt(100) < 20) {
                    ArrayList<Character> availableDirections = getAvailableDirections(ghost.x, ghost.y, ghost.direction);
                    if (!availableDirections.isEmpty()) {
                        char newDirection = availableDirections.get(random.nextInt(availableDirections.size()));
                        ghost.updateDirection(newDirection);
                        ghost.directionChangeCounter = 0;
                    }
                }
            }
        }
    }
    public boolean closeGhosts(Block ghost1, Block ghost2, int minDistance) {
        // Tính khoảng cách tâm giữa hai ghost
        int centerX1 = ghost1.x + ghost1.width/2;
        int centerY1 = ghost1.y + ghost1.height/2;
        int centerX2 = ghost2.x + ghost2.width/2;
        int centerY2 = ghost2.y + ghost2.height/2;
        
        // Tính khoảng cách Euclidean
        double distance = Math.sqrt(Math.pow(centerX1 - centerX2, 2) + Math.pow(centerY1 - centerY2, 2));
        
        return distance < minDistance;
    }

    // Nhận hướng đối diện
    private char getOppositeDirection(char dir) {
        switch (dir) {
            case 'U': return 'D';
            case 'D': return 'U';
            case 'L': return 'R';
            case 'R': return 'L';
            default: return dir;
        }
    }

    // Cải thiện phát hiện va chạm bằng cách thêm dung sai
    public boolean collision(Block a, Block b) {
        int tolerance = 2; // dung sai nhỏ
        return  a.x + tolerance < b.x + b.width &&
                a.x + a.width - tolerance > b.x &&
                a.y + tolerance < b.y + b.height &&
                a.y + a.height - tolerance > b.y;
    }
    
    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            ghost.updateDirection(directions[random.nextInt(4)]);
            ghost.updateVelocity();
        }
    }
    
    // Lưu điểm cao vào file
    private void saveHighScore() {
        try {
            ArrayList<Integer> scores = new ArrayList<>();
            File file = new File("score.txt");
            
            // Đọc các điểm hiện có
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextInt()) {
                    scores.add(scanner.nextInt());
                }
                scanner.close();
            }
            
            // Thêm điểm mới
            scores.add(score);
            
            // Sắp xếp giảm dần
            Collections.sort(scores, Collections.reverseOrder());
            
            // Chỉ giữ tối đa 5 điểm cao nhất
            if (scores.size() > 5) {
                scores = new ArrayList<>(scores.subList(0, 5));
            }
            
            // Ghi lại vào file
            PrintWriter writer = new PrintWriter(file);
            for (int s : scores) {
                writer.println(s);
            }
            writer.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_1) {
            // Chuyển sang độ khó DỄ
            for (Block ghost : ghosts) {
                ghost.setDifficulty(Block.Difficulty.EASY);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_2) {
            // Chuyển sang độ khó TRUNG BÌNH
            for (Block ghost : ghosts) {
                ghost.setDifficulty(Block.Difficulty.MEDIUM);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_3) {
            // Chuyển sang độ khó KHÓ
            for (Block ghost : ghosts) {
                ghost.setDifficulty(Block.Difficulty.HARD);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        } else if (gameOver) {
            // Chơi lại khi thua
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        
        // Điều khiển hướng đi của Pac-Man
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        // Cập nhật hình ảnh Pac-Man theo hướng đi
        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }
    
}
