package notspy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendApp {

    public static byte[] compress(byte[] rgba) {
        if (rgba.length % 4 != 0) {
            throw new IllegalArgumentException("Input length must be multiple of 4 (RGBA pixels)");
        }

        List<Byte> out = new ArrayList<>();
        int numPixels = rgba.length / 4;

        int i = 0;
        while (i < numPixels) {
            // Get current color
            byte r = rgba[i * 4];
            byte g = rgba[i * 4 + 1];
            byte b = rgba[i * 4 + 2];
            byte a = rgba[i * 4 + 3];

            // Count repeats (max 255)
            int count = 1;
            while (i + count < numPixels && count < 255) {
                int idx = (i + count) * 4;
                if (rgba[idx] == r && rgba[idx + 1] == g &&
                        rgba[idx + 2] == b && rgba[idx + 3] == a) {
                    count++;
                } else break;
            }

            // Write [count][r][g][b][a]
            out.add((byte) count);
            out.add(r);
            out.add(g);
            out.add(b);
            out.add(a);

            i += count;
        }

        // Convert to byte array
        byte[] result = new byte[out.size()];
        for (int j = 0; j < out.size(); j++) result[j] = out.get(j);
        return result;
    }

    public static byte[] compress_RGB(byte[] rgb) {
        if (rgb.length % 3 != 0) {
            throw new IllegalArgumentException("Input length must be multiple of 4 (RGBA pixels)");
        }

        List<Byte> out = new ArrayList<>();
        int numPixels = rgb.length / 3;

        int i = 0;
        while (i < numPixels) {
            // Get current color
            byte r = rgb[i * 3];
            byte g = rgb[i * 3 + 1];
            byte b = rgb[i * 3 + 2];

            // Count repeats (max 255)
            int count = 1;
            while (i + count < numPixels && count < 255) {
                int idx = (i + count) * 3;
                if (rgb[idx] == r && rgb[idx + 1] == g &&
                        rgb[idx + 2] == b) {
                    count++;
                } else break;
            }

            // Write [count][r][g][b][a]
            out.add((byte) count);
            out.add(r);
            out.add(g);
            out.add(b);

            i += count;
        }
        // Convert to byte array
        byte[] result = new byte[out.size()];
        for (int j = 0; j < out.size(); j++) result[j] = out.get(j);
        return result;
    }

    public static byte[] intsToBytes(int[] ints, byte[] bytes) {

        for (int i = 0; i < ints.length; i++) {
            bytes[i * 4]     = (byte) ((ints[i] >> 16) & 0xFF); // R
            bytes[i * 4 + 1] = (byte) ((ints[i] >> 8) & 0xFF);  // G
            bytes[i * 4 + 2] = (byte) (ints[i] & 0xFF);         // B
            bytes[i * 4 + 3] = (byte) ((ints[i] >> 24) & 0xFF); // A
        }
        return bytes;
    }

    public static byte[] intsToBytes_RGB(int[] ints, byte[] bytes) {

        for (int i = 0; i < ints.length; i++) {
            bytes[i * 3]     = (byte) ((ints[i] >> 16) & 0xFF); // R
            bytes[i * 3 + 1] = (byte) ((ints[i] >> 8) & 0xFF);  // G
            bytes[i * 3 + 2] = (byte) (ints[i] & 0xFF);         // B
        }
        return bytes;
    }
    public static void main(String[] args) {
        try {
            SendClient client = new SendClient("192.168.8.190", 5000);

            Robot robot = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRect = new Rectangle(screenSize);

            int targetWidth = 1280;  // downscale to save bandwidth
            int targetHeight = 720;

            while (true) {
                // Capture desktop
                BufferedImage screenshot = robot.createScreenCapture(screenRect);

                // Resize if needed
                Image scaled = screenshot.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.drawImage(scaled, 0, 0, null);
                g2d.dispose();

                int[] ints = new int[targetWidth * targetHeight];
                byte[] frameBytes = new byte[targetWidth * targetHeight * 3];
                resized.getRGB(0,0,targetWidth,targetHeight, ints, 0, targetWidth);

                intsToBytes_RGB(ints, frameBytes);
                frameBytes = compress_RGB(frameBytes);

                // Send to server
                client.sendFrame(frameBytes);

                // ~30 FPS
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SendClient {
    private Socket socket;
    private DataOutputStream out;

    private int maxFps = 44; // throttle sending (avoid flooding)

    public SendClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        System.out.println("Connected to server as sender");
    }

    public void sendFrame(byte[] frameData) throws IOException {
        // Safety check: limit frame rate
        long now = System.currentTimeMillis();
        if (now - lastSendTime < 1000 / maxFps) {
            return; // skip frame
        }
        lastSendTime = now;

        // Write packet
        out.writeInt(frameData.length);
        out.write(frameData);
        out.flush();
    }

    private long lastSendTime = 0;

    public void close() throws IOException {
        socket.close();
    }
}


