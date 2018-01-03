package jp.ne.sakura.k_tubo.mobileprinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.provider.MediaStore.Images.Media;
import com.saneielec.libs.PrinterSDK;
import jp.hishidama.html.lexer.rule.HtLexer;
import jp.hishidama.html.lexer.token.ListToken;
import jp.hishidama.html.lexer.token.Token;
import jp.hishidama.html.lexer.token.Tag;
import jp.ne.sakura.k_tubo.mobileprinter.R;

public class demo extends Activity {
	static BluetoothAdapter btAdapter = null;
	static BluetoothDevice device = null;
	static BluetoothSocket socket = null;
	static OutputStream output = null;
	static InputStream input = null;
	static Intent serverIntent = null;
	static String address = null;
	static int printer_type = 0;
	// ビット反転テーブル
	static int[] bit_reverse = { 0x00, 0x80, 0x40, 0xc0, 0x20, 0xa0, 0x60,
			0xe0, 0x10, 0x90, 0x50, 0xd0, 0x30, 0xb0, 0x70, 0xf0, 0x08, 0x88,
			0x48, 0xc8, 0x28, 0xa8, 0x68, 0xe8, 0x18, 0x98, 0x58, 0xd8, 0x38,
			0xb8, 0x70, 0xf8, 0x04, 0x84, 0x44, 0xc4, 0x24, 0xa4, 0x64, 0xe4,
			0x14, 0x94, 0x54, 0xd4, 0x34, 0xb4, 0x74, 0xf4, 0x0c, 0x8c, 0x4c,
			0xcc, 0x2c, 0xac, 0x6c, 0xec, 0x1c, 0x9c, 0x5c, 0xdc, 0x3c, 0xbc,
			0x7c, 0xfc, 0x02, 0x82, 0x42, 0xc2, 0x22, 0xa2, 0x62, 0xe2, 0x12,
			0x92, 0x52, 0xd2, 0x32, 0xb2, 0x72, 0xf2, 0x0a, 0x8a, 0x4a, 0xca,
			0x2a, 0xaa, 0x6a, 0xea, 0x1a, 0x9a, 0x5a, 0xda, 0x3a, 0xba, 0x7a,
			0xfa, 0x06, 0x86, 0x46, 0xc6, 0x26, 0xa6, 0x66, 0xe6, 0x16, 0x96,
			0x56, 0xd6, 0x36, 0xb6, 0x76, 0xf6, 0x0e, 0x8e, 0x4e, 0xce, 0x2e,
			0xae, 0x6e, 0xee, 0x1e, 0x9e, 0x5e, 0xde, 0x3e, 0xbe, 0x7e, 0xfe,
			0x01, 0x81, 0x41, 0xc1, 0x21, 0xa1, 0x61, 0xe1, 0x11, 0x91, 0x51,
			0xd1, 0x31, 0xb1, 0x71, 0xf1, 0x09, 0x89, 0x49, 0xc9, 0x29, 0xa9,
			0x69, 0xe9, 0x19, 0x99, 0x59, 0xd9, 0x39, 0xb9, 0x79, 0xf9, 0x05,
			0x85, 0x45, 0xc5, 0x25, 0xa5, 0x65, 0xe5, 0x15, 0x95, 0x55, 0xd5,
			0x35, 0xb5, 0x75, 0xf5, 0x0d, 0x8d, 0x4d, 0xcd, 0x2d, 0xad, 0x6d,
			0xed, 0x1d, 0x9d, 0x5d, 0xdd, 0x3d, 0xbd, 0x7d, 0xfd, 0x03, 0x83,
			0x43, 0xc3, 0x23, 0xa3, 0x63, 0xe3, 0x13, 0x93, 0x53, 0xd3, 0x33,
			0xb3, 0x73, 0xf3, 0x0b, 0x8b, 0x4b, 0xcb, 0x2b, 0xab, 0x6b, 0xeb,
			0x1b, 0x9b, 0x5b, 0xdb, 0x3b, 0xbb, 0x7b, 0xfb, 0x07, 0x87, 0x47,
			0xc7, 0x27, 0xa7, 0x67, 0xe7, 0x17, 0x97, 0x57, 0xd7, 0x37, 0xb7,
			0x77, 0xf7, 0x0f, 0x8f, 0x4f, 0xcf, 0x2f, 0xaf, 0x6f, 0xef, 0x1f,
			0x9f, 0x5f, 0xdf, 0x3f, 0xbf, 0x7f, 0xff, };
	
	// 応答コマンド[ESC v]
	static byte[] res = { 0x1b, 0x76 };
	// 初期化コード
	static byte[] prn_init_start = {
			// 初期化
			0x1b, '@',
			// 印字濃度設定（１２０％）
			0x1b, 0x59, (byte) 4, };
	
