package research;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

class ClientAttachment {
  ByteBuffer buffer = ByteBuffer.allocate(4096);
  AsynchronousSocketChannel client;

  ClientAttachment(AsynchronousSocketChannel client) {
    this.client = client;
  }
}
