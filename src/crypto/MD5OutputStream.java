package crypto;

import java.io.*;

public class MD5OutputStream extends FilterOutputStream {
	private MD5	md5;

	public MD5OutputStream (OutputStream out) {
		super(out);
		md5 = new MD5();
	}

	public void write (int b) throws IOException {
		out.write(b);
		md5.Update((byte) b);
	}

	public void write (byte b[], int off, int len) throws IOException {
		out.write(b, off, len);
		md5.Update(b, off, len);
	}

	public byte[] hash () {
		return md5.Final();
	}

	public MD5 getMD5() {
		return md5;
	}
}

