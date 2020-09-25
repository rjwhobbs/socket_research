package pmalope;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/*
*
* Ref: https://www.baeldung.com/java-nio-2-async-channels
* */
public class AsyncTestServer {

    public static void main(String[] args) {
        new AsyncTestServer().futureApproach();
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
            System.out.println("Client IP address" + client.getLocalAddress());
            System.out.println("Debug " + future.isDone());

        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

//    TODO
    public void completionHandlerApproach() {

    }
}
