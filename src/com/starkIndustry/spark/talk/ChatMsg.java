package com.starkIndustry.spark.talk;

public class ChatMsg {
	
		public String mUserId;
		public String mMsg;
		public String mDate;
		public String mFrom;

		public ChatMsg(String mUserId, String mMsg, String mDate, String mFrom) {
			this.mUserId = mUserId;
			this.mMsg = mMsg;
			this.mDate = mDate;
			this.mFrom = mFrom;
		}

		public String getMUserId() {
			return mUserId;
		}

		public void setMUserId(String mUserId) {
			this.mUserId = mUserId;
		}

		public String getMMsg() {
			return mMsg;
		}

		public void setMMsg(String mMsg) {
			this.mMsg = mMsg;
		}

		public String getMDate() {
			return mDate;
		}

		public void setMDate(String mDate) {
			this.mDate = mDate;
		}

		public String getMFrom() {
			return mFrom;
		}

		public void setMFrom(String mFrom) {
			this.mFrom = mFrom;
		}

		@Override
		public String toString() {
			return "ChatMsg [userid=" + mUserId + ", msg=" + mMsg + ", date="
					+ mDate + ", from=" + mFrom + ", toString()="
					+ super.toString() + "]";
		}
}


