package code;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        Application.launch(App.class);
    }

    public static class App extends Application {

        public static final float WIDTH = 10;
        public static final float HEIGHT = 8;
        public static final float SQUARE_SIDE = 20;
        private static final String[] FOODS_IMAGES = new String[]{
                "/ic_orange.png",
                "/ic_apple.png",
                "/ic_cherry.png",
                "/ic_berry.png",
                "/ic_coconut_.png",
                "/ic_orange.png"
        };

        private Pane root;
        private Stage stage;
        private Scene scene;
        private List<SnakeBody> snakeBody;
        private Image foodImage;
        private Rectangle food;
        private float speed;
        private float foodX;
        private float foodY;
        private boolean gameOver;
        private Dir currentDirection;
        private boolean tickCheck; // to prevent quick key pressing
        private AnimationTimer timer;

        public void setGame() {
            resetVariables();
            setPane();
            setControls();
            setAnimationTimer();
            setSnakeHead();
            generateFood();
        }

        public void resetVariables() {
            snakeBody = new ArrayList<>();
            foodX = 0;
            foodY = 0;
            speed = 5;
            currentDirection = Dir.LEFT;
            gameOver = false;
            tickCheck = false;
        }

        public void setPane() {
            root = new Pane();
            stage = new Stage();
            root.setStyle("-fx-background-color: #caf2a7;");
            scene = new Scene(root, (WIDTH + 1) * SQUARE_SIDE, (HEIGHT + 1) * SQUARE_SIDE);

        }

        public void setSnakeHead() {
            SnakeBody head = new SnakeBody(WIDTH / 2 * SQUARE_SIDE, HEIGHT / 2 * SQUARE_SIDE);
            head.setColor(Color.RED);
            snakeBody.add(head);
        }

        public void setControls() {
            scene.setOnKeyPressed(event -> {
                if (!tickCheck) {
                    switch (event.getCode()) {
                        case W, UP:
                            if (currentDirection != Dir.DOWN) {
                                currentDirection = Dir.UP;
                                tickCheck = true;
                            }
                            break;
                        case S, DOWN:
                            if (currentDirection != Dir.UP) {
                                currentDirection = Dir.DOWN;
                                tickCheck = true;
                            }
                            break;
                        case D, RIGHT:
                            if (currentDirection != Dir.LEFT) {
                                currentDirection = Dir.RIGHT;
                                tickCheck = true;
                            }
                            break;
                        case A, LEFT:
                            if (currentDirection != Dir.RIGHT) {
                                currentDirection = Dir.LEFT;
                                tickCheck = true;
                            }
                            break;
                    }
                }
            });
        }

        public void setAnimationTimer() {
            timer = new AnimationTimer() {
                long lastTick = 0;

                public void handle(long now) {
                    if (lastTick == 0) {
                        lastTick = now;
                        tick();
                        return;
                    }
                    if (now - lastTick > 1000000000 / speed) {
                        lastTick = now;
                        tick();
                    }
                }
            };
            timer.start();
        }

        public void checkGameOver() {
            if (gameOver) {
                speed = 0;
                Pane pane = new Pane();
                Stage restartStage = new Stage();
                Scene restart = new Scene(pane, 200, 100);
                Button restartButton = new Button("Restart");
                restartButton.setTranslateX(60);
                restartButton.setTranslateY(35);
                restartButton.setPrefSize(80, 30);
                restartButton.setOnAction(event -> {
                    stage.close();
                    restartStage.close();
                    timer.stop();
                    launchGame();
                });
                pane.getChildren().add(restartButton);
                restartStage.setScene(restart);
                restartStage.show();
            }
        }

        public void moveSnakeBody() {
            for (int i = snakeBody.size() - 1; i >= 1; i--) {
                snakeBody.get(i).setX(snakeBody.get(i - 1).getX());
                snakeBody.get(i).setY(snakeBody.get(i - 1).getY());
                snakeBody.get(i).updateCords();
            }
        }

        public void tick() {
            tickCheck = false;
            checkGameOver();
            moveSnakeBody();
            moveSnakeHead();
            eatFood();
            destroySnake();
        }

        private void destroySnake() {
            for (int i = 1; i < snakeBody.size(); i++) {
                if (snakeBody.get(0).getX() == snakeBody.get(i).getX() && snakeBody.get(0).getY() == snakeBody.get(i).getY()) {
                    gameOver = true;
                    break;
                }
            }
        }

        private void eatFood() {
            if (foodX == snakeBody.get(0).getX() && foodY == snakeBody.get(0).getY()) {
                snakeBody.add(new SnakeBody(-1 * SQUARE_SIDE, -1 * SQUARE_SIDE));
                root.getChildren().remove(food);
                speed += 0.5;
                generateFood();
            }
        }

        private void moveSnakeHead() {
            switch (currentDirection) {
                case UP -> {
                    snakeBody.get(0).y -= SQUARE_SIDE;
                    if (snakeBody.get(0).y < 0) {
                        snakeBody.get(0).y = HEIGHT * SQUARE_SIDE;
                    }
                }
                case DOWN -> {
                    snakeBody.get(0).y += SQUARE_SIDE;
                    if (snakeBody.get(0).y > HEIGHT * SQUARE_SIDE) {
                        snakeBody.get(0).y = 0;
                    }
                }
                case LEFT -> {
                    snakeBody.get(0).x -= SQUARE_SIDE;
                    if (snakeBody.get(0).x < 0) {
                        snakeBody.get(0).x = WIDTH * SQUARE_SIDE;
                    }
                }
                case RIGHT -> {
                    snakeBody.get(0).x += SQUARE_SIDE;
                    if (snakeBody.get(0).x > WIDTH * SQUARE_SIDE) {
                        snakeBody.get(0).x = 0;
                    }
                }
            }
            snakeBody.get(0).updateCords();
        }

        private void generateFood() {
            start:
            while (true) {
                foodX = ((int) (Math.random() * (WIDTH - 1))) * SQUARE_SIDE;
                foodY = ((int) (Math.random() * (HEIGHT - 1))) * SQUARE_SIDE;
                for (SnakeBody snake : snakeBody) {
                    if (snake.getX() == foodX && snake.getY() == foodY) {
                        continue start;
                    }
                }
                foodImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(FOODS_IMAGES[(int) (Math.random() * FOODS_IMAGES.length)])));
                break;
            }
            drawFood();
        }

        private void drawFood() {
            food = new Rectangle(foodX, foodY, SQUARE_SIDE, SQUARE_SIDE);
            food.setFill(new ImagePattern(foodImage));
            root.getChildren().add(food);
        }

        @Override
        public void start(Stage primaryStage) {
            launchGame();
        }

        public void launchGame() {
            try {
                setGame();
                Stage primaryStage = stage;
                primaryStage.setScene(scene);
                primaryStage.setTitle("Snake");
                InputStream iconStream = getClass().getResourceAsStream("/snake-icon.png");
                Image icon = null;
                if (iconStream != null) {
                    icon = new Image(iconStream);
                }
                primaryStage.getIcons().add(icon);
                primaryStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public enum Dir {
            LEFT,
            RIGHT,
            UP,
            DOWN
        }

        public class SnakeBody {
            private final Circle circle;
            private float x;
            private float y;

            public SnakeBody(float x, float y) {
                this.x = x;
                this.y = y;
                circle = new Circle(x, y, SQUARE_SIDE / 2);
                circle.setFill(Color.GREEN);
                root.getChildren().add(circle);
            }

            public void updateCords() {
                circle.setCenterX(x + SQUARE_SIDE / 2);
                circle.setCenterY(y + SQUARE_SIDE / 2);
            }

            public float getX() {
                return x;
            }

            public void setX(float x) {
                this.x = x;
            }

            public float getY() {
                return y;
            }

            public void setY(float y) {
                this.y = y;
            }

            public void setColor(Color color) {
                circle.setFill(color);
            }
        }
    }
}