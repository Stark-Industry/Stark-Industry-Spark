package com.starkIndustry.spark.activity;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Message;

import com.starkIndustry.spark.R;
import com.starkIndustry.spark.talk.ChatContent;
import com.starkIndustry.spark.talk.ChatMsg;
import com.starkIndustry.spark.talk.TimeRender;
import com.starkIndustry.spark.talk.XmppTool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityChat extends Activity {

	private ChatContentchatContentAdapter chatContentAdapter;
	private ArrayList<ChatMsg> chatMsgList;
	private ListView chatContentView;
	private String userJID;
	private String chatJID;
	private String chatName;
	private EditText msgText;
	private static XMPPConnection connect;
	private Chat newchat;
	private ChatManager chatManager;
	protected  void RegisterMessageListener() {
		chatManager=connect.getChatManager();
		chatManager.addChatListener(new ChatManagerListener() {
					
		public void chatCreated(Chat chat, boolean arg1) {
		chat.addMessageListener(new MessageListener() {
							
		public void processMessage(Chat arg0, Message message) {
						
			String msg=message.getBody();
			if(msg!=null)
			{
				Log.d("rxm",msg);
				new ChatContent().addChat(chatName, new ChatMsg(chatName,
						msg, TimeRender.getDate(), "IN"));
				mhandler.sendEmptyMessage(11);
			}
			}
		});
						
			}
		});

	}
	

	public void semdmessage(final String msg,final String msgto) {
		
		newchat = chatManager.createChat(msgto, null);
		new Thread()
        {
        	@Override
        	public void run() {
        		try {
        			
					newchat.sendMessage(msg);
					//sendhandlemsg(User,msg,true);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }.start();
        
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		chatMsgList = new ArrayList<ChatMsg>();
		connect = new XmppTool().getConnection();
		RegisterMessageListener();
		this.msgText = (EditText) findViewById(R.id.formclient_text);
		this.chatContentView = (ListView) findViewById(R.id.formclient_listview);
		chatContentView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		this.chatContentAdapter = new ChatContentchatContentAdapter(this);
		chatContentView.setAdapter(chatContentAdapter);

		userJID = getIntent().getStringExtra("USERID");
		chatJID = getIntent().getStringExtra("TALKID");
		chatName = chatJID.substring(0, chatJID.indexOf("@"));
		chatMsgList = new ChatContent().getList(chatName);

		System.out.println("talkID " + chatJID + "userid : " + userJID);

		// message listener
		ChatManager cm = connect.getChatManager();
		final Chat newchat = cm.createChat(chatJID, null);
		/*try {
			newchat.sendMessage("3dsa");
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		// send message
		Button btsend = (Button) findViewById(R.id.formclient_btsend);
		btsend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String msg = msgText.getText().toString();
				if (msg.length() > 0) {
					new ChatContent().addChat(chatName, new ChatMsg(userJID,
							msg, TimeRender.getDate(), "OUT"));
					mhandler.sendEmptyMessage(11);
					try {
						newchat.sendMessage(msg);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
				msgText.setText("");
			}
		});
		new RefreshUI().start();
	}

	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 11:
				chatMsgList = new ChatContent().getList(chatName);
				chatContentAdapter.notifyDataSetChanged();
				break;
			case 2:
				break;
			default:
				break;
			}
		};
	};

	public class RefreshUI extends Thread {

		public void run() {
			while (true) {
				mhandler.sendEmptyMessage(11);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	class ChatContentchatContentAdapter extends BaseAdapter {

		private Context cxt;
		private LayoutInflater inflater;

		public ChatContentchatContentAdapter(ActivityChat formClient) {
			this.cxt = formClient;
		}

		@Override
		public int getCount() {
			if (chatMsgList != null) {
				return chatMsgList.size();
			}
			return -1;
		}

		@Override
		public Object getItem(int position) {
			if (chatMsgList != null) {
				return chatMsgList.get(position);
			}
			return -1;
		}

		@Override
		public long getItemId(int position) {
			if (chatMsgList != null) {
				return position;
			}
			return -1;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			this.inflater = (LayoutInflater) this.cxt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (chatMsgList.get(position).mFrom.equals("IN")) {
				convertView = this.inflater.inflate(
						R.layout.formclient_chat_in, null);
			} else {
				convertView = this.inflater.inflate(
						R.layout.formclient_chat_out, null);
			}
			TextView useridView = (TextView) convertView
					.findViewById(R.id.formclient_row_userid);
			TextView dateView = (TextView) convertView
					.findViewById(R.id.formclient_row_date);
			TextView msgView = (TextView) convertView
					.findViewById(R.id.formclient_row_msg);
			useridView.setText(chatMsgList.get(position).mUserId);
			dateView.setText(chatMsgList.get(position).mDate);
			msgView.setText(chatMsgList.get(position).mMsg);
			return convertView;
		}
	}
}