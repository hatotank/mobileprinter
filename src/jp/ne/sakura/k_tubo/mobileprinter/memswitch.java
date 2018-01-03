package jp.ne.sakura.k_tubo.mobileprinter;

import com.saneielec.libs.PrinterSDK;
import jp.ne.sakura.k_tubo.mobileprinter.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

public class memswitch extends Activity {
	
	public byte[] mem_sw = {
			0x31  // N1  ブザー音有無(0:OFF、1:ON)
			,0x30 // N2  CR有効無効(0:CR無効、1:CR有効)
			,0x30 // N3  LF有効無効(0:LF有効、1:LF無効)
			,0x30 // N4  CRに続くLFを有効とする(SWICH NO=3の場合のみ)
			,0x30 // N5  フォントの設定(0:FONT A 24x12、1:FONT B 16x9)
			,0x30 // N6  0固定
			,0x30 // N7  IrDA有効無効(0:IrDA無効、1:IrDA有効)
			,0x30 // N8  Bluetoothﾃﾞｨｽｶﾊﾞﾘﾓｰﾄﾞ(0:ﾃﾞｨｽｶﾊﾞﾘﾓｰﾄﾞにする、1:ﾃﾞｨｽｶﾊﾞﾘﾓｰﾄﾞにしない)
			,0x31 // N9  USB機能有効無効(0:USB機能無効、1:USB機能有効)
			,0x31 // N10 USBデバイス設定(0:設定禁止、1:USBデバイスとして使用)
			};
	String address;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memswitch);
		Intent varIntent = getIntent();
		address = varIntent.getStringExtra("addr");
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
			return false;
		}
		// プリンタタイプ取得
		psts = PrinterSDK.getPrinterType();
		if (psts == -1) {
			return false;
		}
		// 初期化コマンド送信
		bsts = PrinterSDK.initPrint();
		if (bsts == false) {
			return false;
		}
		// プリンタステータスを取得
		psts = PrinterSDK.getStatus();
		if (psts == -1) {
			// 入出力ストリーム、及びbluetoothを閉じる
			PrinterSDK.btClose();
			return false;
		}
		return true;
	}
	
	public void set_mem_sw_radio(int n, int checkedIdR) {
		RadioButton radioButton = (RadioButton)findViewById(checkedIdR);
		
		if(radioButton.isChecked() == true){
			mem_sw[n] = '0';
		}else{
			mem_sw[n] = '1';
		}
	}
	
	public void onMemSwActivity(View v) {
		
		byte cmd[];
		boolean sts;
		// N1～N10
		set_mem_sw_radio(0,R.id.radio_mem_n1_0);
		set_mem_sw_radio(1,R.id.radio_mem_n2_0);
		set_mem_sw_radio(2,R.id.radio_mem_n3_0);
		set_mem_sw_radio(3,R.id.radio_mem_n4_0);
		set_mem_sw_radio(4,R.id.radio_mem_n5_0);
		set_mem_sw_radio(5,R.id.radio_mem_n6_0);
		set_mem_sw_radio(6,R.id.radio_mem_n7_0);
		set_mem_sw_radio(7,R.id.radio_mem_n8_0);
		set_mem_sw_radio(8,R.id.radio_mem_n9_0);
		set_mem_sw_radio(9,R.id.radio_mem_n10_0);
		
		sts = printerStart(address);
		if(sts){
			// メモリスイッチ設定
			cmd = new byte[] { 0x1d, 0x29 };
			sts = PrinterSDK.sendData(cmd, cmd.length);
			cmd = mem_sw;
			sts = PrinterSDK.sendData(cmd, cmd.length);
			// 電源切断[ESC +]
			cmd = new byte[] { 0x1b, 0x2b };
			sts = PrinterSDK.sendData(cmd, cmd.length);
			Toast.makeText(this, "メモリスイッチを設定しました。", Toast.LENGTH_LONG).show();
		}
		finish();
	}
}
