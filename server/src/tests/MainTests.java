package tests;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import server.Main;

public class MainTests {

    @Test
    public void testConstructor() {
        assertNotNull(Main.INSTANCE);
    }

    @Test
    public void testGetPlayerManager() {
        assertNotNull(Main.INSTANCE.getPlayerManager());
    }

    @Test
    public void testGetRoomManager() {
        assertNotNull(Main.INSTANCE.getRoomManager());
    }

    @Test
    public void testGetMatchMaker() {
        assertNotNull(Main.INSTANCE.getMatchMaker());
    }

    @Test
    public void testGetChallenger() {
        assertNotNull(Main.INSTANCE.getChallenger());
    }

    @Test
    public void testGetServer() {
        assertNotNull(Main.INSTANCE.getServer());
    }
}
