package test;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import server.Server;
import server.ServerHandler;

import static common.Constants.Contexts.FAIL;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ServerHandlerTests {

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Mock
	private Server serverMock;

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
}
