/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.contenttrace.springframework.cloud.gateway.cache.store.inmemory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * An input stream that reads data from a collection of frames. The stream will reliable calculate the
 * available bytes ({@link #available()}, because the content is fixed and does not change.
 */
class FrameInputStream extends InputStream {

	private static int estimateAvailable(final List<byte[]> chunks) {
		int available = 0;
		for (final byte[] chunk : chunks) {
			int diff = available + chunk.length;
			if (diff < 0) {
				return Integer.MAX_VALUE;
			}
			available = available + chunk.length;
		}
		return available;
	}

	private final Iterator<byte[]> frames;

	private byte[] currentFrame;
	private int positionInFrame;
	private int available;
	private boolean finished;

	FrameInputStream(final List<byte[]> frames) {
		requireNonNull(frames, "'frames' must not be null!");
		this.frames = frames.iterator();
		this.available = estimateAvailable(frames);
		this.finished = this.available <= 0;
	}

	private boolean ensureFrame() {
		if (finished) {
			return false;
		}
		if (currentFrame == null || currentFrame.length == positionInFrame) {
			if (!frames.hasNext()) {
				finished = true;
				return false;
			}
			currentFrame = frames.next();
			positionInFrame = 0;
		}
		return true;
	}

	@Override
	public int available() throws IOException {
		return available;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		if (!ensureFrame()) {
			return -1;
		}
		int i = 0;
		for (; i < len; i++) {
			if (!ensureFrame()) {
				break;
			}
			b[off + i] = currentFrame[positionInFrame + i];
			positionInFrame++;
			available--;
		}
		return i;
	}

	@Override
	public int read() throws IOException {
		if (!ensureFrame()) {
			return -1;
		}
		final int b = currentFrame[positionInFrame++];
		available--;
		return b;
	}
}
