package com.rich.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.os.Environment;
import android.util.Log;

public class RichUtils {
	public static boolean runRootCommand(String command) {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "
					+ e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				// nothing
			}
		}
		return true;
	}

	public static int splitBySize(File f, double size, String where) {
		int count = (int) Math.ceil(f.length() / size);
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			OutputStream os;
			byte[] b;
			for (int i = 0; i < count; i++) {
				b = new byte[(int) size];
				raf.seek((long) (i * size));
				int s = raf.read(b);
				String n = f.getName();
				String suffix = n.substring(n.lastIndexOf('.'));
				File ff = new File(Environment.getExternalStorageDirectory(),
						where + "/" + n.substring(0, n.lastIndexOf('.')) + i
								+ suffix);
				os = new FileOutputStream(ff);
				os.write(b, 0, s);
				os.flush();
				os.close();
			}
		} catch (Exception e) {
		}
		return count;
	}

	@SuppressWarnings("unused")
	private static void joinFiles(File f, File[] ps) throws IOException {
		for (File p : ps) {
			RandomAccessFile raf = new RandomAccessFile(p, "r");
			byte[] b = new byte[(int) 1024 * 1024];
			int s = 0;
			while ((s = raf.read(b)) != -1) {
				OutputStream os = new FileOutputStream(f, true);
				os.write(b, 0, s);
				os.flush();
				os.close();
			}
		}
	}
}
