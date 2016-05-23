package com.fixmytrip.train.notifications;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TrainStatus {

	private Date time;
	private int timeLate;
	private String message;
	private int trainNumber;
	private StatusReason statusReason;
	private String delayReason;
	private boolean isUpdate;
	private String station;


	public int getId() {
		switch(statusReason){
			case OVERALL_DELAYS:
			case BUS_BRIDGE:
				return statusReason.ordinal();
			default:
				return trainNumber;
		}
	}

	public static enum StatusReason {
		LATE,
		ANNULLED,
		GENERAL,
		ON_TIME,
		CURRENTLY_STOPPED,
		UNKNOWN,
		OVERALL_DELAYS,
		BUS_BRIDGE,
		BOARDING_PLATFORM,
		ON_THE_MOVE
		
	}
	
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public int getTimeLate() {
		return timeLate;
	}

	public void setTimeLate(int timeLate) {
		this.timeLate = timeLate;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getTrainNumber() {
		return trainNumber;
	}

	public void setTrainNumber(int trainNumber) {
		this.trainNumber = trainNumber;
	}

	public StatusReason getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(StatusReason statusReason) {
		this.statusReason = statusReason;
	}
	
	public String toString()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
		return dateFormat.format(time) + "\ttrain:" + trainNumber + "\tstation:" + station + "\tlate:" + timeLate + "\tisUpdate:" + isUpdate + "\t\tReason:" + statusReason + "\tDelay:" + delayReason + "\t\tmessage:" + message;
	}

	public String getDelayReason() {
		return delayReason;
	}

	public void setDelayReason(String delayReason) {
		this.delayReason = delayReason;
	}

	public boolean isUpdate() {
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}
}
