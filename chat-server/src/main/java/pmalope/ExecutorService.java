package pmalope;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

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
//                    pool.execute(new Handler(serverSocket.accept()));
                    var t = pool.submit(new Handler(serverSocket.accept()) {

                    });
                    System.out.println(t.get());
                }
            } catch (IOException ex) {
                pool.shutdown();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private  static class Handler implements Runnable {
        private final Socket socket;

        Handler(Socket socket) { this.socket = socket; }

        public void run() {
            // read and service request on socket
            System.out.println("==== Client Connected ====");
            System.out.println("Name - " + Thread.currentThread().getName());
            System.out.println("ID - " + Thread.currentThread().getId());
        }
    }
}
