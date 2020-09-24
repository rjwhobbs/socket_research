package pmalope;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class ExecutorService {
    public static void main(String[] args) throws IOException {
        System.out.println("Inside: " + Thread.currentThread().getName());
        new NetworkService(59001, 10).run();
    }

    private static class NetworkService implements Runnable {
        private final ServerSocket serverSocket;
        private final java.util.concurrent.ExecutorService pool;

        public NetworkService(int port, int poolSize) throws IOException {
            serverSocket = new ServerSocket(port);
            pool = Executors.newFixedThreadPool(poolSize);
            System.out.println("Waiting for connection on port " + port);
        }

        public void run() { // run the service
            try {
                for (;;) {
                    pool.execute(new Handler(serverSocket.accept()));
                }
            } catch (IOException ex) {
                pool.shutdown();
            }
        }
    }

    private  static class Handler implements Runnable {
        private final Socket socket;
        Handler(Socket socket) { this.socket = socket; }
        public void run() {
            // read and service request on socket
            System.out.println("Connected");
        }
    }
}