	// セルフテスト[ESC T]
	static byte[] prn_self_test = { 0x1b, 0x54, };
	// 電源の切断[ESC +]
	static byte[] prn_power_off = { 0x1b, 0x2b, };
	// 圧縮構造のビットイメージ指定(非圧縮モード)[ESC *]
	static byte[] prn_bmp_c10 = { 0x1b, 0x2a, 0x10, };
	// 印字および紙送り
	static byte[] prn_feed_24dot = { 0x1b, 0x4a, 24, };
	// 印字および紙送り
	static byte[] prn_feed_4lines = { 0x1b, 0x64, 4, };
	// 印字および紙送り
	static byte[] prn_feed_1lines = { 0x1b, 0x64, 1, };
	// 印字位置指定
	static byte[] prn_align_left = { 0x1b, 0x61, 0x00, };
	static byte[] prn_align_centre = { 0x1b, 0x61, 0x01, };
	static byte[] prn_align_right = { 0x1b, 0x61, 0x02, };
	
	public byte bulkFS  = 0x00;
	public byte bulkESC = 0x00;
	
	// 情報サイズ(漢字基準とする)
	static int[][] qr_data_size = {
		// L    M    Q    H
		{ 10,   8,   7,   4}, // v1
		{ 48,  38,  28,  21}, // v4
		{ 82,  65,  45,  36}, // v6
		{119,  93,  66,  52}, // v8
		{167, 131,  93,  74}, // v10
		{226, 177, 125,  96}, // v12
		{282, 223, 159, 120}  // v14
	};
	
	public String picturePath = "";
	public int qr_v = 1;
	public int qr_size = 6;
	
	public enum qr_version {
		v1(0),
		v4(1),
		v6(2),
		v8(3),
		v10(4),
		v12(5),
		v14(6),
		;
		
		private final int id;
		
		private qr_version(final int id) {
			this.id = id;
		}
		
		public int getInt() {
			return this.id;
		}
	}
	

