package pmalope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * NIO Socket Chat Client
 *
 * Reference:
 *
 * @author  Phetho Malope Malope
 * @since   2020-09-28
 * @version 1.0
 */

/*
 * TODO
 *
 * 1. Add debugger
 * */
public class AsyncChatClient {
    private static AsynchronousSocketChannel clientChannel;
    private static final int PORT = 5000;
    private static final String HOST = "localhost";

    public static void main(String[] args) throws IOException {
        AsyncChatClient chatClient = new AsyncChatClient();
        chatClient.start();
    }

    private void start() {
        Scanner scanner = new Scanner(System.in);
        /*
         * An asynchronous channel for stream-oriented connecting sockets.
         * */
        try {
            clientChannel = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress(HOST, PORT);
            /*
             * Establish connection
             * */
            Future<Void> results = clientChannel.connect(hostAddress);
            /*
             * Await connection
             * */
            results.get();

            System.out.println(":::DEBUG::: connected to server - " + clientChannel.getRemoteAddress());
            System.out.println("Are you ready to start a conversation? (y)es or (n)o");
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(":::ERROR::: invalid input, please choose (y)es or (n)o");
                }
                else if (input.toLowerCase().equals("no") || input.toLowerCase().equals("n")) {
                    System.out.println("Thank you for trying");
                    System.exit(-1);
                } else if (input.toLowerCase().equals("yes") || input.toLowerCase().equals("y")) {
                    readWriteHandler();
//                    String response = sendMessage(input);
//                    System.out.println(":::DEBUG::: Response from server");
//                    System.out.println(response);
                } else {
                    System.out.println(":::ERROR::: invalid input, please choose (y)es or (n)o");
                }
//                System.out.println(input);
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
//            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void readWriteHandler() throws ExecutionException, InterruptedException {
//        byte[] byteMsg = message.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
//        System.out.println(":::DEBUG::: read handler -> " + Arrays.toString(buffer.array()));
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
//        String echo = new String(buffer.array()).trim();
        System.out.println(new String(buffer.array()));
        buffer.clear();
//        return echo;
    }

    public String sendMessage(String message) throws ExecutionException, InterruptedException {
        byte[] byteMsg = message.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        Future<Integer> writeResult = clientChannel.write(buffer);

        // do some computation

        writeResult.get();
        buffer.flip();
        Future<Integer> readResult = clientChannel.read(buffer);

        // do some computation

        readResult.get();
        String echo = new String(buffer.array()).trim();
        buffer.clear();
        return echo;
    }
}
