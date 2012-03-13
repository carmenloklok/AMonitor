package com.rich;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class APNSwitcher {
	private static Uri APN_CURRENT_URI = Uri
			.parse("content://telephony/carriers/current");
	private ContentResolver resolver;
	private static String[] PROJECTION = new String[] { "_id", "apn", "type" };

	public APNSwitcher(ContentResolver r) {
		resolver = r;
	}

	private boolean isApnStringDisabled(String paramString) {
		if ((paramString != null)
				&& ((paramString.endsWith("[disabled]"))
						|| (paramString.endsWith("apndroid")) || (paramString
							.startsWith("-"))))
			return true;
		return false;
	}

	private String cleanApnEnable(String paramString) {
		if (paramString != null) {
			paramString = paramString.replace("[disabled]", "").replace(
					"apndroid", "");
			if (paramString.startsWith("-"))
				paramString = paramString.substring(1);
		}
		return paramString;
	}

	public boolean isAPNEnabled() {
		Cursor c = resolver
				.query(APN_CURRENT_URI, PROJECTION, null, null, null);
		if (c == null)
			return false;
		c.moveToFirst();
		String str2 = c.getString(1);
		String str3 = c.getString(2);
		if ((isApnStringDisabled(str2)) && (isApnStringDisabled(str3)))
			return false;
		else
			return true;
	}

	public void switchAPN(boolean paramBoolean) {
		Cursor Cursor = resolver.query(APN_CURRENT_URI, PROJECTION, null, null,
				null);
		if (Cursor == null)
			return;
		ContentValues ContentValues = new ContentValues();
		while (Cursor.moveToNext()) {
			String str2 = Cursor.getString(1);
			String str3 = Cursor.getString(2);
			if (paramBoolean) {
				if ((isApnStringDisabled(str2)) && (isApnStringDisabled(str3))) {
					String str4 = cleanApnEnable(str2);
					ContentValues.put("apn", str4);
					String str5 = cleanApnEnable(str3);
					ContentValues.put("type", str5);
					resolver.update(APN_CURRENT_URI, ContentValues, "_id=?",
							new String[] { Cursor.getInt(0) + "" });
				}
			} else {
				if (!(isApnStringDisabled(str2))
						&& !(isApnStringDisabled(str3))) {
					String str9 = str2 + "[disabled]";
					ContentValues.put("apn", str9);
					String str11 = str3 + "[disabled]";
					ContentValues.put("type", str11);
					resolver.update(APN_CURRENT_URI, ContentValues, "_id=?",
							new String[] { Cursor.getString(0) });
				}
			}
			ContentValues.clear();
		}
		Cursor.close();
		return;
	}
}
