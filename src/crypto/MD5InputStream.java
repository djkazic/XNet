package crypto;

import java.io.*;

import main.Utils;

public class MD5InputStream extends FilterInputStream {
	/**
	 * MD5 context
	 */
	private MD5	md5;

	/**
	 * Creates a MD5InputStream
	 * @param in	The input stream
	 */
	public MD5InputStream (InputStream in) {
		super(in);

		md5 = new MD5();
	}

	/**
	 * Read a byte of data. 
	 * @see java.io.FilterInputStream
	 */
	public int read() throws IOException {
		int c = in.read();

		if (c == -1)
			return -1;

		if ((c & ~0xff) != 0) {
			Utils.print(this, "MD5InputStream.read() got character with (c & ~0xff) != 0)!");
		} else {
			md5.Update(c);
		}
		return c;
	}

	public int read (byte bytes[], int offset, int length) throws IOException {
		int	r;
		if ((r = in.read(bytes, offset, length)) == -1)
			return r;
		md5.Update(bytes, offset, r);
		return r;
	}

	public byte[] hash () {
		return md5.Final();
	}

	public MD5 getMD5() {
		return md5;
	}
}

