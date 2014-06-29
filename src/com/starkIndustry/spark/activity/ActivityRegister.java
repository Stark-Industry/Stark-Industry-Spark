package com.starkIndustry.spark.activity;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import org.jivesoftware.smack.packet.Registration;
import android.widget.Toast;
import com.starkIndustry.spark.R;
import com.starkIndustry.spark.talk.XmppTool;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ActivityRegister extends Activity {

	private XMPPConnection connect;
	private EditText useridText, pwdText,pwdConfirmText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		Button btn_reg=(Button)findViewById(R.id.formlogin_register);
		btn_reg.setOnClickListener(new BtregListener());
		useridText=(EditText)findViewById(R.id.formlogin_userid);
		pwdText=(EditText)findViewById(R.id.formlogin_pwd);
		pwdConfirmText=(EditText)findViewById(R.id.formlogin_pwd_confirm);
		connect = new XmppTool().getConnection();
		if (connect == null) {
			Toast.makeText(ActivityRegister.this, "connect failure", 0)
					.show();
			finish();
		}


	}
	public class BtregListener implements OnClickListener {


		public void onClick(View v) {
			// TODO Auto-generated method stub
			new RegThread().start();

		}

	}
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(ActivityRegister.this, "register succsed", 0)
				.show();
				break;
			case 2:
				Toast.makeText(ActivityRegister.this, "no data from server", 0)
						.show();
				break;
			case 3:
				Toast.makeText(ActivityRegister.this, "account exists", 0).show();
				break;
			case 4:
				Toast.makeText(ActivityRegister.this, "register failed", 0)
						.show();
				break;
			case 5:
				Toast.makeText(ActivityRegister.this, "the password must keep same", 0)
						.show();
				break;
			default:
				break;
			}
		}
	};

	public class RegThread extends Thread {
		final String USERID =  useridText.getText().toString();
		final String PWD = pwdText.getText().toString();
		final String PWDCONFIRM=pwdConfirmText.getText().toString();

		@Override
		public void run() {
			try {
				if(PWD.equalsIgnoreCase(PWDCONFIRM))
				{
					Registration reg = new Registration();
					reg.setType(IQ.Type.SET);
					reg.setTo(connect.getServiceName());
					reg.setUsername(USERID);
					reg.setPassword(PWD);
					reg.addAttribute("android", "geolo_createUser_android");
					System.out.println("reg:" + reg);
					PacketFilter filter = new AndFilter(new PacketIDFilter(
							reg.getPacketID()), new PacketTypeFilter(IQ.class));
					PacketCollector collector = connect.createPacketCollector(filter);
					connect.sendPacket(reg);

					IQ result = (IQ) collector.nextResult(SmackConfiguration
							.getPacketReplyTimeout());
					// Stop queuing results
					collector.cancel();
					if (result == null) {
						Log.d("ruan", "no result from server");
						handler.sendEmptyMessage(2);
					} else if (result.getType() == IQ.Type.ERROR) {
						if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
							Log.d("ruan", "account exists");
							handler.sendEmptyMessage(3);
						} else {
							Log.d("ruan", "account register failed");
							handler.sendEmptyMessage(4);
						}
					} else if (result.getType() == IQ.Type.RESULT) {
						System.out.println("good");
						Log.d("ruan", "account register succsed");
						handler.sendEmptyMessage(1);
					}
				}
				else {
						handler.sendEmptyMessage(5);
				}

			} catch (Exception e) {
				Log.d("ruan", "account register failed");
				handler.sendEmptyMessage(3);
				connect.disconnect();
			}
		}
	}

}
