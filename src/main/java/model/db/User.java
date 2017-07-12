package model.db;


import api.aws.DynamoDbClient;
import api.aws.DynamoDbStorable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class User implements Comparable<User>, DynamoDbStorable {

	protected int id;
	protected Number balanceLimit;
	protected String accessToken;
	protected String accessTokenExpiryTime;

	public static Factory factory = (Factory<User>) User::new;
	public static final String TABLE_NAME = "user";

	public User() {
	}

	public static User make() {
		User user = new User();
		DynamoDbClient.instance.putItem(TABLE_NAME, user);
		return user;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public Number getBalanceLimit() {
		return balanceLimit;
	}

	public void setBalanceLimit(Number balanceLimit) {
		this.balanceLimit = balanceLimit;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenExpiryTime() {
		return accessTokenExpiryTime;
	}

	public void setAccessTokenExpiryTime(String accessTokenExpiryTime) {
		this.accessTokenExpiryTime = accessTokenExpiryTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User)) {
			return false;
		}

		User tt = (User) obj;

		return this == tt ||
				(id == tt.id && Objects.equals(balanceLimit, tt.balanceLimit));
	}

	@Override
	public int compareTo(User o) {
		if (id > o.id) {
			return 1;
		}
		if (id < o.id) {
			return -1;
		}
		return 0;
	}

	@Override
	public Map<String, AttributeValue> getDynamoDbItem() {
		Map<String, AttributeValue> map = new TreeMap<>();

		map.put("id", new AttributeValue().withN(Integer.toString(this.id)));
		map.put("balanceLimit", new AttributeValue().withN(balanceLimit.toString()));
		map.put("accessToken", new AttributeValue().withS(accessToken));
		map.put("accessTokenExpiryTime", new AttributeValue().withS(accessTokenExpiryTime));

		return map;
	}

	@Override
	public Map<String, AttributeValue> getDynamoDbKey() {
		Map<String, AttributeValue> map = new TreeMap<>();
		map.put("id", new AttributeValue().withN(Integer.toString(this.id)));
		return map;
	}

	@Override
	public void setDynamoDbAttribute(String attributeName, AttributeValue attributeValue) {
		switch (attributeName) {
			case "id":
				this.id = Integer.parseInt(attributeValue.getN());
				break;
			case "balanceLimit":
				this.balanceLimit = Integer.parseInt(attributeValue.getN());
				break;
			case "accessToken":
				this.accessToken = attributeValue.getS();
				break;
			case "accessTokenExpiryTime":
				this.accessTokenExpiryTime = attributeValue.getS();
				break;
			default:
				throw new RuntimeException("Unknown attribute");
		}
	}

}
