package pmalope;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AsyncEchoServer {
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;
    private static final int PORT = 5000;
    public AsyncEchoServer() {
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", PORT);
            serverChannel.bind(hostAddress);
            while (true) {
                /*
                * This method initiates an asynchronous operation to accept a connection made to this channel's socket.
                * The handler parameter is a completion handler that is invoked when a connection is accepted (or the operation fails).
                 * When a connection is established, the completed callback method in the CompletionHandler of the accept operation is called.
                 *  */
                serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        System.out.println("Client connected");
                        System.out.println(":::DEBUG::: Accept call back initiated");
                        System.out.println(":::DEBUG::: Results -> " + result);
                        if (serverChannel.isOpen()) {
                            /*
                             * we call the accept API again to get ready for another incoming connection while reusing the same handler.
                             * */
                            serverChannel.accept(null, this);
                        }

                        clientChannel = result;
                        if ((clientChannel != null) && (clientChannel.isOpen())) {
                            ReadWriteHandler handler = new ReadWriteHandler();
                            ByteBuffer buffer = ByteBuffer.allocate(32);

                            Map<String, Object> readInfo = new HashMap<>();
                            readInfo.put("action", "read");
                            readInfo.put("buffer", buffer);

//                            handler.clientChannel.read(buffer, readInfo, handler);
                            clientChannel.read(buffer, readInfo, handler);
                        }
                        System.out.println(":::DEBUG::: completed method exiting");
                        System.out.println();
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        System.out.println("There was an error in the completion handler.");
                        System.out.println(exc.getMessage());
                    }
                });
                System.out.println("Waiting for connection on port " + PORT);
                System.out.println(":::DEBUG::: Outside the accept callback");
                try {
                    // This method of "pausing" the while loop only works
                    // if nothing is inserted on stdin on this process,
                    // not ideal.
                    System.in.read();
                } catch (IOException e) {
                    System.out.println("Inner catch");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Outer catch");
            e.printStackTrace();
        }
    }

    class ReadWriteHandler implements CompletionHandler<Integer, Map<String, Object>> {
        // It is import to remember that the calls to client.read() and write()
        // are async and that this handler is only the callback to those reads
        // and writes, at a glance it might seem like an implementation which
        // of course it isn't.
        @Override
        public void completed(Integer result, Map<String, Object> attachment) {
            System.out.println("ReadWriteHandler handler started");
            Map<String, Object> actionInfo = attachment;
            String action = (String) actionInfo.get("action");
            if ("read".equals(action)) {
                ByteBuffer buffer = (ByteBuffer) actionInfo.get("buffer");
                // Result from read operation
//                if (result == -1) {
//                    try {
//                        System.out.println("Closing client.");
//                        clientChannel.close();
//                    } catch (IOException e) {
//                        System.out.println("Error in client close().");
//                        e.printStackTrace();
//                    } finally {
//                        return;
//                    }
//                }
                String fromBuffer = new String(buffer.array());
                String message = "echo: ";
                String stringToWrite = message + fromBuffer;
                System.out.println(":::DEBUG::: Data to be written to stream -> " + stringToWrite);
                System.out.println(":::DEBUG::: Data to be written to stream converted to ByteBuffer" + Arrays.toString(ByteBuffer.wrap(stringToWrite.getBytes()).array()));
                buffer.flip();
                actionInfo.put("action", "write");
//                System.out.println(":::DEBUG::: Buffer Before writing to stream -> " + Arrays.toString(buffer.array()));
//                System.out.println(":::DEBUG::: User input in bytes converted to array -> " + Arrays.toString(stringToWrite.getBytes()));
//                System.out.println(":::DEBUG::: User input in bytes converted to ByteBufferWrapper -> " + Arrays.toString(ByteBuffer.wrap(stringToWrite.getBytes()).array()));
                clientChannel.write(ByteBuffer.wrap(stringToWrite.getBytes()), actionInfo, this); // FIXME this seems to be writting incomplete buffer
//                clientChannel.write(buffer, actionInfo, this);
                buffer.clear();
                System.out.println(":::DEBUG::: Read from stream is done.");
            } else if ("write".equals(action)) {
                ByteBuffer buffer = ByteBuffer.allocate(32);
                actionInfo.put("action", "read");
                actionInfo.put("buffer", buffer);
                clientChannel.read(buffer, actionInfo, this);
                System.out.println(":::DEBUG::: Writing to stream is done");
            } else {
                System.out.println("***************End of the RW Handler*************************");
            }

            if (clientChannel.isOpen()) {
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<Client socket is open >>>>>>>>>>>>>>>>>>>>>>");
            } else {
                System.out.println("<<<<<<!!!!!!!<<<<<<<<<<<<<<Client socket is Closed >>>>>>>>!!!!!!>>>>>>>>>");
            }
        }

        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {
            System.out.println("ReadWriteHandler failed() method call:");
            System.out.println(exc.getMessage());
        }

    }

    public static void main(String[] args) {
        new AsyncEchoServer();
    }
}
