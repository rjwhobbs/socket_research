package pmalope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncEchoClient {
    private static AsynchronousSocketChannel clientChannel;
    private static final int PORT = 5000;
    private static final String HOST = "localhost";

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        try {
            /*
            * An asynchronous channel for stream-oriented connecting sockets.
            * */
            clientChannel = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", PORT);
            /*
            * Establish connection
            * */
            Future<Void> future = clientChannel.connect(hostAddress);
            /*
            * Await connection
            * */
            future.get();
            System.out.println(":::DEBUG::: connected to server - " + clientChannel.getRemoteAddress());
            System.out.println("Enter your message or type 'bye' to quit");
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.toLowerCase().equals("bye")) {
                    System.out.println("Thank you for trying");
                    System.exit(-1);
                } else if (input.isEmpty()) {
                    System.out.println(":::ERROR::: please enter your message or type bye to quit");
                } else {
                    String response = sendMessage(input);
                    System.out.println(response);
                }
//                System.out.println(input);
            }
        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static String sendMessage(String message) throws ExecutionException, InterruptedException {
        byte[] byteMsg = message.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        System.out.println(":::DEBUG::: buffer before write -> " + Arrays.toString(buffer.array()));
        Future<Integer> writeResult = clientChannel.write(buffer);

        // do some computation
//        System.out.println(":::DEBUG::: Do some computation on writeResults");

        writeResult.get();
//        System.out.println(":::DEBUG::: buffer after write -> " + Arrays.toString(buffer.array()));
        buffer.flip();
//        System.out.println(":::DEBUG::: Do some computation on readResults");
        Future<Integer> readResult = clientChannel.read(buffer);

        // do some computation

//        System.out.println(":::DEBUG::: buffer before read -> " + Arrays.toString(buffer.array()));
        readResult.get();
//        System.out.println(":::DEBUG::: buffer after read -> " + Arrays.toString(buffer.array()));
        String echo = new String(buffer.array()).trim();
        buffer.clear();
        return echo;
    }
}
