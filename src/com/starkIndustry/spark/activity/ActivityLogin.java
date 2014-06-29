package com.starkIndustry.spark.activity;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;

import com.starkIndustry.spark.R;
import com.starkIndustry.spark.talk.XmppTool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


public class ActivityLogin extends Activity {
	private EditText useridText, pwdText;
	private LinearLayout layout1, layout2;
	private XmppTool xmppTool;
	private XMPPConnection connect;
	private Button register;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		this.useridText = (EditText) findViewById(R.id.formlogin_userid);
		this.pwdText = (EditText) findViewById(R.id.formlogin_pwd);
		this.layout1 = (LinearLayout) findViewById(R.id.formlogin_layout1);
		this.layout2 = (LinearLayout) findViewById(R.id.formlogin_layout2);
		Runnable r = new NetWorkHandler();  
        Thread thread = new Thread(r);  
        thread.start();  
		Button btsave = (Button) findViewById(R.id.formlogin_btsubmit);
		btsave.setOnClickListener(new BtsaveListener());
		Button btcancel = (Button) findViewById(R.id.formlogin_btcancel);
		btcancel.setOnClickListener(new BtcancelListener());
		register = (Button) findViewById(R.id.formlogin_register);
		register.setOnClickListener(new RegisterListener());

	}
	public class NetWorkHandler implements Runnable
	{
		public void run()
		{
			try
			{
				xmppTool = new XmppTool();
			}
			catch(Exception e)
			{
				handler.sendEmptyMessage(3);
				finish();
			}
			connect = xmppTool.getConnection();
			if (connect == null) {
				handler.sendEmptyMessage(3);
				finish();
			}
		}
	}
	public class RegisterListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			startActivity(new Intent(ActivityLogin.this,ActivityRegister.class));

			
		}

	}

	public class BtsaveListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			new LoginThread().start();

		}

	}

	public class BtcancelListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}

	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				layout1.setVisibility(View.VISIBLE);
				layout2.setVisibility(View.GONE);
				break;
			case 2:
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.VISIBLE);
				Toast.makeText(ActivityLogin.this, "login failure", 0)
						.show();
				break;
			case 3:
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.VISIBLE);
				Toast.makeText(ActivityLogin.this, "connect failed", 0)
						.show();
				break;
			default:
				break;
			}
		};
	};

	public class LoginThread extends Thread {
		final String USERID =  useridText.getText().toString();
		final String PWD = pwdText.getText().toString();

		public void run() {
			handler.sendEmptyMessage(1);
			try {
				connect.login(USERID, PWD);
				Presence presence = new Presence(Presence.Type.available);
				connect.sendPacket(presence);
				Intent intent = new Intent(ActivityLogin.this,
						ActivityFriendsList.class);
				intent.putExtra("USERID", USERID);
				startActivity(intent);
				ActivityLogin.this.finish();
			} catch (Exception e) {
				Log.d("x", "in exception ");
				connect.disconnect();
				handler.sendEmptyMessage(2);
			}
		}
	}

}