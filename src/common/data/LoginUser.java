package common.data;

/** Temporarily used object for login authentication */
public class LoginUser {

	private String userName;
	private String password;

	public LoginUser(){}
	public LoginUser(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "LoginUser{" +
				"userName='" + userName + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
