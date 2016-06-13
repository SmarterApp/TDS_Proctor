package TDS.Proctor.performance.utils;

import org.junit.Assert;
import org.junit.Test;

public class TdsRouterHelperTest {

    @Test
    public void testPrefixWithZone() throws Exception {
        Assert.assertEquals("Alu-", TdsRouterHelper.createRoutePrefix("Plu-", "A"));
    }

    @Test
    public void testNullZone() throws Exception {
        Assert.assertEquals("Plu-", TdsRouterHelper.createRoutePrefix("Plu-", null));
    }

    @Test
    public void testEmptyZone() throws Exception {
        Assert.assertEquals("Plu-", TdsRouterHelper.createRoutePrefix("Plu-", ""));
    }

    @Test
    public void testNullPrefix() throws Exception {
        Assert.assertEquals(null, TdsRouterHelper.createRoutePrefix(null, "B"));
    }

    @Test
    public void testEmptyPrefix() throws Exception {
        Assert.assertEquals("", TdsRouterHelper.createRoutePrefix("", "B"));
    }

    @Test
    public void testLongZone() throws Exception {
        Assert.assertEquals("Fred-", TdsRouterHelper.createRoutePrefix("Fred-", "BAD"));
    }

    @Test
    public void testShortPrefix() throws Exception {
        Assert.assertEquals("Cpl-", TdsRouterHelper.createRoutePrefix("Pl-", "C"));
        Assert.assertEquals("Cs-", TdsRouterHelper.createRoutePrefix("S-", "C"));
        Assert.assertEquals("C-", TdsRouterHelper.createRoutePrefix("-", "C"));
        Assert.assertEquals("Cs", TdsRouterHelper.createRoutePrefix("S", "C"));
    }

}