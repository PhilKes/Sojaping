package test.server;

import common.data.Account;
import common.data.AccountBuilder;
import common.data.Message;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.Connection;
import server.Server;
import server.ServerHandler;

import static common.Constants.Contexts.FAIL;
import static common.Constants.Contexts.LOGIN_SUCCESS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ServerHandlerTests {

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Mock
	private Server serverMock;

	@Mock
	private Connection connectionMock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInvalidPacket() {
		String json = null;
		ServerHandler s = new ServerHandler(this.serverMock, null, json);
		s.run();

		verify(this.serverMock, times(1)).sendToUser(any(), eq(FAIL), eq("Invalid JSON received!"));
	}

	@Test
	public void testLoginPackage() throws Exception {
		String json = "{\"method\":\"login\",\"data\":{\"userName\":\"aaa\",\"password\":\"abc\",\"_class\":\"common.data.LoginUser\"}}";
		Account account = new AccountBuilder().setAid(100).setUserName("myName").setPassword("myPassword").setStatus(0).setAboutMe("").setProfilePicture("").createAccount();

		when(this.serverMock.loginUser(any())).thenReturn(account);

		ServerHandler s = new ServerHandler(this.serverMock, this.connectionMock, json);
		s.run();

		verify(this.serverMock, times(1)).sendToUser(eq(this.connectionMock), eq(LOGIN_SUCCESS), eq(account));
        verify(this.serverMock, times(1)).setConnectionAccount(eq(this.connectionMock), eq(account));
	}

	@Test
	public void testFailedPackage() throws Exception {
		String json = "{\"method\":\"fail\",\"data\":{\"userName\":\"aaa\",\"password\":\"abc\",\"_class\":\"common.data.LoginUser\"}}";
		Account account = new AccountBuilder().setAid(100).setUserName("myName").setPassword("myPassword").setStatus(0).setAboutMe("").setProfilePicture("").createAccount();

		when(this.serverMock.loginUser(any())).thenReturn(account);

		ServerHandler s = new ServerHandler(this.serverMock, this.connectionMock, json);
		s.run();

		verify(this.serverMock, times(1)).sendToUser(any(), contains("fail"), isA(Exception.class));
	}

	@Test
	public void testMessageBroadcastPackage() {
		String json = "{\"method\":\"messageSent\",\"data\":{\"translate\":false,\"text\":\"my message hi\\n\",\"timestamp\":1574255059370,\"sender\":\"aaa\",\"receiver\":null,\"_class\":\"common.data.Message\"}}";
		ServerHandler s = new ServerHandler(this.serverMock, this.connectionMock, json);
		s.run();
		verify(this.serverMock, times(1)).broadcastMessages(isA(Message.class));
	}
}