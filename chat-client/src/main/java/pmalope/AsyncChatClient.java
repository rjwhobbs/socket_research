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
 * @author  Phetho Malope Malope & Roger Hobbs
 * @since   2020-09-28
 * @version 1.0
 */

/*
 * TODO
 *
 * 1. Add debugger
 * */
public class AsyncChatClient {
    private AsynchronousSocketChannel client;
    private Future<Void> future;
    private static AsyncChatClient instance;
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    private AsyncChatClient() {
        try {
            client = AsynchronousSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
            future = client.connect(hostAddress);
            start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AsyncChatClient getInstance() {
        if (instance == null)
            instance = new AsyncChatClient();
        return instance;
    }

    private void start() {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public String readHandler() throws ExecutionException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        client.read(buffer).get();
        buffer.flip();
        return new String(buffer.array());
    }

    public void writeHandler() throws ExecutionException, InterruptedException {
        String input = getNextLine();
        client.write(ByteBuffer.wrap(input.getBytes())).get();
    }

    String getNextLine() {
        String line;
        try {
            line = reader.readLine();
            if (line == null) {
                return "EXIT";
            }
            return line;
        }
        catch (IOException e) {
            e.printStackTrace();
            return "EXIT";
        }
    }

    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        final AsyncChatClient client = AsyncChatClient.getInstance();
        client.start();
        Executor pool = Executors.newFixedThreadPool(2);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String response = null;
                    try {
                        response = client.readHandler();
                        System.out.println(response);
                    } catch (ExecutionException | InterruptedException e) {
                        System.out.println(e.getMessage());
                        client.stop();
                        System.exit(-1);
                    }
                }
            }
        });
        pool.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        client.writeHandler();
                    } catch (ExecutionException | InterruptedException e) {
                        System.out.println(e.getMessage());
                        client.stop();
                        System.exit(-1);
                    }
                }
            }
        });
    }
}
