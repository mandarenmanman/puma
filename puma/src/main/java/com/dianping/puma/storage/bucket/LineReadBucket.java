package com.dianping.puma.storage.bucket;

import com.dianping.puma.common.AbstractLifeCycle;

import java.io.*;

public final class LineReadBucket extends AbstractLifeCycle implements ReadBucket {

	private final String filename;

	private final int bufSizeByte;

	private final int avgSizeByte;

	private BufferedReader reader;

	protected LineReadBucket(String filename, int bufSizeByte, int avgSizeByte) {
		this.filename = filename;
		this.bufSizeByte = bufSizeByte;
		this.avgSizeByte = avgSizeByte;
	}

	@Override
	protected void doStart() {
		try {
			reader = new BufferedReader(new FileReader(filename), bufSizeByte);
			if (!reader.markSupported()) {
				throw new RuntimeException("line read bucket should support mark.");
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException("bucket file not found.");
		}
	}

	@Override
	protected void doStop() {
		try {
			reader.close();
		} catch (IOException ignore) {
		}
	}

	@Override
	public byte[] next() throws IOException {
		checkStop();

		try {
			reader.mark(avgSizeByte);

			String line = reader.readLine();
			if (line == null) {
				throw new EOFException("reach the end of line reader bucket.");
			}

			return line.getBytes();
		} catch (EOFException eof) {
			throw eof;
		} catch (IOException io) {
			try {
				reader.reset();
			} catch (IOException ignore) {
			}

			throw io;
		}
	}

	@Override
	public void skip(long offset) throws IOException {
		checkStop();

		if (offset < 0) {
			throw new IllegalArgumentException("offset is negative.");
		}

		long count = offset;
		while (count > 0) {
			long skipLength = reader.skip(offset);
			count -= skipLength;
		}
	}
}