
import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.*;

public class Server {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService pool = Executors.newCachedThreadPool();
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

            ClientHandler handler = new ClientHandler(clientSocket);
            clients.add(handler);
            pool.submit(handler);
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int size = in.readInt();
                    byte[] data = new byte[size];
                    in.readFully(data);

                    // Broadcast to all other clients
                    broadcast(data, this);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                if (clients.isEmpty()) {
                    System.out.println("No clients connected. Closing server.");
                    try { serverSocket.close(); } catch (IOException ignored) {}
                    pool.shutdownNow();
                    System.exit(0);
                }
            } finally {
                clients.remove(this);
                try { socket.close(); } catch (IOException ignored) {}
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

    public static void main(String[] args) throws IOException {
        Server server = new Server(5000);
        server.start();
    }
}
