package notspy;

import jade.Window;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//public class RecieverClient {
//    private Socket socket;
//    private DataOutputStream out;
//    private DataInputStream in;
//    private final ConcurrentLinkedQueue<byte[]> receivedQueue;
//
//    public RecieverClient(String host, int port, ConcurrentLinkedQueue<byte[]> queue) throws IOException {
//        this.receivedQueue = queue;
//        socket = new Socket(host, port);
//        out = new DataOutputStream(socket.getOutputStream());
//        in = new DataInputStream(socket.getInputStream());
//
//        new Thread(() -> {
//            try {
//                while (true) {
//                    int size = in.readInt();
//                    byte[] data = new byte[size];
//                    in.readFully(data);
//                    receivedQueue.add(data); // enqueue only
//                }
//            } catch (IOException e) {
//                System.out.println("Disconnected from server: " + e.getMessage());
//            }
//        }, "RecieveClient-NetworkThread").start();
//    }
//
//    public void send(byte[] data) throws IOException {
//        out.writeInt(data.length);
//        out.write(data);
//        out.flush();
//    }
//
//    public void close() throws IOException {
//        socket.close();
//    }
//}

public class RecieverServer {
    public Thread thread;
    private int port;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService pool = Executors.newCachedThreadPool();
    public final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    private final ConcurrentLinkedQueue<SpyScene.FrameData> receivedQueue;
    private ServerSocket serverSocket;

    public RecieverServer(int port, ConcurrentLinkedQueue<SpyScene.FrameData> queue) throws IOException {
        this.port = port;
        this.receivedQueue = queue;
    }

    public void start() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocket = serverSocketChannel.socket();
        System.out.println("Server started on port " + port);

        thread = new Thread(() -> {

            while (!Window.shouldClose()) {
                try {
                    Socket clientSocket = serverSocketChannel.accept().socket();
                    System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());
                    ClientHandler handler = new ClientHandler(clientSocket);

                    Window.getScene().queueEvent(() -> Objects.requireNonNull(Window.getScene(SpyScene.class)).makeClient(handler.id));

                    clients.add(handler);
                    pool.submit(handler);
                } catch (IOException e) {
                    if (!serverSocketChannel.isOpen()) {
                        break; // Channel was closed, exit the loop
                    }
                    System.out.println("Error accepting client connection: " + e.getMessage());
                }
            }

            System.out.println("Server stopped");
            close();
        });

        thread.start();
    }

    public boolean isClientConnected (int clientId) {
        for(ClientHandler client: clients)
            if (client.id == clientId) return true;
        return false;
    }

    public boolean isRunning () {
        return !serverSocket.isClosed();
    }

    public void close() {
        try {
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            pool.shutdownNow();
        } catch (IOException e) {
            System.out.println("Error closing server: " + e.getMessage());
        }
    }

    public static int getPort(Socket socket) {
        return socket.getPort();
    }

    public String getRemoteAddress(int clientId) {
        for (ClientHandler client : clients) {
            if (client.id == clientId) {
                return client.socket.getRemoteSocketAddress().toString();
            }
        }
        return "Unknown";
    }

    private class ClientHandler implements Runnable {
        private static int uuid = 0;
        private final Socket socket;
        public final int id;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.id = uuid++;
        }

        @Override
        public void run() {
            try {
                while (!serverSocket.isClosed()) {
                    int size = in.readInt();
                    byte[] data = new byte[size];
                    in.readFully(data);

                    SpyScene.FrameData frameData = new SpyScene.FrameData();
                    frameData.data = data;
                    frameData.clientId = id;

                    //System.out.println("Received frame from client " + id + ": " + frameData.data.length + " bytes");
                    receivedQueue.add(frameData); // enqueue only
                    // Broadcast to all other clients
                    //broadcast(data, this);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());

                Window.getScene().queueEvent(() -> {
                    ((SpyScene) Window.getScene()).removeClient(id);
                });

            } finally {
                clients.remove(this);
                try { socket.close(); } catch (IOException ignored) {}
                System.out.println("Connection with client " + id + " closed.");
            }
        }

        public void send(byte[] data) {
            try {
                out.writeInt(data.length);
                out.write(data);
                out.flush();
            } catch (IOException e) {
                System.out.println("Failed to send to client " + socket.getRemoteSocketAddress());
            }
        }
    }


    private void broadcast(byte[] data, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) { // don't send back to sender
                client.send(data);
            }
        }
    }
}
