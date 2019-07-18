package org.contenttrace.springframework.cloud.gateway.cache;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.Assert;

import java.nio.charset.Charset;

public abstract class DataBufferTestUtils {

  /**
   * Dump all the bytes in the given data buffer, and returns them as a byte array.
   * <p>Note that this method reads the entire buffer into the heap,  which might
   * consume a lot of memory.
   * @param buffer the data buffer to dump the bytes of
   * @return the bytes in the given data buffer
   */
  public static byte[] dumpBytes(DataBuffer buffer) {
    Assert.notNull(buffer, "'buffer' must not be null");
    byte[] bytes = new byte[buffer.readableByteCount()];
    buffer.read(bytes);
    return bytes;
  }

  /**
   * Dump all the bytes in the given data buffer, and returns them as a string.
   * <p>Note that this method reads the entire buffer into the heap,  which might
   * consume a lot of memory.
   * @param buffer the data buffer to dump the string contents of
   * @param charset the charset of the data
   * @return the string representation of the given data buffer
   */
  public static String dumpString(DataBuffer buffer, Charset charset) {
    Assert.notNull(charset, "'charset' must not be null");
    byte[] bytes = dumpBytes(buffer);
    return new String(bytes, charset);
  }

}
