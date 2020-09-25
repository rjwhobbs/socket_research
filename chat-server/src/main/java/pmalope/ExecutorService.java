package pmalope;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
*  https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
* */
public class ExecutorService {
    public static void main(String[] args) throws IOException {
        System.out.println("Inside: " + Thread.currentThread().getName());
        NetworkService t = new NetworkService(59001, 10);
        t.run();
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
//                    shutdownAndAwaitTermination(pool);
                    pool.execute(new Handler(serverSocket.accept()));
                }
            } catch (IOException ex) {
                pool.shutdown();
            }
        }

//        TODO where or how do I use this method
//        void shutdownAndAwaitTermination(java.util.concurrent.ExecutorService pool) {
//            pool.shutdown(); // Disable new tasks from being submitted
//            System.out.println("Something happening here");
//            try {
//                // Wait a while for existing tasks to terminate
//                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
//                    pool.shutdownNow(); // Cancel currently executing tasks
//                    // Wait a while for tasks to respond to being cancelled
//                    if (!pool.awaitTermination(60, TimeUnit.SECONDS))
//                        System.err.println("Pool did not terminate");
//                }
//            } catch (InterruptedException ie) {
//                // (Re-)Cancel if current thread also interrupted
//                pool.shutdownNow();
//                // Preserve interrupt status
//                Thread.currentThread().interrupt();
//            }
//        }

    }

    private  static class Handler implements Runnable {
        private final Socket socket;

        Handler(Socket socket) { this.socket = socket; }

        public void run() {
            // read and service request on socket
            System.out.println("==== Client Connected ====");
            System.out.println("Name - " + Thread.currentThread().getName());
            System.out.println("ID - " + Thread.currentThread().getId());

            System.out.println("==== Socket Address ====");
            System.out.println("Local Socket - " + this.socket.getLocalSocketAddress());
            System.out.println("Remote Socket - " + this.socket.getRemoteSocketAddress());
        }
    }
}
