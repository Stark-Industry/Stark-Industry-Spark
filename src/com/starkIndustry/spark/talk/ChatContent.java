package com.starkIndustry.spark.talk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

public class ChatContent {
	
	static private HashMap<String, ArrayList<ChatMsg>> sChatItem = new HashMap<String, ArrayList<ChatMsg>>();
	//private HashMap<String, ArrayList<ChatMsg>> map;
	private ArrayList<ChatMsg> mList;
	//private ChatMsg cMsg;
		
	
	public void creatChatLog (String str) {	
		ArrayList<ChatMsg> list = new ArrayList<ChatMsg>();
		sChatItem.put(str, list);
		
	}

	
	public void addChat (String key, ChatMsg msg) {
		mList = sChatItem.get(key);
		if(mList == null){
			creatChatLog(key);
		}
		sChatItem.get(key).add(msg);
		
	}
	
	public ArrayList<ChatMsg> getList(String key){
		return sChatItem.get(key);
	}
	
	public void putChat(String key){
		mList = sChatItem.get(key);
		if(mList == null){
			System.out.println("alist is null");
			return;
		}
		
		int i = mList.size();
			System.out.println("the size is "  + mList.size() );
		
	}
	
	public void putMap(){
		System.out.println("map" + sChatItem.toString());
	}

}
