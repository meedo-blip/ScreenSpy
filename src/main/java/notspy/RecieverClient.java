package notspy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RecieverClient {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private final ConcurrentLinkedQueue<byte[]> receivedQueue;

    public RecieverClient(String host, int port, ConcurrentLinkedQueue<byte[]> queue) throws IOException {
        this.receivedQueue = queue;
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        new Thread(() -> {
            try {
                while (true) {
                    int size = in.readInt();
                    byte[] data = new byte[size];
                    in.readFully(data);
                    receivedQueue.add(data); // enqueue only
                }
            } catch (IOException e) {
                System.out.println("Disconnected from server: " + e.getMessage());
            }
        }, "RecieveClient-NetworkThread").start();
    }

    public void send(byte[] data) throws IOException {
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public void close() throws IOException {
        socket.close();
    }
}
