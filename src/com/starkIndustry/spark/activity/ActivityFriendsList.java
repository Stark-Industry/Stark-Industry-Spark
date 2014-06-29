package com.starkIndustry.spark.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import com.starkIndustry.spark.R;
import com.starkIndustry.spark.activity.ActivityLogin.LoginThread;
import com.starkIndustry.spark.talk.ChatContent;
import com.starkIndustry.spark.talk.ChatMsg;
import com.starkIndustry.spark.talk.TimeRender;
import com.starkIndustry.spark.talk.XmppTool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityFriendsList extends Activity {

	final private String host = "yu-pc";
	private XMPPConnection connect;
	private ListView friendListView;
	private ImageView chatSuggest;
	private String userJID;
	private String userName;
	private ArrayList<HashMap<String, Object>> friendListItem;
	private ArrayList<String> rosterList;
	private String temp;
	private SimpleAdapter friendListAdapter;
	private android.os.Message msg;
	private ChatContent chatContent;
	private ChatMsg chatMsg;
	private ChatManager chatManager;
	private ChatListener chatListener;
	private Roster roster;
	private Collection<RosterEntry> rosterEntries;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.formfriend_list);

		setTitle("Happy Every Day");
		userName=getIntent().getStringExtra("USERID"); 
		userJID = userName + "@" + host;
		connect = new XmppTool().getConnection();
		chatContent = new ChatContent();
		friendListView = (ListView) findViewById(R.id.ListView01);
		chatListener = new ChatListener();
		friendListItem = new ArrayList<HashMap<String, Object>>();
		rosterList = new ArrayList<String>();

		roster = connect.getRoster();
		roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);

		getRost();

		friendListAdapter = new SimpleAdapter(this, friendListItem,
				R.layout.friend_adapter,
				new String[] { "ItemImage", "ItemTitle", "ItemText" },
				new int[] { R.id.ItemImage, R.id.ItemTitle, R.id.ItemText });

		friendListView.setAdapter(friendListAdapter);

		friendListView.setOnItemClickListener(new FriendListViewOnItemClickListener());

		friendListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("长按菜单ContextMenu");
				menu.add(0, 0, 0, "长按菜单弹出0");
				menu.add(0, 1, 0, "长按菜单弹出1");
			}
		});

		chatManager = connect.getChatManager();
		chatManager.addChatListener(chatListener);
		roster.addRosterListener(new QuickRosterListener());
	}

	public void createRost(String name) {
		if (name.indexOf("@") == -1) {
			name = name + "@" + host;
		}
		try {
			roster.createEntry(name, null, new String[] { "Friends" });
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public class FriendListViewOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			chatSuggest = (ImageView) arg1.findViewById(R.id.chatSuggest);
			chatSuggest.setVisibility(View.GONE);
			TextView myText = (TextView) arg1.findViewById(R.id.ItemText);
			System.out.println(myText.getText().toString());
			Intent mIntent = new Intent();
			String talkto=myText.getText().toString().split("@")[0];
			mIntent.putExtra("TALKID", talkto+"@"+host);
			mIntent.putExtra("USERID", userJID);
			mIntent.setClass(ActivityFriendsList.this, ActivityChat.class);
			startActivity(mIntent);
		}

	}
	public void getAllFriends(String username) {
		try {
			UserSearchManager search = new UserSearchManager(connect);
			String searchf = "search." + host;
			Form searchForm = search.getSearchForm(searchf);
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("search",username );
			ReportedData data = search.getSearchResults(answerForm, searchf);

			Iterator<Row> it = data.getRows();
			Row row = null;
			String newName = "";
			while (it.hasNext()) {
				row = it.next();
				newName = row.getValues("Username").next().toString();
				if (!rosterList.contains(newName)) {
					createRost(newName);
				}
				Log.d("x", row.getValues("Username").next().toString());
			}
		} catch (Exception e) {
			Toast.makeText(this,
					e.getMessage() + " " + e.getClass().toString(),
					Toast.LENGTH_LONG).show();
		}

	}

	public void addItem(String title, String text) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ItemTitle", title);
		rosterList.add(title);
		map.put("ItemText", text);
		map.put("ItemImage", R.drawable.folder);
		friendListItem.add(map);
	}

	public void getRost() {
		// TODO Auto-generated method stub
		String rostJID;
		rosterEntries = roster.getEntries();
		friendListItem.clear();
		rosterList.clear();
		String rostName;
		int index = 0;
		for (RosterEntry entry : rosterEntries) {
			rostJID = entry.getUser();
			System.out.println(rostJID + "group" + entry.getGroups());
			index = rostJID.indexOf("@");
			if (index == -1) {
				System.out.println("is wrong");
				return;
			}
			rostName = rostJID.substring(0, index);
			if (!temp.equals(userJID)) {
				addItem(rostName, temp);
			}
		}
	}

	public class ChatListener implements ChatManagerListener {
		String chatJID,chatName;

		@Override
		public void chatCreated(Chat chat, boolean arg1) {
			// TODO Auto-generated method stub
			chat.addMessageListener(new MessageListener() {
				@Override
				public void processMessage(Chat chat, Message message) {
					if (message.getBody() != null) {
						System.out.println(" 2: Received from {"
								+ message.getFrom() + "}message: "
								+ message.getBody());
						chatJID = message.getFrom();
						chatName = chatJID.substring(0, chatJID.indexOf("@"));
						if (rosterList.contains(chatName)) {
							msg = handler.obtainMessage();
							msg.what = 1;
							msg.obj = chatName;
							msg.sendToTarget();

							chatContent.putMap();
							System.out.println("today " + temp);
							chatContent.putChat(chatName);
							chatMsg = new ChatMsg(chatName, message.getBody(),
									TimeRender.getDate(), "IN");
							chatContent.addChat(chatName, chatMsg);
							chatContent.putChat(chatName);
							chatContent.putMap();
						} else {
							createRost(temp);
						}
					}
				}
			});
		}
	}

	public class QuickRosterListener implements RosterListener {

		@Override
		public void entriesAdded(Collection<String> arg0) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(2);
		}

		@Override
		public void entriesDeleted(Collection<String> arg0) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(2);
		}

		@Override
		public void entriesUpdated(Collection<String> arg0) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(2);

		}

		@Override
		public void presenceChanged(Presence arg0) {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(2);
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		new XmppTool().closeConnection();
		System.exit(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 1, 1, "查找朋友");
		menu.add(0, 2, 2, "刷新朋友列表");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 1) {
			final EditText diaEdit = new EditText(this);
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle("请输入")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(diaEdit)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									temp = diaEdit.getText().toString();
									createRost(temp);
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();

			dialog.show();

		} else if (item.getItemId() == 2) {
			getAllFriends(userName);
		}
		return true;
	}

	public int getChildItem(String str) {

		int i = rosterList.indexOf(str);
		return i >= 0 ? i : -1;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String rostName;
			switch (msg.what) {
			case 1:
				rostName = (String) msg.obj;
				int i = getChildItem(rostName);
				if (i == -1) {
					return;
				}
				chatSuggest = (ImageView) friendListView.getChildAt(i).findViewById(
						R.id.chatSuggest);
				chatSuggest.setVisibility(View.VISIBLE);
				setTitle("dear, you have the new message");
				break;
			case 2:
				getRost();
				System.out.println("dasfdfsdfsdfsdf in listener3");
				friendListAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		};
	};

}
