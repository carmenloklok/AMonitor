package com.rich.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.content.Context;
import android.util.Log;

public class RichUtils {
	public static boolean runRootCommand(String command) {
		int a=0;
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

	// public static int splitBySize(File f, double size, String where) {
	// int count = (int) Math.ceil(f.length() / size);
	// try {
	// RandomAccessFile raf = new RandomAccessFile(f, "r");
	// OutputStream os;
	// byte[] b;
	// for (int i = 0; i < count; i++) {
	// b = new byte[(int) size];
	// raf.seek((long) (i * size));
	// int s = raf.read(b);
	// String n = f.getName();
	// String suffix = n.substring(n.lastIndexOf('.'));
	// File ff = new File(Environment.getExternalStorageDirectory(),
	// where + "/" + n.substring(0, n.lastIndexOf('.')) + i
	// + suffix);
	// os = new FileOutputStream(ff);
	// os.write(b, 0, s);
	// os.flush();
	// os.close();
	// }
	// } catch (Exception e) {
	// }
	// return count;
	// }

	public static int splitBySize(Context c, File f, double size, String where) {
		String tagFile = "/data/data/com.rich/ffmpeg";
		File ff = new File(tagFile);
		if (!ff.exists()) {
			RichUtils.getassetsfile(c, "ffmpeg", tagFile);
			RichUtils.runCommand("chmod 744 " + tagFile);
		}
		runCommand(tagFile
				+ " -i ~/Movies/a.3gp -ss 00:00:00:00 -fs 870000 -y -vcodec copy -acodec copy ~/Movies/b.3gp");
		runCommand(tagFile
				+ " -i ~/Movies/a.3gp  2>&1 | grep 'Duration' | cut -d ' ' -f 4 | sed s/,//");
		return 0;
	}

	public static boolean runCommand(String command) {
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec(command);
			InputStreamReader isr = new InputStreamReader(
					process.getErrorStream());
			int i = 0;
			String t = "";
			while ((i = isr.read()) != -1) {
				char c = (char) i;
				t += c;
			}
			Log.e("rich", t);
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

	public static int getassetsfile(Context context, String fileName,
			String tagFile) {
		int retVal = 0;
		try {

			File dir = new File(tagFile);
			if (dir.exists()) {
				dir.delete();
			}

			InputStream in = context.getAssets().open(fileName);
			if (in.available() == 0) {
				return retVal;
			}

			FileOutputStream out = new FileOutputStream(tagFile);
			int read;
			byte[] buffer = new byte[4096];
			while ((read = in.read(buffer)) > 0) {
				out.write(buffer, 0, read);
			}
			out.close();
			in.close();

			retVal = 1;
			return retVal;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
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
