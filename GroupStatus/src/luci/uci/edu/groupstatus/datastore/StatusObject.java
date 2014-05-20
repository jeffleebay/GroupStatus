package luci.uci.edu.groupstatus.datastore;

public class StatusObject {
	
	private Long id;
	private String userID;
	private String group;
	private String timestamp;
	private String status;
	private String groupStatus;
	private String wifiList;
	private String noiseLevel;
	private String location;
	private String address;
	
	public StatusObject() {
		super();
	}
	
	public StatusObject(String userID, String group, String timestamp, String status, 
			String groupStatus, String wifiList, String noiseLevel, String location, String address) {
		this.userID = userID;
		this.group = group;
		this.timestamp = timestamp;
		this.status = status;
		this.groupStatus = groupStatus;
		this.wifiList = wifiList;
		this.noiseLevel = noiseLevel;
		this.location = location;
		this.address = address;
	}

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getGroupStatus() {
		return groupStatus;
	}

	public void setGroupStatus(String groupStatus) {
		this.groupStatus = groupStatus;
	}	

	public String getWifiList() {
		return wifiList;
	}

	public void setWifiList(String wifiList) {
		this.wifiList = wifiList;
	}	

	public String getNoiseLevel() {
		return noiseLevel;
	}

	public void setNoiseLevel(String noiseLevel) {
		this.noiseLevel = noiseLevel;
	}	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}	
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}	

}
