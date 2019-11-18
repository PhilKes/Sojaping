package test;

import common.data.Account;
import common.data.AccountBuilder;
import common.data.LoginUser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.DatabaseService;
import server.Server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ServerTests {

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Mock
	private DatabaseService dbServiceMock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testLoginInvalidPassword() throws Exception {
		Server server = new Server(9999, this.dbServiceMock);

		LoginUser loginUser = new LoginUser("myName", "myInavlidPassword");

		Account account = new AccountBuilder().setAid(100).setUserName("myName").setPassword("myPassword").setStatus(0).setAboutMe("").setProfilePicture("").createAccount();

		when(this.dbServiceMock.getAccountByLoginUser(any())).thenReturn(account);

		exceptionRule.expect(Exception.class);
		exceptionRule.expectMessage("Invalid password");
		server.loginUser(loginUser);
	}
}
