package pmalope;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.channels.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/*
*
* Ref: https://www.baeldung.com/java-nio-2-async-channels
* */
public class AsyncTestServer {

    public static void main(String[] args) {
        new AsyncTestServer().completionHandlerApproach();
    }

//    TODO
    public void futureApproach() {
        System.out.println("Testing out Future Approach...");
        try {
            AsynchronousServerSocketChannel server
                    = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("localhost", 5000));
            System.out.println("Waiting for connection...");
            Future<AsynchronousSocketChannel> future = server.accept();

            AsynchronousSocketChannel client = future.get();
            System.out.println("Connection Accepted");
            System.out.println("Client IP address - " + client.getLocalAddress());
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

//    TODO
    public void completionHandlerApproach() {
        System.out.println("CompletionHandler Approach...");
        try {
            AsynchronousServerSocketChannel server
                    = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("localhost", 5000));

            server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel result, Object attachment) {
                    System.out.println("connected");
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    System.out.println("connection failed");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
