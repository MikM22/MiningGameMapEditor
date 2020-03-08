package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main extends Canvas implements Runnable {
    private BufferedImage[][] tiles = new BufferedImage[210][4];
    private int mapX = 26, mapY = 21;
    private int[][][] map = new int[mapX][mapY][3];
    private int[][][] rotations = new int[mapX][mapY][3];
    private final int tw = 48, actualTiles = 88;
    private BufferedImage tile;
    private int selectedTile;
    private boolean running;
    private Point mp = new Point(0,0);
    private Thread thread;
    private String[][] output = new String[mapX][mapY];
    private int camX, camY;
    private int groundTile = 48, emptyTile = 79;
    private boolean shiftHeld;
    private Dimension[] tileSizes = new Dimension[5];
    private int[] indexWhereTileStarts = new int[5];
    private boolean home = true;
    private String path = home ? "E:\\IntelliJprojects\\Arctown\\src\\com\\company\\assets\\images\\" : "C:\\Users\\mieczkowski4327\\IdeaProjects\\MiningGame\\src\\com\\company\\assets\\images\\";


    private boolean putInMapString = true;

    public static void main(String[] args) {
        new Main();
    }
    private Main() {
        String s = null;
        if (putInMapString) {
            Scanner in = new Scanner(System.in);
            System.out.println("Got string?");
            s = in.nextLine();
        }
        if (putInMapString) {
            if (!s.equalsIgnoreCase("n")) {
                s = s.substring(0, s.length() - 3);
                for (int x = 0; x < mapX; x++) {
                    for (int y = 0; y < mapY; y++) {
                        output[x][y] = "";
                    }
                }
                int i = 0;
                int j = 0;
                boolean pastComma = false;
                String fullnum = "";
                for (int x = 0; x < s.length(); x++) {
                    output[i][j] += String.valueOf(s.charAt(x));
                    if (isNumber(s.charAt(x)) && s.charAt(x) != ',') {
                        fullnum += s.charAt(x);
                    }
                    if (s.charAt(x) == 'f' || s.charAt(x) == 'w' || s.charAt(x) == 'p' || s.charAt(x) == 'c' || s.charAt(x) == 'd' || s.charAt(x) == 'b' || s.charAt(x) == 'i' || s.charAt(x) == 'y') {
                        if (pastComma) {
                            map[i][j][1] = Integer.parseInt(fullnum);
                            pastComma = false;
                        } else {
                            map[i][j][0] = Integer.parseInt(fullnum);
                        }
                        fullnum = "";
                        i++;
                        if (i + 1 > mapX) {
                            i = 0;
                            j++;
                        }
                    }
                    if (s.charAt(x) == 'r') {
                        if (pastComma) {
                            rotations[i][j][1]++;
                        } else {
                            rotations[i][j][0]++;
                        }
                    }
                    if (s.charAt(x) == ',') {
                        map[i][j][0] = Integer.parseInt(fullnum);
                        fullnum = "";
                        pastComma = true;
                    }
                }
                for (int y = 0; y < mapY; y++) {
                    for (int x = 0; x < mapX; x++) {
                        for (int e = 1; e < 3; e++) {
                            if (map[x][y][e] == 0) {
                                map[x][y][e] = emptyTile;
                            }
                        }
                    }
                }
            } else {
                for (int y = 0; y < mapY; y++) {
                    for (int x = 0; x < mapX; x++) {
                        map[x][y][0] = groundTile;
                        for (int i = 1; i < 3; i++) {
                            map[x][y][i] = emptyTile;
                        }
                        output[x][y] = groundTile + "f";
                    }
                }
            }
        } else {
            for (int y = 0; y < mapY; y++) {
                for (int x = 0; x < mapX; x++) {
                    map[x][y][0] = groundTile;
                    for (int i = 1; i < 3; i++) {
                        map[x][y][i] = emptyTile;
                    }
                    output[x][y] = groundTile + "f";
                }
            }
        }
        System.out.println(Arrays.deepToString(output));
        System.out.println(Arrays.deepToString(map));
        new Window(1000, 800, this);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseInput(e);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                mouseInput(e);
            }
        });
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_E) {
                    System.out.println(output[mp.x / tw][mp.y / tw] + " " + rotations[mp.x / tw][mp.y / tw][0] + " " + rotations[mp.x / tw][mp.y / tw][1]);
                }
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    if (shiftHeld) {
                        for (int x = 0; x < output[mp.x / tw][mp.y / tw].length(); x++) {
                            if (output[mp.x / tw][mp.y / tw].charAt(x) == ',') {
                                output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, x + 1) + "r" + output[mp.x / tw][mp.y / tw].substring(x + 1);
                                break;
                            }
                        }
                        if (rotations[mp.x / tw][mp.y / tw][1] < 3) {
                            rotations[mp.x / tw][mp.y / tw][1]++;
                        } else {
                            for (int x = 0; x < output[mp.x / tw][mp.y / tw].length(); x++) {
                                if (output[mp.x / tw][mp.y / tw].charAt(x) == ',' && output[mp.x / tw][mp.y / tw].charAt(x + 1) == 'r') {
                                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, x + 1) + output[mp.x / tw][mp.y / tw].substring(x + 5);
                                    break;
                                }
                            }
                            rotations[mp.x / tw][mp.y / tw][1] = 0;
                        }
                    } else {
                        output[mp.x / tw][mp.y / tw] = "r" + output[mp.x / tw][mp.y / tw];
                        if (rotations[mp.x / tw][mp.y / tw][0] < 3) {
                            rotations[mp.x / tw][mp.y / tw][0]++;
                        } else {
                            for (int x = 0; x < output[mp.x / tw][mp.y / tw].length(); x++) {
                                if (output[mp.x / tw][mp.y / tw].charAt(x) == 'r') {
                                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(x + 4);
                                    break;
                                }
                            }
                            rotations[mp.x / tw][mp.y / tw][0] = 0;
                        }
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_F) {
                    if (shiftHeld) {
                        //fix up
                        if (map[mp.x / tw][mp.y / tw][1] > 9) {
                            if (output[mp.x / tw][mp.y / tw].substring(3, 4).equals("x")) {
                                output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(4);
                            } else { // "3," + "x" +
                                output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, 3) + "x" + output[mp.x / tw][mp.y / tw].substring(3);
                            }
                        } else {
                            if (output[mp.x / tw][mp.y / tw].substring(2, 3).equals("x")) {
                                output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(3);
                            } else {
                                output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, 2) + "x" + output[mp.x / tw][mp.y / tw];
                            }
                        }
                    } else {
                        if (output[mp.x / tw][mp.y / tw].substring(0, 1).equals("x")) {
                            output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(1);
                        } else {
                            output[mp.x / tw][mp.y / tw] = "x" + output[mp.x / tw][mp.y / tw];
                        }
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    StringBuilder sb = new StringBuilder();
                    for (int y = 0; y < mapY; y++) {
                        for (int x = 0; x < mapX; x++) {
                            sb.append(output[x][y]);
                        }
                    }
                    System.out.println(sb.toString());
                }
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftHeld = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, output[mp.x / tw][mp.y / tw].length() - 1) + "w";
                }
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    if (!(mp.x / tw == mapX / 2 && mp.y / tw == mapY / 2))
                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, output[mp.x / tw][mp.y / tw].length() - 1) + "p";
                }
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, output[mp.x / tw][mp.y / tw].length() - 1) + "c";
                }
                if (e.getKeyCode() == KeyEvent.VK_B) {
                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, output[mp.x / tw][mp.y / tw].length() - 1) + "b";
                }
                if (e.getKeyCode() == KeyEvent.VK_D) {
                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, output[mp.x / tw][mp.y / tw].length() - 1) + "d";
                }
                if (e.getKeyCode() == KeyEvent.VK_I) {
                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, output[mp.x / tw][mp.y / tw].length() - 1) + "i";
                }
                if (e.getKeyCode() == KeyEvent.VK_Y) {
                    output[mp.x / tw][mp.y / tw] = output[mp.x / tw][mp.y / tw].substring(0, output[mp.x / tw][mp.y / tw].length() - 1) + "y";
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    camX += tw;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    camY += tw;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    camY -= tw;
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    camX -= tw;
                }
                if (e.getKeyCode() == KeyEvent.VK_G) {
                    map[mp.x / tw][mp.y / tw][1] = map[mp.x / tw][mp.y / tw][0];
                    map[mp.x / tw][mp.y / tw][0] = groundTile;
                    rotations[mp.x / tw][mp.y / tw][1] = rotations[mp.x / tw][mp.y / tw][0];
                    rotations[mp.x / tw][mp.y / tw][0] = 0;
                    output[mp.x / tw][mp.y / tw] = groundTile + "," + output[mp.x / tw][mp.y / tw];
                }
                if (e.getKeyCode() == KeyEvent.VK_X) {
                    output[mp.x / tw][mp.y / tw] = groundTile + "f";
                    map[mp.x / tw][mp.y / tw][0] = groundTile;
                    map[mp.x / tw][mp.y / tw][1] = emptyTile;
                    rotations[mp.x / tw][mp.y / tw][0] = 0;
                    rotations[mp.x / tw][mp.y / tw][1] = 0;
                }
                if (e.getKeyCode() == KeyEvent.VK_2) {
                    selectedTile--;
                    if (selectedTile < 0) {
                        selectedTile = actualTiles;
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_1) {
                    selectedTile++;
                    if (selectedTile > actualTiles) {
                        selectedTile = 0;
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_4) {
                    selectedTile -= 5;
                    if (selectedTile < 0) {
                        selectedTile += actualTiles;
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_3) {
                    selectedTile += 5;
                    if (selectedTile > actualTiles) {
                        selectedTile -= actualTiles;
                    }
                }
            }
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftHeld = false;
                }
            }
        });
        tile = loadImage("tiles", 3);
        int z = 0;
        for (int y = 0; y<10; y++) {
            for (int x = 0; x<8; x++) {
                BufferedImage img = tile.getSubimage(x * 48, y * 48, 48, 48);
                for (int i = 0; i < 4; i++) {
                    tiles[z][i] = rotateImage(img, i * 90);
                }
                z++;
            }
        }

        tileSizes[0] = new Dimension(4, 4);
        tileSizes[1] = new Dimension(2, 4);
        tileSizes[2] = new Dimension(3, 3);
        tileSizes[3] = new Dimension(3, 8);
        tileSizes[4] = new Dimension(5, 2);

        tiles[81][0] = loadImage("house", 3);
        tiles[82][0] = loadImage("farm", 3);
        tiles[83][0] = loadImage("tree", 3);
        tiles[84][0] = loadImage("wiztower", 3);
        tiles[85][0] = loadImage("stall", 3);

        tiles[86][0] = loadImage("houseRoof", 3);
        tiles[87][0] = loadImage("stallRoof", 3);
        tiles[88][0] = loadImage("tree", 3);

        z = 0;
        for (int i = 0; i < tileSizes.length; i++) {
            indexWhereTileStarts[i] = 120 + z;
            for (int x = 0; x < tileSizes[i].width; x++) {
                for (int y = 0; y < tileSizes[i].height; y++) {
                    tiles[120 + z][0] = tiles[81 + i][0].getSubimage(x * 48, y * 48, 48, 48);
                    z++;
                }
            }
        }
        System.out.println(Arrays.toString(indexWhereTileStarts));
        start();
    }

    private void mouseInput(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (selectedTile > 80 && selectedTile < 86) {
                int z = 0;
                for (int x = 0; x < tileSizes[selectedTile - 81].width; x++) {
                    for (int y = 0; y < tileSizes[selectedTile - 81].height; y++) {
                        map[mp.x / tw + x][mp.y / tw + y][1] = indexWhereTileStarts[selectedTile - 81] + z;
                        output[mp.x / tw + x][mp.y / tw + y] = map[mp.x / tw + x][mp.y / tw + y][0] + "," + map[mp.x / tw + x][mp.y / tw + y][1] + "f";
                        rotations[mp.x / tw + x][mp.y / tw + y][1] = 0;
                        z++;
                    }
                }
            } else {
                if (shiftHeld) {
                    map[mp.x / tw][mp.y / tw][1] = selectedTile;
                    output[mp.x / tw][mp.y / tw] = map[mp.x / tw][mp.y / tw][0] + "," + map[mp.x / tw][mp.y / tw][1] + "f";
                    rotations[mp.x / tw][mp.y / tw][1] = 0;
                } else {
                    map[mp.x / tw][mp.y / tw][0] = selectedTile;
                    output[mp.x / tw][mp.y / tw] = map[mp.x / tw][mp.y / tw][0] + "f";
                    rotations[mp.x / tw][mp.y / tw][0] = 0;
                }
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            map[mp.x / tw][mp.y / tw][0] = groundTile;
            map[mp.x / tw][mp.y / tw][1] = emptyTile;
            output[mp.x / tw][mp.y / tw] = groundTile + "f";
            rotations[mp.x / tw][mp.y / tw][0] = 0;
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            output[mp.x / tw][mp.y / tw] = "INSERT";
        }
    }

    private boolean isNumber(char c) {
        return !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
    }

    private void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    private void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        mp = new Point(MouseInfo.getPointerInfo().getLocation().x - Window.frame.getLocationOnScreen().x + camX - 8, MouseInfo.getPointerInfo().getLocation().y - Window.frame.getLocationOnScreen().y + camY - 31);
        //DRAW THINGS HERE//
        g.translate(-camX, -camY);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(-500, -500, mapX * tw + 1500, mapY * tw + 1300);
        for (int y = 0; y < mapY; y++) {
            for (int x = 0; x < mapX; x++) {
                if (output[x][y].substring(0, 1).equals("x")) {
                    for (int i = 0; i < map[x][y].length; i++) {
                        g.drawImage(flipped(tiles[map[x][y][i]][rotations[x][y][i]]), x * tw, y * tw, null);
                    }
                } else {
                    for (int i = 0; i < map[x][y].length; i++) {
                        g.drawImage(tiles[map[x][y][i]][rotations[x][y][i]], x * tw, y * tw, null);
                    }
                }
                if (output[x][y].substring(output[x][y].length() - 1).equals("w")) {
                    g.setColor(Color.red);
                    g.fillRect(x * tw, y * tw, 6, 6);
                }
                if (output[x][y].substring(output[x][y].length() - 1).equals("p")) {
                    g.setColor(Color.blue);
                    g.fillRect(x * tw, y * tw, 6, 6);
                }
                if (output[x][y].substring(output[x][y].length() - 1).equals("c")) {
                    g.setColor(Color.lightGray);
                    g.fillRect(x * tw, y * tw, 6, 6);
                }
                if (output[x][y].substring(output[x][y].length() - 1).equals("d")) {
                    g.setColor(new Color(150,75,0));
                    g.fillRect(x * tw, y * tw, 6, 6);
                }
                if (output[x][y].substring(output[x][y].length() - 1).equals("b")) {
                    g.setColor(Color.black);
                    g.fillRect(x * tw, y * tw, 6, 6);
                }
                if (output[x][y].substring(output[x][y].length() - 1).equals("i")) {
                    g.setColor(Color.green);
                    g.fillRect(x * tw, y * tw, 6, 6);
                }
                if (output[x][y].substring(output[x][y].length() - 1).equals("y")) {
                    g.setColor(Color.yellow);
                    g.fillRect(x * tw, y * tw, 6, 6);
                }
            }
        }

        g.setColor(Color.YELLOW);
        g.fillRect(9 + camX, 9 + camY, tw+2, tw+2);
        g.drawImage(tiles[selectedTile][0], 10 + camX, 10 + camY, null);
        g.drawString(selectedTile + "", 70 + camX, 10 + camY);

        g.setColor(Color.green);
        g.drawRect(mapX / 2 * tw, mapY / 2 * tw, tw, tw);

        g.setColor(Color.lightGray);
        g.drawRect(mp.x / tw * tw, mp.y / tw * tw, tw, tw);
        g.translate(camX, camY);
        ////////////////////
        g.dispose();
        bs.show();
    }

    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) /ns;
            lastTime = now;
            while(delta >= 1) {
                render();
                frames++;
                delta--;
            }
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frames = 0;
            }
        }
        stop();
    }

    private BufferedImage resizeImage(final BufferedImage image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    private BufferedImage flipped(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    private BufferedImage rotateImage(BufferedImage image, int angle) {
        if (image != null) {
            final double rads = Math.toRadians(angle);
            final double sin = Math.abs(Math.sin(rads));
            final double cos = Math.abs(Math.cos(rads));
            final int w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
            final int h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
            final BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
            final AffineTransform at = new AffineTransform();
            at.translate(w / 2f, h / 2f);
            at.rotate(rads, 0, 0);
            at.translate(-image.getWidth() / 2f, -image.getHeight() / 2f);
            final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            return rotateOp.filter(image, rotatedImage);
        }
        return null;
    }

    private BufferedImage[] cutSpriteSheet(String path, int width, int height, int mult, int spriteWidth, int spriteHeight) {
        BufferedImage sheet = loadImage(path, 1);
        assert sheet != null;
        BufferedImage[] sprites = new BufferedImage[width * height];
        int z = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sprites[z] = resizeImage(sheet.getSubimage(x * spriteWidth, y * spriteHeight, spriteWidth, spriteHeight), spriteWidth * mult, spriteHeight * mult);
                z++;
            }
        }
        return sprites;
    }

    private BufferedImage loadImage(String path, float mult) {
        try {
            BufferedImage img = ImageIO.read(new File(this.path + path + ".png"));
            return resizeImage(img, (int) (img.getWidth() * mult), (int) (img.getHeight() * mult));
        } catch (IOException e) {
            return null;
        }
    }
}
