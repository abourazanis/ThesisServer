package thesis.server.pedstore;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public enum PedPackager {
	instance;
	private final static String pedsFolder = "/home/tas0s/peds";
	private final String fileSeparator = System.getProperty("file.separator");

	public void create(OutputStream out, String pedId) {
		final String dirName = pedId;
		ZipOutputStream zos = new ZipOutputStream(out);
		File pedDir = new File(pedsFolder);

		if (pedDir.exists() && pedDir.canRead()) {

			String[] fileList = pedDir.list(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.equalsIgnoreCase(dirName);
				}
			});

			if (fileList.length > 0) {
				try {
					zip(pedsFolder + fileSeparator + fileList[0], zos);
				} catch (Exception e) {

				}
			}
		}

	}

	public static void zip(String dir2zip, ZipOutputStream zout)
			throws IOException {
		File directory = new File(dir2zip);
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		Closeable res = zout;
		try {
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File kid : directory.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new ZipEntry(name));
					} else {
						zout.putNextEntry(new ZipEntry(name));
						copy(kid, zout);
						zout.closeEntry();
					}
				}
			}
		} finally {
			res.close();
		}
	}

	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	private static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			copy(in, out);
		} finally {
			out.close();
		}
	}

}
