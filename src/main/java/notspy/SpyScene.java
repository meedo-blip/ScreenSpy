package notspy;

import components.StaticBlock;
import components.TextNode;
import jade.Scene;
import jade.Transform;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import util.Utils;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static jade.Constants.ARIAL_FONT;
import static jade.Constants.DEFAULT_SH;

public class SpyScene extends Scene {

    private RecieverServer server;

    private static class Client {
        public StaticBlock block;
        public TextNode infoText = null;
        public int clientId;
    }

    private static final List<Client> clients = new ArrayList<>();

    private static final int texWidth = 1280;  // scaled resolution
    private static final int texHeight = texWidth * 9 / 16;

    private static final int gridResWidth = 1280;
    private static final int gridResHeight = gridResWidth * 9 / 16;

    // sprite width
    private static int blockWidth = 1920;
    private static int blockHeight = blockWidth * 9 / 16;

    private final ByteBuffer buffer = BufferUtils.createByteBuffer(texWidth * texHeight * 3);

    static class FrameData {
        public int clientId;
        public byte[] data;
    }

    // Thread-safe queue for raw bytes from network
    private static final ConcurrentLinkedQueue<FrameData> receivedQueue = new ConcurrentLinkedQueue<>();

    // Decompress RLE RGBA array
    public static byte[] decompress(byte[] compressed) {
        List<Byte> out = new ArrayList<>();
        int i = 0;
        while (i < compressed.length) {
            int count = Byte.toUnsignedInt(compressed[i++]);
            byte r = compressed[i++];
            byte g = compressed[i++];
            byte b = compressed[i++];
            byte a = compressed[i++];

            for (int j = 0; j < count; j++) {
                out.add(r);
                out.add(g);
                out.add(b);
                out.add(a);
            }
        }

        byte[] result = new byte[out.size()];
        for (int j = 0; j < out.size(); j++) result[j] = out.get(j);
        return result;
    }

    public static byte[] decompress_RGB(byte[] compressed) {
        List<Byte> out = new ArrayList<>();
        int i = 0;
        while (i < compressed.length) {
            int count = Byte.toUnsignedInt(compressed[i++]);
            byte r = compressed[i++];
            byte g = compressed[i++];
            byte b = compressed[i++];

            for (int j = 0; j < count; j++) {
                out.add(r);
                out.add(g);
                out.add(b);
            }
        }

        byte[] result = new byte[out.size()];
        for (int j = 0; j < out.size(); j++) result[j] = out.get(j);
        return result;
    }

    public Client getClient(int clientId) {
        if(clients.isEmpty()) return null;
        Client c;

        for (int i = Math.min(clientId, clients.size() - 1); i >= 0; i--) {
            c = clients.get(i);
            if(c.clientId == clientId) {
                return c;
            }
        }

        return null;
    }

    public void updateClients() {
        int gridSize = (int) Math.ceil(Math.sqrt(clients.size()));
        if (gridSize < 1) return;
        blockWidth = gridResWidth / gridSize;
        blockHeight = gridResHeight / gridSize;

        System.out.println("Grid size: " + gridSize + " block size: " + blockWidth + "x" + blockHeight);

        for (int i = 0; i < clients.size(); i++) {
            int row = i / gridSize;
            int col = i % gridSize;

            System.out.println("Row: " + row + " Col: " + col);

            float x = (col * blockWidth) - (gridResWidth / 2f) + (blockWidth / 2f);
            float y = (row * blockHeight) - (gridResHeight / 2f) + (blockHeight / 2f);

            Client c = clients.get(i);
            c.block.x(x).y(y).width(blockWidth).height(blockHeight);

            c.infoText.setText("");
            if (c.infoText != null) {
                c.infoText.x(x).y( y - ((float) blockHeight / 2) + 100);
            }

            System.out.println("Client " + c.clientId + " at " + x + ", " + y);
        }
    }


    public void makeClient(int clientId) {
        // Check if client already exists
        Client c = getClient(clientId);

        System.out.println("Making client " + clientId);
        if(c != null) return;

        c = new Client();
        c.clientId = clientId;

        // Initialize block
        c.block = (StaticBlock) new StaticBlock(DEFAULT_SH, new org.joml.Vector4f(1,1,1,1))
                .setTransform(new Transform(new Vector2f(0,0), -1, new Vector2f(blockWidth, blockHeight)));
        addSprite(c.block);

        c.infoText = makeText(ARIAL_FONT, server.getRemoteAddress(clientId), 0, 0, 11, new Vector4f(0.5f,0.5f,1,1));
        clients.add(c);

        updateClients();
    }

    public void removeClient(int clientId) {
        Client c = null;
        for (int i = clientId; i >= 0; i--) {
            if((c = clients.get(i)).clientId == clientId) {
                clients.remove(c);
                break;
            }
        }
        removeSprite(c.block);
        if (c.infoText != null)
            removeSprite(c.infoText);
        updateClients();

    }

    @Override
    public void init() {

        try {
            server = new RecieverServer(5000, receivedQueue);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //makeClient(-102020);
    }

    @Override
    public void dispose() {
        server.thread.interrupt();
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        // Consume queued data safely on render thread
        FrameData data;
        int i = 0;
        while ((data = receivedQueue.poll()) != null && data.data != null) {
            //if (i == clients.size()) break;
            Client c = getClient(data.clientId);

            if (c == null) {
                System.out.println("Client " + clients.getLast().clientId + " not found");
                continue;
            }

            data.data = decompress_RGB(data.data);

            //System.out.println("Received frame from " + data.clientId + " len: " + data.data.length);
            if (data.data.length != texWidth * texHeight * 3)  continue;

            buffer.clear();
            buffer.put(data.data);
            buffer.flip();

            //System.out.println("data from " + data.clientId + ": " + Arrays.toString(Arrays.copyOf(data.data, 16)) + " len: " + data.data.length);

            //c.infoText.setText("");
            if (c.block.texId == -1) {
                // Create texture once
                c.block.texId = Utils.generateTexture(buffer, texWidth, texHeight, 3);
            } else {
                // Update existing texture
                Utils.updateTexture(c.block.texId, buffer, texWidth, texHeight, 3);
            }

            i++;
        }
    }
}
