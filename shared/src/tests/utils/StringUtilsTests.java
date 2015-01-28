package tests.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import findfour.shared.utils.Reference;
import findfour.shared.utils.StringUtils;

public class StringUtilsTests {

    @Test
    public void testExtractCommand() {
        assertEquals(StringUtils.extractCommand("testing test test", ' '), "testing");
        assertEquals(StringUtils.extractCommand("test123", ' '), "test123");
        assertNotEquals(StringUtils.extractCommand("test test a", ' '), "te");
        assertNotEquals(StringUtils.extractCommand("testing", ' '), "te");
    }

    @Test
    public void testExtractArgs() {
        String[] a = StringUtils.extractArgs("testing test 123", ' ', false);
        String[] b = StringUtils.extractArgs("testing", ' ', false);
        String[] c = StringUtils.extractArgs("testing \"test 123\" testzzz", ' ', true);

        assertArrayEquals(a, new String[] {"test", "123"});
        assertArrayEquals(b, new String[] {});
        assertArrayEquals(c, new String[] {"test 123", "testzzz"});
    }

    @Test
    public void testTryParseInt() {
        Reference<Integer> result = new Reference<Integer>();

        assertFalse(StringUtils.tryParseInt("bad", result));
        assertTrue(StringUtils.tryParseInt("3", result));
        assertEquals(result.get().intValue(), 3);
    }

    @Test
    public void testParseInt() {
        assertEquals(StringUtils.parseInt("1"), 1);
        assertEquals(StringUtils.parseInt("001"), 1);
        assertEquals(StringUtils.parseInt("531"), 531);
        assertEquals(StringUtils.parseInt("-41"), -41);
    }

}