	/**
	 * ビットマップ画像をバイトデータに変換する
	 * 
	 * @param src
	 *            Bitmap
	 * @param format
	 *            Bitmap.CompressFormat
	 * @param quality
	 *            int
	 * @return byte[]
	 */
	public static byte[] chngBmpToData(Bitmap src,
			Bitmap.CompressFormat format, int quality) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		src.compress(format, quality, output);
		return output.toByteArray();
	}

	/**
	 * プリント
	 */
	public void OutTextPrint(String address, String SDInFile) {
		boolean sts;
		int printer;
		int prn_status;
		byte[] cmd;
		byte[] tmp;
		
		//エディットテキストからテキスト取得
		String strtextview;
		EditText editText1 = (EditText) findViewById(R.id.editText1);
		strtextview = editText1.getText().toString();
		
		// テキストが無い場合リターン
		if(strtextview.length() < 1){
			return;
		}
		
		// プリント初期化処理
		if (!printerStart(address)) {
			return;
		}
		
		// シフトJISコード指定[FS C]
		cmd = new byte[] { 0x1c, 0x43, 0x01 };
		sts = PrinterSDK.sendData(cmd, cmd.length);

		String s1;
		String str;

		// HtHtmlLexer
		// http://www.ne.jp/asahi/hishidama/home/tech/soft/java/htlexer.html
		HtLexer lexer = new HtLexer();
		lexer.setTarget(strtextview);
		ListToken lstTkn;

		// 簡易ESC/POSタグ解析
		try {
			lstTkn = lexer.parse();
			lexer.close();
			s1 = "";
			// ListToken list;
			for (Token t : lstTkn) {
				if (t instanceof Tag) {
					Tag tag = (Tag) t;
					// N:文字装飾初期化
					if (tag.getName().toLowerCase().equals("n")) {
						// 初期化(ASCII、全角)
						bulkESC = 0x00;
						bulkFS  = 0x00;
						cmd = new byte[] { 0x1b, 0x21, bulkESC, 0x1c, 0x21, bulkFS };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// LOGO:ロゴ
					else if (tag.getName().toLowerCase().equals("logo")) {
						logo_out(0);
					} else if (tag.getName().equals("time")) {
						Date date = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy年MM月dd日(E) HH:mm");

						s1 = sdf.format(date).toString();
						cmd = s1.getBytes("Shift_JIS");
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// FONTA:文字フォントＡ
					else if (tag.getName().toLowerCase().equals("fonta")) {
						// 一括モード指定(ASCII[ESC !]、全角[FS !])
						bulkESC &= 0xFE;
						bulkFS  &= 0xFE;
						cmd = new byte[] { 0x1b, 0x21, bulkESC, 0x1c, 0x21, bulkFS };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// FONTB:文字フォントＢ
					else if (tag.getName().toLowerCase().equals("fontb")) {
						// 一括モード指定(ASCII[ESC !]、全角[FS !])
						bulkESC |= 0x01;
						bulkFS  |= 0x01;
						cmd = new byte[] { 0x1b, 0x21, bulkESC, 0x1c, 0x21, bulkFS };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// B:強調文字(ASCIIのみ)
					else if (tag.getName().toLowerCase().equals("b")) {
						// 一括モード指定(ASCII[ESC !])
						bulkESC |= 0x08;
						cmd = new byte[] { 0x1b, 0x21, bulkESC };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// B2:横倍
					else if (tag.getName().toLowerCase().equals("b2")) {
						// 一括モード指定(ASCII[ESC !]、全角[FS !])
						bulkESC &= 0x81; // 文字フォント、アンダーラインのみ引継ぎ
						bulkFS  &= 0x81; // 文字フォント、アンダーラインのみ引継ぎ
						bulkESC |= 0x20; // 横倍(0010 0000)
						bulkFS  |= 0x04; // 横倍(0000 0100)
						cmd = new byte[] { 0x1b, 0x21, bulkESC, 0x1c, 0x21, bulkFS };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// B3:縦倍
					else if (tag.getName().toLowerCase().equals("b3")) {
						// 一括モード指定(ASCII[ESC !]、全角[FS !])
						bulkESC &= 0x81; // 文字フォント、アンダーラインのみ引継ぎ
						bulkFS  &= 0x81; // 文字フォント、アンダーラインのみ引継ぎ
						bulkESC |= 0x10; // 縦倍(0001 0000)
						bulkFS  |= 0x08; // 縦倍(0000 1000)
						cmd = new byte[] { 0x1b, 0x21, bulkESC, 0x1c, 0x21, bulkFS };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// B4:４倍角
					else if (tag.getName().toLowerCase().equals("b4")) {
						// 一括モード指定(ASCII[ESC !]、全角[FS !])
						bulkESC &= 0x81; // 文字フォント、アンダーラインのみ引継ぎ
						bulkFS  &= 0x81; // 文字フォント、アンダーラインのみ引継ぎ
						bulkESC |= 0x30; // ４倍(0011 0000)
						bulkFS  |= 0x0c; // ４倍(0000 1100)
						cmd = new byte[] { 0x1b, 0x21, bulkESC, 0x1c, 0x21, bulkFS };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// U:アンダーライン(1ドットピッチ デフォルト)
					else if (tag.getName().toLowerCase().equals("u1")) {
						// アンダーラインピッチの設定(ASCII[ESC -]、全角[FS -])
						cmd = new byte[] { 0x1b, 0x2d, 1 , 0x1c, 0x2d, 1};
						sts = PrinterSDK.sendData(cmd, cmd.length);
						// 一括モード指定(ASCII[ESC !]、全角[FS !])
						bulkESC |= 0x80; // アンダーライン(1000 0000)
						bulkFS  |= 0x80; // アンダーライン(1000 0000)
						cmd = new byte[] { 0x1b, 0x21, bulkESC, 0x1c, 0x21, bulkFS };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// U2:アンダーライン(2ドットピッチ)
					else if (tag.getName().toLowerCase().equals("u2")) {
						// アンダーラインピッチの設定(ASCII[ESC -]、全角[FS -])
						cmd = new byte[] { 0x1b, 0x2d, 2 , 0x1c, 0x2d, 2};
						sts = PrinterSDK.sendData(cmd, cmd.length);
						// 一括モード指定(ASCII[ESC !]、全角[FS !])
						bulkESC |= 0x80; // アンダーライン(1000 0000)
						bulkFS  |= 0x80; // アンダーライン(1000 0000)
						cmd = new byte[] { 0x1b, 0x21, bulkESC, 0x1c, 0x21, bulkFS };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// R:倒立文字
					else if (tag.getName().toLowerCase().equals("r")) {
						// 倒立印字の指定・解除[ESC {]
						cmd = new byte[] { 0x1b, 0x7b, 0x01 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// D1:印字濃度60%
					else if (tag.getName().toLowerCase().equals("d1")) {
						// コマンド(ESC Y n)
						cmd = new byte[] { 0x1b, 0x59, 0x00 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// D2:印字濃度75%
					else if (tag.getName().toLowerCase().equals("d2")) {
						// コマンド(ESC Y n)
						cmd = new byte[] { 0x1b, 0x59, 0x01 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// D3:印字濃度90%
					else if (tag.getName().toLowerCase().equals("d3")) {
						// コマンド(ESC Y n)
						cmd = new byte[] { 0x1b, 0x59, 0x02 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// D4:印字濃度100%
					else if (tag.getName().toLowerCase().equals("d4")) {
						// コマンド(ESC Y n)
						cmd = new byte[] { 0x1b, 0x59, 0x03 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// D5:印字濃度120%
					else if (tag.getName().toLowerCase().equals("d5")) {
						// コマンド(ESC Y n)
						cmd = new byte[] { 0x1b, 0x59, 0x04 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// D6:印字濃度140%
					else if (tag.getName().toLowerCase().equals("D6")) {
						// コマンド(ESC Y n)
						cmd = new byte[] { 0x1b, 0x59, 0x05 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// D7:印字濃度160%
					else if (tag.getName().toLowerCase().equals("d7")) {
						// コマンド(ESC Y n)
						cmd = new byte[] { 0x1b, 0x59, 0x06 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// CENTER:位置揃え
					else if (tag.getName().toLowerCase().equals("center")) {
						// コマンド(ESC a n)
						cmd = new byte[] { 0x1b, 0x61, 0x01 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// RIGHT:位置揃え
					else if (tag.getName().toLowerCase().equals("right")) {
						// コマンド(ESC a n)
						cmd = new byte[] { 0x1b, 0x61, 0x02 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// HRI文字フォントの印字設定(印字なし)
					else if (tag.getName().toLowerCase().equals("hri-none")){
						// コマンド(GS H)
						cmd = new byte[] { 0x1d, 0x48, 0 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// HRI文字フォントの印字設定(印字↑)
					else if (tag.getName().toLowerCase().equals("hri-top")){
						// コマンド(GS H)
						cmd = new byte[] { 0x1d, 0x48, 1 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// HRI文字フォントの印字設定(印字↓)
					else if (tag.getName().toLowerCase().equals("hri-bottom")){
						// コマンド(GS H)
						cmd = new byte[] { 0x1d, 0x48, 2 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// HRI文字フォントの印字設定(印字上下)
					else if (tag.getName().toLowerCase().equals("hri-top-bottom")){
						// コマンド(GS H)
						cmd = new byte[] { 0x1d, 0x48, 3 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// HRI文字フォントの設定(FONT A)
					else if (tag.getName().toLowerCase().equals("hria")){
						// コマンド(GS f)
						cmd = new byte[] { 0x1d, 0x66, 0 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// HRI文字フォントの設定(FONT B)
					else if (tag.getName().toLowerCase().equals("hrib")){
						// コマンド(GS f)
						cmd = new byte[] { 0x1d, 0x66, 1 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					// QRコードバージョン指定
					else if (tag.getName().toLowerCase().startsWith("qrv:")) {
						str = tag.getName().substring(4);
						if (0 < str.length() && str.length() < 3) {
							qr_size = Integer.valueOf(str);
							switch(Integer.valueOf(str)){
							case 1:
								qr_v = 0;
								break;
							case 4:
								qr_v = 1;
								break;
							case 6:
								qr_v = 2;
								break;
							case 8:
								qr_v = 3;
								break;
							case 10:
								qr_v = 4;
								break;
							case 12:
								qr_v = 5;
								break;
							case 14:
								qr_v = 6;
								break;
							default:
								qr_size = 6;
								break;
							}
						}
					}
					// QRコード
					else if (tag.getName().toLowerCase().startsWith("qr:")) {
						str = tag.getName().substring(3);
						// 指定バージョンのエラーコレクションレベルの高い順から選択
						int i;
						for(i = 3; i > 0; i--){
							if(str.length() < qr_data_size[qr_v][i]){
								break;
							}
						}
						if(i >= 0){
							byte _qr_size = (byte)qr_size;
							byte _qr_eccl = (byte)i;
							tmp = str.getBytes("Shift_JIS");
							short size = (short)tmp.length;
							byte high = (byte)(size & 0xff00);
							byte low  = (byte)(size & 0x00ff);
							
							cmd = new byte[] { 0x1d, 0x51, 6, _qr_size, _qr_eccl, low, high};
							sts = PrinterSDK.sendData(cmd, cmd.length);
							
							// デバッグ
							//android.util.Log.e("", "tmp.lenght"+tmp.length);
							//android.util.Log.e("", "high"+high+":low"+low);
							// バーコードデータ
							sts = PrinterSDK.sendData(tmp, tmp.length);
							
							//TODO:大きさによって紙送りを変更する
							//cmd = new byte[] { 0x1b, 0x4a, 0x78, 0x0a}; // 紙送り
							//sts = PrinterSDK.sendData(cmd, cmd.length);
						}
					}
					// バーコード高さ(1～255)
					else if (tag.getName().toLowerCase().startsWith("bch:")) {
						str = tag.getName().substring(4);
						if (0 < str.length() && str.length() < 4) {
							int n = Integer.valueOf(str);
							//Byte n = Byte.valueOf(str);
							if (1 <= n && n <= 255) {
								cmd = new byte[] { 0x1d, 0x68, (byte)n };
							}else{
								cmd = new byte[] { 0x1d, 0x68, (byte)162 };
							}
							sts = PrinterSDK.sendData(cmd, cmd.length);
						}
					}
					// バーコード幅(2～8)
					else if (tag.getName().toLowerCase().startsWith("bcw:")) {
						str = tag.getName().substring(4);
						if (str.length() == 1) {
							int n = Integer.valueOf(str);
							if(2 <= n && n <= 8){
								cmd = new byte[] { 0x1d, 0x77, (byte)n };
							}else{
								cmd = new byte[] { 0x1d, 0x77, (byte)3 };
							}
							sts = PrinterSDK.sendData(cmd, cmd.length);
						}
					}
					// EAN13
					else if (tag.getName().toLowerCase().startsWith("ean13:")) { //TODO:12桁にそろえる？
						//Log.d("MB", "EAN13");
						str = tag.getName().substring(6);
						if(str.length() <= 12) {
							// EAN13
							cmd = new byte[] { 0x1d, 0x6b, 67 ,12 };
							sts = PrinterSDK.sendData(cmd, cmd.length);
							// データ
							cmd = str.getBytes("Shift_JIS");
							sts = PrinterSDK.sendData(cmd, cmd.length);
						}
					}
					// CODE 39
					else if (tag.getName().toLowerCase().startsWith("code39:")) {
						//Log.d("MB", "CODE 39");
						str = tag.getName().substring(7);
						if (0 < str.length() && str.length() < 8) {
							// CODE 39
							cmd = new byte[] { 0x1d, 0x6b, 4 };
							sts = PrinterSDK.sendData(cmd, cmd.length);
							// データ(7文字まで)
							cmd = str.toUpperCase().getBytes("Shift_JIS");
							sts = PrinterSDK.sendData(cmd, cmd.length); //大文字にするA-Z 0-9
							// NULL
							cmd = new byte[] { 0x00 };
							sts = PrinterSDK.sendData(cmd, cmd.length);
						}
					}
					// CODE 128
					/* CDとか内部付加を行わないので使用しない
					else if (tag.getName().toLowerCase().startsWith("code128:")) {
						str = tag.getName().substring(8);
						// CODE 128
						cmd = new byte[] { 0x1d, 0x6b, 73 , 5, 0x7b, 0x41};
						sts = PrinterSDK.sendData(cmd, cmd.length);
						cmd = str.getBytes("Shift_JIS");
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					*/
					// IFT
					else if (tag.getName().toLowerCase().startsWith("itf:")) {
						Log.d("MB", "ITF");
						str = tag.getName().substring(4);
						Log.d("MB", str);
						// 偶数バイトのみ
						if (str.length() % 2 == 0) {
							// ITF
							cmd = new byte[] { 0x1d, 0x6b, 5 };
							sts = PrinterSDK.sendData(cmd, cmd.length);
							// データ
							cmd = str.getBytes("Shift_JIS");
							sts = PrinterSDK.sendData(cmd, cmd.length);
							// NULL
							cmd = new byte[] { 0x00 };
							sts = PrinterSDK.sendData(cmd, cmd.length);
							//Log.d("MB", "ITF_end");
						}
					}
					//PDF417
					/* 確認が出来ないので使用しない
					else if (tag.getName().toLowerCase().equals("pdf417")) {
						cmd = new byte[] { 0x1d, 0x6b, 74, 0, 0x1b, 0x00 };
						sts = PrinterSDK.sendData(cmd, cmd.length);
						s1 = "http://www.sanei-elec.co.jp";
						cmd = s1.getBytes("Shift_JIS");
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
					*/
					// 改行
					else if (tag.getName().toLowerCase().equals("br")) {
						s1 = "\n";
						try {
							cmd = s1.getBytes("Shift_JIS");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						sts = PrinterSDK.sendData(cmd, cmd.length);
					}
				} else {
					s1 = t.getText().replace("\n", "");
					try {
						cmd = s1.getBytes("Shift_JIS");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					sts = PrinterSDK.sendData(cmd, cmd.length);
					int psts;
					psts = PrinterSDK.getStatus();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 紙送り[ESC J]
		cmd = new byte[] { 0x1b, 0x4a, (byte) 0x80 };
		sts = PrinterSDK.sendData(cmd, cmd.length);

		int len = 0;
		byte[] data = new byte[16];
		// ステータスを得る（ここで切断までの時間を調整）
		len = PrinterSDK.receiveData(data);
		if (len == -1) {
			new AlertDialog.Builder(demo.this)
				.setTitle("error")
				.setMessage("読み込みエラーです。")
				.setNeutralButton("OK", null)
				.show();
			// 入出力ストリーム、及びbluetoothを閉じる
			PrinterSDK.btClose();
			return;
		}
		// 入出力ストリーム、及びbluetoothを閉じる
		PrinterSDK.btClose();
		return;
	}

	/**
	 * セルフテスト
	 */
	public void OutSelfTest() {

		boolean sts;

		if (!printerStart(address)) {
			return;
		}

		// 初期化
		sts = PrinterSDK.sendData(prn_init_start, prn_init_start.length);
		// セルフテスト
		sts = PrinterSDK.sendData(prn_self_test, prn_self_test.length);

		int len = 0;
		byte[] data = new byte[16];
		// ステータスを得る(ここで切断までの時間を調整)
		len = PrinterSDK.receiveData(data);
		if (len == -1) {
			new AlertDialog.Builder(demo.this)
			.setTitle("error")
			.setMessage("読み込みエラーです。")
			.setNeutralButton("OK", null)
			.show();
			// 入出力ストリーム、及びbluetoothを閉じる
			PrinterSDK.btClose();
			return;
		}
		// 入出力ストリーム、及びbluetoothを閉じる
		PrinterSDK.btClose();
		return;
	}
	
	/**
	 * プリントスタート
	 */
	public boolean printerStart(String address) {
		boolean bsts;
		int psts;

		// Bluetooth接続
		bsts = PrinterSDK.btConnect(address);
		if (bsts == false) {
			// アラートの表示
			new AlertDialog.Builder(demo.this)
			.setTitle("error")
			.setMessage("コネクト出来ません。")
			.setNeutralButton("OK", null)
			.show();
			return false;
		}

		// プリンタタイプ取得
		psts = PrinterSDK.getPrinterType();
		if (psts == -1) {
			// アラートの表示
			new AlertDialog.Builder(demo.this)
			.setTitle("error")
			.setMessage("プリンタタイプ取得エラーです。")
			.setNeutralButton("OK", null)
			.show();
			return false;
		}

		// 初期化コマンド送信
		bsts = PrinterSDK.initPrint();
		if (bsts == false) {
			new AlertDialog.Builder(demo.this)
			.setTitle("error")
			.setMessage("初期化エラーです。")
			.setNeutralButton("OK", null)
			.show();
			return false;
		}

		// プリンタステータスを取得
		psts = PrinterSDK.getStatus();
		if (psts == -1) {
			// アラートの表示
			new AlertDialog.Builder(demo.this)
			.setTitle("error")
			.setMessage("プリンタステータスエラーです。")
			.setNeutralButton("OK", null)
			.show();
			// 入出力ストリーム、及びbluetoothを閉じる
			PrinterSDK.btClose();
			return false;
		}
		// ステータス：用紙切れ、カバーオープン
		if ((psts & 0x01) != 0) {
			// アラートの表示
			new AlertDialog.Builder(demo.this)
			.setTitle("error")
			.setMessage("紙切れです。")
			.setNeutralButton("OK", null)
			.show();
			// 入出力ストリーム、及びbluetoothを閉じる
			PrinterSDK.btClose();
			return false;
		}
		// ステータス：プリンタヘッド温度異常
		if ((psts & 0x02) != 0) {
			// アラートの表示
			new AlertDialog.Builder(demo.this)
			.setTitle("error")
			.setMessage("温度エラーです。")
			.setNeutralButton("OK", null)
			.show();
			// 入出力ストリーム、及びbluetoothを閉じる
			PrinterSDK.btClose();
			return false;
		}
		// ステータス：ローバッテリー
		if ((psts & 0x04) != 0) {
			// アラートの表示
			new AlertDialog.Builder(demo.this)
			.setTitle("error")
			.setMessage("ローバッテリーです。")
			.setNeutralButton("OK", null)
			.show();
			// 入出力ストリーム、及びbluetoothを閉じる
			PrinterSDK.btClose();
			return false;
		}
		return true;
	}

	/**
	 * ロゴ(bmp印刷)
	 */
	public void logo_out(int flg) {
		boolean sts;
		int printer = 0;

		if(picturePath.length() <= 0) {
			return;
		}
		File inFile = new File(picturePath);
		
		byte[] readBytes = null;
		try {
			FileInputStream fis = new FileInputStream(inFile);
			readBytes = new byte[fis.available()];
			fis.read(readBytes);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/***** BMPファイルを印字 -TOP- *****/
		
		/*** BMP情報と印字に必要な変数 -TOP- ***/
		// ファイルのサイズ
		// int iSzFile = ((int)(readBytes[5]&0xff)<<24) +
		// ((int)(readBytes[4]&0xff)<<16) + ((int)(readBytes[3]&0xff)<<8) +
		// (int)(readBytes[2]&0xff);
		int iBzOffset = ((int) (readBytes[13] & 0xff) << 24)
				+ ((int) (readBytes[12] & 0xff) << 16)
				+ ((int) (readBytes[11] & 0xff) << 8)
				+ (int) (readBytes[10] & 0xff);

		// 読み込んだBMPデータの横方向、縦方向のピクセル数とバイト数を取得
		int iSzPxX = ((int) (readBytes[21] & 0xff) << 24)
				+ ((int) (readBytes[20] & 0xff) << 16)
				+ ((int) (readBytes[19] & 0xff) << 8)
				+ (int) (readBytes[18] & 0xff);
		int iSzImgX = iSzPxX / 8;
		int iSzPxY = ((int) (readBytes[25] & 0xff) << 24)
				+ ((int) (readBytes[24] & 0xff) << 16)
				+ ((int) (readBytes[23] & 0xff) << 8)
				+ (int) (readBytes[22] & 0xff);
		int iSzImgY = iSzPxY;

		byte[] cmd_width = new byte[1];
		// BMPデータの横方向のLONG数とバイト数
		int iSzLngX = (int) (Math.ceil((double) (iSzPxX) / 32.0));
		int iSzLngXByt = iSzLngX * 4;
		// BMPデータに含まれるパディングのバイト数
		int iSzPad = 0;
		if ((iSzImgX % 2) != 0) {
			iSzPad = 2 - (iSzImgX % 2);
		}
		// イメージの縦方向のライン数、最後の分割画像の画像部分のライン数を算出。
		// 分割画像のサイズ = 24ライン * （印字可能な最大の横方向のバイト数、または画像の横幅のバイト数）
		// 分割画像の単位で送信データを構成する。
		int iImgLine, iImgLastLine;
		if (iSzPxY < 24) {
			iImgLine = iSzImgY;
		} else {
			iImgLine = 24;
		}
		if ((iSzPxY % 24) == 0) {
			iImgLastLine = 24;
		} else {
			iImgLastLine = 24 - (iSzPxY % 24);
		}
		/*** BMP情報と印字に必要な変数 -END- ***/
		
		/*** 印字用バッファ領域の生成 -TOP- ***/
		// 宣言
		byte[] bImgBuf = null;
		// Y方向の画面分割数
		int iImgDiv = (int) (Math.ceil((double) (iSzImgY) / 24.0));
		// 実体の生成
		int iMaxWidth;

		if (printer == 0) {
			if (iSzPxX > 384)
				iMaxWidth = 48; // SM1-21
			else
				iMaxWidth = iSzImgX;
		} else if (printer == 1) {
			if (iSzPxX > 576)
				iMaxWidth = 72; // BLM-80
			else
				iMaxWidth = iSzImgX;
		} else {
			if (iSzPxX > 832)
				iMaxWidth = 104; // SM2-41
			else
				iMaxWidth = iSzImgX;
		}

		cmd_width[0] = (byte) iMaxWidth;

		bImgBuf = new byte[iImgLine * iMaxWidth];
		/*** 印字用バッファ領域の生成 -END- ***/

		/*** 印字用バッファにBMPファイルから読み込んだデータを格納する -TOP- ***/
		// ループカウンタ、その他
		int j, k, m, line, offset;
		int iCntD, iCntL, iCntW, iArrNum;
		k = iSzLngXByt - iSzPad;
		m = 0;
		iArrNum = 0;
		// 縦方向ループ（画像全体）
		for (iCntD = 0; iCntD < iImgDiv; iCntD++) {
			// 縦方向ループ（24ライン単位）
			for (iCntL = 0; iCntL < iImgLine; iCntL++) {
				line = (iSzPxY - ((iCntD * 24) + iCntL) - 1);
				offset = iBzOffset + (line * iSzLngXByt);
				// 横方向ループ（印字可能な最大の横方向のバイト数、または画像の横幅のバイト数）
				for (iCntW = 0; iCntW < iMaxWidth; iCntW++) {
					j = offset + iCntW;
					m = iCntL * iMaxWidth + iCntW;
					// BMPに含まれる0x00パディングをスキップして、送信データを作る。
					if (iCntW < k) {
						// 最終ブロック？
						if (iCntD == (iImgDiv - 1)) {
							// 実データが無いライン
							// 例えば、画像のサイズが768px*545pxの場合、必要な分割画面の数は23となり
							// (545÷24=22余17)、最後の分割画面は17ライン分ほど画像データが不足する。
							// 不足分は0x00(白)で埋めておく。
							if (iCntL >= (24 - iImgLastLine)) {
								bImgBuf[m] = 0x00;
							}
							// 実データがあるライン
							else {
								// １バイト毎にMSBとLSBを逆転する(コマンドリファレンスp22を参照)
								// 例)
								// 元データが"0xC5(= 11000101b)"であれば、"0xA3(= 10100011b)"に変換。
								if (readBytes[j] < 0) {
									iArrNum = readBytes[j] + 256;
								} else {
									iArrNum = readBytes[j];
								}
								bImgBuf[m] = (byte) bit_reverse[iArrNum];
							}
						}
						// 通常ライン
						else {
							if (readBytes[j] < 0) {
								iArrNum = readBytes[j] + 256;
							} else {
								iArrNum = readBytes[j];
							}
							bImgBuf[m] = (byte) bit_reverse[iArrNum];
						}
					}
				}
			}
			// BMPイメージの印字
			// イメージコマンド送信
			sts = PrinterSDK.sendData(prn_bmp_c10, prn_bmp_c10.length);
			if (sts == false) {
				new AlertDialog.Builder(demo.this).setTitle("error")
						.setMessage("書き込みエラーです。").setNeutralButton("OK", null)
						.show();
				// 入出力ストリーム、及びbluetoothを閉じる
				PrinterSDK.btClose();
				return;
			}
			// イメージコマンドの横バイト数を送信
			sts = PrinterSDK.sendData(cmd_width, cmd_width.length);
			if (sts == false) {
				new AlertDialog.Builder(demo.this).setTitle("error")
						.setMessage("書き込みエラーです。").setNeutralButton("OK", null)
						.show();
				// 入出力ストリーム、及びbluetoothを閉じる
				PrinterSDK.btClose();
				return;
			}
			// イメージデータ送信
			sts = PrinterSDK.sendData(bImgBuf, bImgBuf.length);
			if (sts == false) {
				new AlertDialog.Builder(demo.this).setTitle("error")
						.setMessage("書き込みエラーです。").setNeutralButton("OK", null)
						.show();
				// 入出力ストリーム、及びbluetoothを閉じる
				PrinterSDK.btClose();
				return;
			}
			// 印字ポインタを24ライン分送る
			sts = PrinterSDK.sendData(prn_feed_24dot, prn_feed_24dot.length);
			if (sts == false) {
				new AlertDialog.Builder(demo.this).setTitle("error")
						.setMessage("書き込みエラーです。").setNeutralButton("OK", null)
						.show();
				// 入出力ストリーム、及びbluetoothを閉じる
				PrinterSDK.btClose();
				return;
			}
		}
		/*** 印字用バッファにBMPファイルから読み込んだデータを格納する -END- ***/
		
		/***** BMPファイルを印字 -END- *****/
		
		// ステータス取得命令送信
		int psts;
		psts = PrinterSDK.getStatus();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// ボタンを取り出す
		Button b = (Button) findViewById(R.id.widget30);

		// リスナークラスを作って登録する
		ClickListener1 listener1 = new ClickListener1();
		b.setOnClickListener(listener1);

		// ボタンを取り出す
		b = (Button) findViewById(R.id.widget31);
		// リスナークラスを作って登録する
		ClickListener2 listener2 = new ClickListener2();
		b.setOnClickListener(listener2);

		// ボタンを取り出す
		b = (Button) findViewById(R.id.button1);
		// リスナークラスを作って登録する
		ClickListener3 listener3 = new ClickListener3();
		b.setOnClickListener(listener3);

		// ボタンを取り出す
		b = (Button) findViewById(R.id.button2);
		// リスナークラスを作って登録する
		ClickListener4 listener4 = new ClickListener4();
		b.setOnClickListener(listener4);

		// デフォルトアドレスセット
		address = "00:00:00:00:00:00";

		// EditTextを取り出す
		EditText editText = (EditText) findViewById(R.id.widget33);
		// フォーカスを合わせなくして入力不可とする
		editText.setFocusable(false);
		// EditTextにアドレスをセットする
		editText.setText(address);
		
		serverIntent = new Intent(this, jp.ne.sakura.k_tubo.mobileprinter.devicelist.class);
	}
	
	// オプションメニュー
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		// メモリスイッチ
		case R.id.menu_settings:
			//Log.d("DEBUG", "Settings Selected.");
			Intent varIntent = new Intent(this, jp.ne.sakura.k_tubo.mobileprinter.memswitch.class);
			varIntent.putExtra("addr", address);
			startActivity(varIntent);
			
			break;
			
		// バージョン情報
		case R.id.menu_version:
			//Log.d("DEBUG", "Version Selected.");
			PackageInfo packageInfo = null;
			String str = "";
			try {
				packageInfo = getPackageManager().getPackageInfo("jp.ne.sakura.k_tubo.mobileprinter", PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
			// プリンタステータスを取得
			str = PrinterSDK.getSdkVersion();
			if(str.length() > 0) {
				str = "printerSDK: " + str + "\n";
			}
			str += "app version: " + packageInfo.versionCode + " / " + packageInfo.versionName;
			AlertDialog.Builder alertDialogBuilder =
					new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("バージョン情報");
			alertDialogBuilder.setIcon(R.drawable.icon);
			alertDialogBuilder.setMessage(str);
			alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			alertDialogBuilder.show();
			break;
			
		// コマンド説明
		case R.id.menu_command:
			//Log.d("DEBUG", "Settings Selected.");
			Intent varIntent2 = new Intent(this, jp.ne.sakura.k_tubo.mobileprinter.command.class);
			startActivity(varIntent2);
			break;
			
		// セルフテスト印字
		case R.id.menu_selftest:
			this.OutSelfTest();
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 100 && resultCode == RESULT_OK) {
			address = data.getStringExtra("device_address");
			// EditTextを取り出す
			EditText editText = (EditText) findViewById(R.id.widget33);
			// EditTextにアドレスをセットする
			editText.setText(address);
		}
		if (requestCode == RESULT_PICK_FILENAME && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			picturePath = cursor.getString(columnIndex);
			cursor.close();

			// パスをトースト表示
			Toast.makeText(this, picturePath, Toast.LENGTH_LONG).show();
		}

		if (requestCode == 12345 && resultCode == RESULT_OK) {

			String tPath = data.getDataString().replace("file://", "");
			File inFile = new File(tPath);

			String text = "";
			try {
				FileInputStream fis = new FileInputStream(inFile);
				BufferedReader in = null;
				in = new BufferedReader(new InputStreamReader(fis, "SJIS"));
				String tmp = "";
				while ((tmp = in.readLine()) != null) {
					text = text + tmp + "\n";
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			EditText editText = (EditText) findViewById(R.id.editText1);
			// エディットテキストにセット
			editText.setText(text);

			// パスをトースト表示
			Toast.makeText(this, tPath, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 印字ボタンの処理
	 */
	class ClickListener1 implements OnClickListener {
		public void onClick(View v) {
			// SDカードのファイルパス＆名称の取得
			String SDInFile = Environment.getExternalStorageDirectory()
					.getPath() + "/sm1-ami20.bmp";
			OutTextPrint(address, SDInFile);
		}
	}

	/**
	 * デバイス選択ボタンの処理
	 */
	class ClickListener2 implements OnClickListener {
		public void onClick(View v) {
			if (serverIntent != null) {
				startActivityForResult(serverIntent, 100);
			}
			// Intent serverIntent=new
			// Intent(demo,SANEI_DEMO.SAMPLE.devicelist.class);
			// startActivityForResult(serverIntent,RQ_CONNECT_DEVICE);
		}
	}

	int RESULT_PICK_FILENAME = 1;
	//ロゴファイル選択ボタンの処理
	class ClickListener3 implements OnClickListener {
		
		public void onClick(View v) {
			Intent i = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
			//pickFilenameFromGallery();
			startActivityForResult(i, RESULT_PICK_FILENAME);
		}
	}

	/**
	 * テキストファイル選択ボタンの処理
	 */
	class ClickListener4 implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			startActivityForResult(intent, 12345);
		}
	}
}
