package com.fixmytrip.train.notifications;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.fixmytrip.train.R;
import com.fixmytrip.train.trains.Train;
import com.fixmytrip.train.trains.TrainStation;
import com.fixmytrip.train.trains.TrainSystem;
import com.fixmytrip.train.utils.Constants;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Parser {

	private static ArrayList<String> stationAbbrevs = new ArrayList<String>();
	private static List<TrainStatus> allTrainStatuses = new ArrayList<TrainStatus>();
	private static int previousCount=0;

	public static List<TrainStatus> updateOfficialNotifications() {
		Document doc = null;
		List<TrainStatus> newTrainStatuses = new ArrayList<TrainStatus>();
		try {
			doc = Jsoup.connect(Constants.AlertsURL).get(); //Jsoup.parse(new URL(Constants.AlertsURL).openStream(), "Unicode", Constants.AlertsURL); // //Jsoup.parse(listOfFiles[i], "ISO-8859-1");
			for (Element table : doc.select("table")) {
				//System.out.println(listOfFiles[i].getName());
				if(previousCount>table.select("tr").size()) {
					previousCount = 0; //reset the count as it is most likely a new day
					allTrainStatuses.clear();
				}
				Element row;
				for (int i = table.select("tr").size()-1; i >= previousCount;i--) {
					row = table.select("tr").get(i);
					for (Element cell : row.select("td")) {
						TrainStatus trainStatus = groupUpdate(cell.text());
						if(trainStatus!=null)
							allTrainStatuses.add(trainStatus);
							newTrainStatuses.add(trainStatus);
					}

				}
				previousCount = table.select("tr").size();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newTrainStatuses;
	}

	private static TrainStatus groupUpdate(String text) {
		String textLower = text.toLowerCase();
		String dateString = null;
		int trainNum = -1;
		int timeLate = -1;
		TrainStatus trainStatus = new TrainStatus();
		TrainStatus.StatusReason reason = null;
		
		//LIST OF STATION IDENTIFIERS
		ArrayList<String> stations = new ArrayList<String>();
		stations.add("MAN"); //Mangonia
		stations.add("WPB"); //West Palm Beach
		stations.add("LAK");stations.add("LKW"); //Lake Worth
		stations.add("BOY"); //Boynton
		stations.add("DEL"); //Delray Beach
		stations.add("BOC"); // BOCA???
		stations.add("DER"); stations.add("DFB");//Deerfield
		stations.add("POM"); //Pompano
		stations.add("CYP"); //Cypress
		stations.add("FTL"); //Fort Lauderdale
		stations.add("FLA"); stations.add("FLL");//FLL Airport
		stations.add("SHE"); //Sheridan Street
		stations.add("HOL"); //Hollywood???
		stations.add("GOL"); //Golden Glades
		stations.add("OPL"); stations.add("OPA"); //Opa laka
		stations.add("MET"); //Metrorail transfer
		stations.add("HIM"); stations.add("HIA"); //Hialeah
		stations.add("MIC"); stations.add("MIA");//Miami Airport
		
		//MEANINGLESS MESSAGES
		ArrayList<String> messagesToIgnore = new ArrayList<String>();
		messagesToIgnore.add("No VIP messages!".toLowerCase());
		messagesToIgnore.add("Tri-Rail's Phone App has not been updated to reflect the opening of the Miami Airport Station. Please refer to the train schedule on www.tri-rail.com or call 1-800-TRI-RAIL (874-7245) to verify train times.".toLowerCase());
		messagesToIgnore.add("Tri-Rail trains and shuttle buses will operate on a holiday/weekend schedule".toLowerCase());
		messagesToIgnore.add("Bike car today on trains".toLowerCase());
		messagesToIgnore.add("VIP messages will not".toLowerCase());
		messagesToIgnore.add("THIS IS A TEST".toLowerCase());
		messagesToIgnore.add("Due to recent acts of theft and vandalism".toLowerCase());

		//messagesToIgnore.add("IF YOU SEE SOMETHING, SAY SOMETHING".toLowerCase());
		for(String message: messagesToIgnore)
			if(textLower.contains(message))
				return null;
		
		//UNIMPORTANT PART OF MESSAGES
		ArrayList<String> stringsToRemove = new ArrayList<String>();
		stringsToRemove.add("thank you for your cooperation. ");
		stringsToRemove.add("update to follow. ");
		stringsToRemove.add("we apologize for any inconvenience and ");
		stringsToRemove.add("thank you for your patience. ");
		stringsToRemove.add("we apologize for any inconvenience. ");
		stringsToRemove.add("attention passengers: ");
		stringsToRemove.add("attention passenger: ");
		stringsToRemove.add("fyi: ");
		stringsToRemove.add("advisory: ");
		stringsToRemove.add("alert: ");
		stringsToRemove.add("updates to follow");
		stringsToRemove.add("updates to follow");
		stringsToRemove.add("attention passengers ");
		for (String removal : stringsToRemove) {
			int index = textLower.indexOf(removal);
			if (index != -1) {
				text = text.substring(0, index)
						+ text.substring(index + removal.length(),
								text.length());
				textLower = text.toLowerCase();
			}
		}

		
		//FIND STATION FROM MESSAGE
		Pattern station = Pattern
				.compile("[A-Z][A-Z][A-Z]");
		Matcher mTEMP = station.matcher(text);
		while (mTEMP.find()) {
			if(!stationAbbrevs.contains(mTEMP.group(0)))
					stationAbbrevs.add(mTEMP.group(0));
		}
		for(String stationAbv : stations)
		{
			if(text.contains(stationAbv))
			{
				trainStatus.setStation(stationAbv);
				break;
			}
		}
		
		//GET TIME MESSAGE WAS SENT
		Pattern time = Pattern
				.compile("[0-9][0-9]?:[0-9][0-9]:[0-9][0-9] [AP](M)");
		Matcher m = time.matcher(text);
		if (m.find()) {
			dateString = m.group(0);
			text=text.replace(m.group(0), "");
			// s now contains "BAR"
		}
		textLower = text.toLowerCase();

		// GET TRAIN NUMBER
		Pattern train = Pattern.compile("[pP][0-9][0-9][0-9]");
		m = train.matcher(text);
		if (m.find()) {
			trainNum = Integer.parseInt(m.group(0).substring(1));
			// s now contains "BAR"
		} else {
			train = Pattern.compile(" 6[0-9][0-9] ");
			m = train.matcher(text);
			if (m.find()) {
				trainNum = Integer.parseInt(m.group(0).substring(1,m.group(0).length()-1));
				// s now contains "BAR"
			}
		}
		timeLate = getDelay(text);

		//SET REASON FOR THE MESSAGE
		if(textLower.contains("major delays") || textLower.contains("significant delays") || textLower.contains("can expect delays"))
		{
			reason = TrainStatus.StatusReason.OVERALL_DELAYS;
		}
		else if(textLower.contains("boarding on track"))
		{
			reason = TrainStatus.StatusReason.BOARDING_PLATFORM;
		}
		else if(textLower.contains("currently stop") || textLower.contains("is stopped ") || textLower.contains("stopped just")){
			reason = TrainStatus.StatusReason.CURRENTLY_STOPPED;
		} else if (textLower.contains("terminate")
				|| textLower.contains("annul")
				|| textLower.contains("cancelled")
				|| textLower.contains("de-train")
				|| textLower.contains("detrain")) {
			reason = TrainStatus.StatusReason.ANNULLED;
		} else if (textLower.contains("on-time") || textLower.contains("on time")) {
			reason = TrainStatus.StatusReason.ON_TIME;
		}else if (timeLate != -1 || textLower.contains("expect delays") ) {
			if(trainStatus.getStation()!=null || trainNum != -1)
				reason = TrainStatus.StatusReason.LATE;
			else
				reason = TrainStatus.StatusReason.OVERALL_DELAYS;
		}
		else if (textLower.contains("on the move"))
		{
			reason = TrainStatus.StatusReason.ON_THE_MOVE;
		}
		else if (textLower.contains("bridge"))
		{
			reason = TrainStatus.StatusReason.BUS_BRIDGE;
		}
		else if (trainNum == -1) {
			reason = TrainStatus.StatusReason.GENERAL;
		} else {
			reason = TrainStatus.StatusReason.UNKNOWN;
		}

		//FORMAT TIME
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
		Date date = null;
		try {
			
			date = dateFormat.parse(dateString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// IS IT AN UPDATE
		if (textLower.startsWith("update")
				|| textLower.startsWith("correction")) {
			trainStatus.setUpdate(true);
			text= text.replace("Update: ", "");
			text= text.replace("UPDATE: ", "");
			text= text.replace("Correction: ", "");
			text= text.replace("Update ", "");
		}
		textLower = text.toLowerCase();
		
		//SAVE OFF THE INFORMATION
		trainStatus.setMessage(text);
		trainStatus.setTime(date);
		trainStatus.setTimeLate(timeLate);
		trainStatus.setTrainNumber(trainNum);
		trainStatus.setStatusReason(reason);
		

		//SET THE DELAY REASON
		int index = textLower.indexOf("due to");
		if(index != -1 && text.length()!=0)
		{
			int minIndex = text.indexOf(".", index);
			int commaIndex = text.indexOf(",", index);
			int andIndex = text.indexOf(" and",index);

			if(minIndex==-1 || (commaIndex!=-1 && commaIndex < minIndex))
				minIndex = commaIndex;

			if(minIndex==-1 || (andIndex!=-1 && andIndex < minIndex))
				minIndex = andIndex;

			String delayReason = "";
			if(minIndex !=-1)
			{
				delayReason = text.substring(index, minIndex);
			}
			else
			{
				try{
					delayReason = text.substring(index, text.length()-1);
				}catch(Exception e){
					System.out.println("TEST");
				}
			}
			
			delayReason = delayReason.replace("due to ", "");
			delayReason = delayReason.replace("Due to ", "");
			trainStatus.setDelayReason(delayReason);


		}
		
		//SAVE OFF TRAIN
		return trainStatus;
	}
	
	private static int getDelay(String text)
	{
		ArrayList<Pattern> patterns = new ArrayList<Pattern>(5);
		patterns.add(Pattern.compile("(1)?[0-9]?[0-9]\""));
		patterns.add(Pattern.compile("(1)?[0-9]?[0-9] min"));
		patterns.add(Pattern.compile("(1)?[0-9]?[0-9]\'"));
		patterns.add(Pattern.compile("\\s(1)?[0-9]?[0-9]\\s"));
		patterns.add(Pattern.compile("-(1)?[0-9]?[0-9]"));
		patterns.add(Pattern.compile("\\s(1)?[0-9]?[0-9](.)\\slate"));
		patterns.add(Pattern.compile("\\s(1)?[0-9]?[0-9](.)\\smin"));
		patterns.add(Pattern.compile("\\s(1)?[0-9]?[0-9](.)late"));
		patterns.add(Pattern.compile("\\s(.)(1)?[0-9]?[0-9]\\slate"));
		patterns.add(Pattern.compile("(1)?[0-9]?[0-9]\\s\'"));
		
		Pattern numPattern = Pattern.compile("[0-9][0-9]?[0-9]?");
		
		for(Pattern pattern : patterns)
		{
			Matcher m = pattern.matcher(text.toLowerCase());
			if (m.find()) {
				String match = m.group(0);
				Matcher numMatcher = numPattern.matcher(match);
				numMatcher.find();
				return Integer.parseInt(numMatcher.group(0));
			} 
		}
		return -1;
	}

	public static List<TrainStatus> getAllNotificationsByStation(TrainStation trainStation, List<Train> currentTrains)
	{
		if(allTrainStatuses==null)
			updateOfficialNotifications();


		List<Train> upcomingTrains = new ArrayList<Train>();
		for(Train train : currentTrains)
		{
			if(train.isUpcoming(trainStation))
				upcomingTrains.add(train);
		}

		List<TrainStatus> trainStatusByStation = new ArrayList<TrainStatus>();
		for(int i = allTrainStatuses.size()-1 ; i >= 0 ; i--)
		{
			TrainStatus trainStatus = allTrainStatuses.get(i);
			boolean isMatchingTrain=false;
			for(Train train : upcomingTrains) {
				if (train.getTrainNumber() == trainStatus.getTrainNumber())
				{
					isMatchingTrain = true;
					break;
				}

			}
			if(isMatchingTrain || upcomingTrains.contains(trainStatus.getTrainNumber()) || trainStation.abbreviations.contains(trainStatus.getStation()) || trainStatus.getStatusReason()== TrainStatus.StatusReason.OVERALL_DELAYS || trainStatus.getStatusReason()== TrainStatus.StatusReason.BUS_BRIDGE)
			{
				trainStatusByStation.add(trainStatus);
			}


		}
		return trainStatusByStation;
	}

	public static List<TrainStatus> getAllNotificationsByTrain(Train train)
	{
		if(allTrainStatuses==null)
			updateOfficialNotifications();

		List<TrainStatus> trainStatusByTrain = new ArrayList<TrainStatus>();
		for(int i = allTrainStatuses.size()-1 ; i >= 0 ; i--)
		{
			TrainStatus trainStatus = allTrainStatuses.get(i);
			if(trainStatus.getTrainNumber()==train.getTrainNumber() || trainStatus.getStatusReason()== TrainStatus.StatusReason.OVERALL_DELAYS || trainStatus.getStatusReason()== TrainStatus.StatusReason.BUS_BRIDGE)
			{
				trainStatusByTrain.add(trainStatus);
			}
		}
		return trainStatusByTrain;
	}

	public static String getNotificationBigText(TrainStatus trainStatus) {
		return getDateString(trainStatus) + " MESSAGE: " + trainStatus.getMessage();
	}

	public static String getNotificationMainText(TrainStatus trainStatus, TrainSystem trainSystem) {
		String returnString="";
		String station="";
		switch(trainStatus.getStatusReason()){
			case OVERALL_DELAYS:
				String timeLate = getLateTime(trainStatus);
				if(timeLate!="")
					returnString += "LATE:" + timeLate;
				return returnString + getReasonString(trainStatus);
			case ON_THE_MOVE:
			case ON_TIME:
				station = getStationString(trainStatus, trainSystem);
				if(station!="")
					returnString += "STATION:" + station;
				return returnString;
			case BOARDING_PLATFORM:
				return "";
			default: //late, bus, annulled, stopped
				station = getStationString(trainStatus, trainSystem);
				if(station!="")
					returnString += "STATION:" + station;
				return returnString + getReasonString(trainStatus);
		}
	}

	public static String getDateString(TrainStatus trainStatus) {
		//FORMAT TIME
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
		try {

			return dateFormat.format(trainStatus.getTime()) + " ";
		} catch (Exception e) {
			return "";
		}
	}

	public static String getNotificationTitle(TrainStatus trainStatus) {
		switch(trainStatus.getStatusReason()){
			case OVERALL_DELAYS:
				return "Overall Train Service Delays";
			case BUS_BRIDGE:
				return "Bus Bridge";
			case ANNULLED:
				return getUpdateString(trainStatus) + "Train P" + trainStatus.getTrainNumber() + " is Annulled";
			case ON_THE_MOVE:
				return getUpdateString(trainStatus) + "Train P" + trainStatus.getTrainNumber() + " is on the move";
			case BOARDING_PLATFORM:
				return getUpdateString(trainStatus) + "Train P" + trainStatus.getTrainNumber() + " is boarding on opposite platform";
			case ON_TIME:
				return getUpdateString(trainStatus) + "Train P" + trainStatus.getTrainNumber() + " is on time";
			case CURRENTLY_STOPPED:
				return getUpdateString(trainStatus) + "Train P" + trainStatus.getTrainNumber() + " is currently Stopped";
			default:
				return getUpdateString(trainStatus) + "Train P" + trainStatus.getTrainNumber() + " is late" + getLateTime(trainStatus);
		}
	}

	public static String getStationString(TrainStatus trainStatus, TrainSystem trainSystem) {
		if(trainSystem ==null)
			return "";

		TrainStation trainStation = trainSystem.getStationByAbbreviation(trainStatus.getStation());
		if(trainStatus.getStation()!= null && trainStation != null)
			return trainStation.name;
		else
			return "";
	}

	public static String getLateTime(TrainStatus trainStatus) {

		if(trainStatus.getTimeLate() != -1)
			return " " + trainStatus.getTimeLate() + " min";
		else
			return "";
	}

	public static String getUpdateString(TrainStatus trainStatus)
	{
		if(trainStatus.isUpdate())
			return "UPDATE: ";
		else return "";
	}

	public static String getReasonString(TrainStatus trainStatus)
	{
		if(trainStatus.getDelayReason()!= null)
		{
			return " REASON: " + trainStatus.getDelayReason();
		}
		return "";
	}

	public static int getUpdateIcon(Context context, TrainStatus trainStatus)
	{
		switch(trainStatus.getStatusReason()){
			case OVERALL_DELAYS:
				return R.drawable.ic_public_black_18dp;
			case BUS_BRIDGE:
				return R.drawable.ic_directions_bus_black_18dp;
			case ANNULLED:
				return R.drawable.ic_close_black_18dp;
			case BOARDING_PLATFORM:
				return R.drawable.ic_swap_horiz_black_18dp;
			case ON_THE_MOVE:
			case ON_TIME:
				return R.drawable.ic_check_black_18dp;
			case CURRENTLY_STOPPED:
				return R.drawable.ic_report_black_18dp;
			default:
				return R.drawable.ic_access_time_black_18dp;
		}
	}

}
