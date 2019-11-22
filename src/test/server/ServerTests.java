package test.server;

import common.data.Account;
import common.data.AccountBuilder;
import common.data.LoginUser;
import common.data.Message;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.DatabaseService;
import server.Server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

		when(this.dbServiceMock.getAccountByLoginUser(eq(loginUser))).thenReturn(account);

		exceptionRule.expect(Exception.class);
		exceptionRule.expectMessage("Invalid password");
		server.loginUser(loginUser);
	}

	@Test
	public void testLoginUnknownUsername() throws Exception {
		Server server = new Server(9999, this.dbServiceMock);

		LoginUser loginUser = new LoginUser("myUnknownName", "myPassword");

		when(this.dbServiceMock.getAccountByLoginUser(loginUser)).thenReturn(null);

		exceptionRule.expect(Exception.class);
		exceptionRule.expectMessage("Unknown username");
		server.loginUser(loginUser);
	}

	@Test
	public void testGetAccountAfterSuccessfullLogin() throws Exception {
		Server server = new Server(9999, this.dbServiceMock);

		LoginUser loginUser = new LoginUser("myName", "myPassword");

		Account account = new AccountBuilder().setAid(100).setUserName("myName").setPassword("myPassword").setStatus(0).setAboutMe("").setProfilePicture("").createAccount();

		when(this.dbServiceMock.getAccountByLoginUser(eq(loginUser))).thenReturn(account);
		Account resultAccount = server.loginUser(loginUser);
		assertEquals("myName", resultAccount.getUserName());
		assertEquals("myPassword", resultAccount.getPassword());
	}

	@Test
	public void testRegisterUser() throws Exception {
		Server server = new Server(9999, this.dbServiceMock);
		Account account = new AccountBuilder().setAid(100).setUserName("myName").setPassword("myPassword").setStatus(0).setAboutMe("").setProfilePicture("").createAccount();
		server.registerUser(account);
		verify(this.dbServiceMock, times(1)).insertAccount(eq(account));
	}

	@Test
	public void testSendMessageFailed() {
		Server server = new Server(9999, this.dbServiceMock);
		Message msg = new Message();
		assertFalse(server.sendMessage(msg));
	}
}
