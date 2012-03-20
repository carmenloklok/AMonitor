package com.rich;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.mms.ContentType;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduComposer;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.SendReq;

/**
 * @author carmenloklok
 * @version created£º2012-2-18
 */
public class MMSInfo {
	private Context con;
	private PduBody pduBody;
	private String recieverNum;
	private int partCount = 1;
	private String subject;

	public MMSInfo(Context con, String subject, String recieverNum) {
		this.con = con;
		this.subject = subject;
		this.recieverNum = recieverNum;
		pduBody = new PduBody();
	}

	/**
	 * 
	 * add picture attachment,each adding invoke once
	 * 
	 * @author
	 * @param uriStr
	 *            case:file://mnt/sdcard//1.jpg
	 */
	public void addPart(String uriStr) {
		PduPart part = new PduPart();
		part.setCharset(CharacterSets.UTF_8);
		part.setName(("attachment" + partCount++).getBytes());
		if (uriStr.endsWith(".jpg"))
			part.setContentType(ContentType.IMAGE_JPG.getBytes());
		else if (uriStr.endsWith(".3gp"))
			part.setContentType(ContentType.VIDEO_3GPP.getBytes());
		if(uriStr.endsWith(".txt"))
			part.setContentType(ContentType.TEXT_PLAIN.getBytes());
		part.setDataUri(Uri.parse(uriStr));
		pduBody.addPart(part);
	}

	/**
	 * turn mms's content and subject etc into byte array,repreare sending to
	 * "http://mmsc.monternet.com" via http
	 * 
	 * @author µË
	 * @return
	 */
	public byte[] getMMSBytes() {
		PduComposer composer = new PduComposer(con, initSendReq());
		byte[] bb = new byte[0];
		try {
			bb = composer.make();
		} catch (Exception e) {
			Log.e("save", e.getMessage());
		}
		Log.e("save", "got it"+bb.length);
		return bb;
	}

	/**
	 * init SendReq
	 * 
	 * @author
	 * @return
	 */
	private SendReq initSendReq() {
		SendReq req = new SendReq();
		EncodedStringValue[] sub = EncodedStringValue.extract(subject);
		if (sub != null && sub.length > 0) {
			req.setSubject(sub[0]);// set subject
		}
		EncodedStringValue[] rec = EncodedStringValue.extract(recieverNum);
		if (rec != null && rec.length > 0) {
			req.addTo(rec[0]);// set recipient
		}
		req.setBody(pduBody);
		return req;
	}

}