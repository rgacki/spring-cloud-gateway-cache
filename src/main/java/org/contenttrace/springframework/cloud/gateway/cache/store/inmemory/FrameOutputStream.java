package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class FrameOutputStream extends OutputStream {

  private static final int MAXIMUM_FRAMES = Integer.getInteger(FrameOutputStream.class.getName() + ".maximumFrames", 100_000);
  private static final int DEFAULT_FRAME_SIZE = Integer.getInteger(FrameOutputStream.class.getName() + ".frameSize", 100 * 1000);

  private final List<byte[]> frames;
  private final int frameSize;
  private final long maximumSize;

  private byte[] currentChunk;
  private int positionInChunk;

  private long written;
  private boolean closed;

  public FrameOutputStream(final long estimatedSize) {
    this(DEFAULT_FRAME_SIZE, estimatedSize, -1L);
  }

  public FrameOutputStream(final int frameSize,
                           final long estimatedSize) {
    this(frameSize, estimatedSize, -1L);
  }

  FrameOutputStream(final int frameSize,
                    final long estimatedSize,
                    final long maximumSize) {
    if (frameSize > 0) {
      throw new IllegalArgumentException("'frameSize' must be larger 0!");
    }
    this.frameSize = frameSize;
    this.maximumSize = maximumSize;
    this.written = 0L;
    this.frames = estimatedSize > 0L && estimatedSize < MAXIMUM_FRAMES
      ? new ArrayList<>(((int) Math.floor(estimatedSize / frameSize)) + 1)
      : new LinkedList<>();
    this.closed = false;
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      throw new IOException("Stream is already closed!");
    }
    if (currentChunk != null) {
      if (currentChunk.length == positionInChunk) {
        frames.add(currentChunk);
      } else {
        frames.add(Arrays.copyOf(currentChunk, positionInChunk));
      }
    }
    this.closed = true;
  }

  private void ensureChunk() {
    if (currentChunk == null || currentChunk.length == positionInChunk) {
      if (currentChunk != null) {
        frames.add(currentChunk);
      }
      currentChunk = new byte[frameSize];
      positionInChunk = 0;
    }
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if ((off < 0) || (off > b.length) || (len < 0) ||
      ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }

    if (maximumSize > 0 && (written + len) > maximumSize) {
      throw new SizeLimitExceededException(maximumSize, "Size limit exceeded!");
    }

    ensureChunk();

    int unread = len;
    int bytesLeftInChunk = currentChunk.length - positionInChunk;
    int bytesToNextCheck = unread > bytesLeftInChunk ? bytesLeftInChunk : -1;

    for (int i = 0; i < len; i++) {
      if (bytesToNextCheck == 0) {
        ensureChunk();
        bytesLeftInChunk = currentChunk.length - positionInChunk;
        bytesToNextCheck = unread > bytesLeftInChunk ? bytesLeftInChunk : -1;
      }
      write(b[off + i]);
      if (bytesToNextCheck > -1) {
        bytesToNextCheck--;
        unread--;
      }
    }
  }

  @Override
  public void write(final int b) throws IOException {
    if (closed) {
      throw new IOException("Stream is closed!");
    }
    if (maximumSize > 0 && written > maximumSize) {
      throw new SizeLimitExceededException(maximumSize, "Size limit exceeded!");
    }
    ensureChunk();
    currentChunk[positionInChunk++] = ((byte) b);
    written++;
  }

  public InputStream openInputStream() throws IOException {
    return new FrameInputStream(getFrames());
  }

  List<byte[]> getFrames() throws IllegalStateException {
    if (!closed) {
      throw new IllegalStateException("Stream is not closed!");
    }
    return Collections.unmodifiableList(frames);
  }

  public long size() {
    return written;
  }

}
