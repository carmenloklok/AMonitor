//发送类
package com.rich;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * @author
 * @version created：2012-2-18
 */
public class MMSSender {
	private static final String TAG = "MMSSender";
	// public static String mmscUrl =
	// "http://mms.msg.eng.t-mobile.com/mms/wapenc";
	// public static String mmscUrl = "http://mmsc.monternet.com";
	// public static String mmsProxy = "10.0.0.172";
	// public static String mmsProxy = "216.155.165.050";
	// public static int mmsPort = 80;
	// public static int mmsPort = 8080;

	private static String HDR_VALUE_ACCEPT_LANGUAGE = "";
	private static final String HDR_KEY_ACCEPT = "Accept";
	private static final String HDR_KEY_ACCEPT_LANGUAGE = "Accept-Language";
	private static final String HDR_VALUE_ACCEPT = "*/*, application/vnd.wap.mms-message, application/vnd.wap.sic";

	public static byte[] sendMMS(Context context, byte[] pdu)
			throws IOException {
		// HDR_VALUE_ACCEPT_LANGUAGE = getHttpAcceptLanguage();
//		APNSwitcher apnSwitcher = new APNSwitcher(context.getContentResolver());
//		apnSwitcher.switchAPN(true);
		ConnectivityManager conManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		conManager.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE,
				"mms");
		HDR_VALUE_ACCEPT_LANGUAGE = HTTP.UTF_8;
		SharedPreferences s = context.getSharedPreferences(
				"com.rich_preferences", 0);
		String mmscUrl = s.getString("mmsc_url", "");
		String mmsProxy = s.getString("mms_proxy", "");
		String mmsPort = s.getString("mms_port", "");
		if (mmscUrl == null || mmscUrl.equals("")) {
			return new byte[0];
		}
		HttpClient client = null;
		try {
			// Make sure to use a proxy which supports CONNECT.
			// client = HttpConnector.buileClient(context);

			HttpParams httpParams = new BasicHttpParams();
			if (!mmsProxy.equals("") && !mmsPort.equals("")) {
				HttpHost httpHost = new HttpHost(mmsProxy,
						Integer.parseInt(mmsPort));
				httpParams
						.setParameter(ConnRouteParams.DEFAULT_PROXY, httpHost);
			}
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);

			client = new DefaultHttpClient(httpParams);

			HttpPost post = new HttpPost(mmscUrl);
			// mms PUD START
			ByteArrayEntity entity = new ByteArrayEntity(pdu);
			entity.setContentType("application/vnd.wap.mms-message");
			post.setEntity(entity);
			post.addHeader(HDR_KEY_ACCEPT, HDR_VALUE_ACCEPT);
			post.addHeader(HDR_KEY_ACCEPT_LANGUAGE, HDR_VALUE_ACCEPT_LANGUAGE);
			post.addHeader(
					"user-agent",
					"Mozilla/5.0(Linux;U;Android 2.1-update1;zh-cn;ZTE-C_N600/ZTE-C_N600V1.0.0B02;240*320;CTC/2.0)AppleWebkit/530.17(KHTML,like Gecko) Version/4.0 Mobile Safari/530.17");
			// mms PUD END
			HttpParams params = client.getParams();
			HttpProtocolParams.setContentCharset(params, "UTF-8");

			// PlainSocketFactory localPlainSocketFactory =
			// PlainSocketFactory.getSocketFactory();

			HttpResponse response = client.execute(post);

			StatusLine status = response.getStatusLine();
			context.sendBroadcast(new Intent("com.rich.code").putExtra("code",
					"status code:" + status.getStatusCode()));
			Log.d(TAG, "status " + status.getStatusCode());
			if (status.getStatusCode() != 200) {
				Log.d(TAG, "!200");
				return new byte[0];
			}
			HttpEntity resentity = response.getEntity();
			byte[] body = null;
			if (resentity != null) {
				try {
					if (resentity.getContentLength() > 0) {
						body = new byte[(int) resentity.getContentLength()];
						DataInputStream dis = new DataInputStream(
								resentity.getContent());
						try {
							dis.readFully(body);
						} finally {
							try {
								dis.close();
							} catch (IOException e) {
								Log.e(TAG,
										"Error closing input stream: "
												+ e.getMessage());
							}
						}
					}
				} finally {
					if (entity != null) {
						entity.consumeContent();
					}
				}
			}
			Log.d(TAG, "result:" + new String(body));

			System.out.println("成功！！" + new String(body));

			return body;
		} catch (IllegalStateException e) {
			Log.e(TAG, "", e);
			// handleHttpConnectionException(e, mmscUrl);
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "", e);
			// handleHttpConnectionException(e, mmscUrl);
		} catch (SocketException e) {
			Log.e(TAG, "", e);
			// handleHttpConnectionException(e, mmscUrl);
		} catch (Exception e) {
			Log.e(TAG, "", e);
			// handleHttpConnectionException(e, mmscUrl);
		} finally {
			// apnSwitcher.switchAPN(false);
			if (client != null) {
				// client.;
			}
		}
		return new byte[0];
	}
}
