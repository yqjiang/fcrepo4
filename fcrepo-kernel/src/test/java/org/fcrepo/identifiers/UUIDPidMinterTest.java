package org.fcrepo.identifiers;

import org.junit.Test;

import java.util.UUID;

import static java.util.regex.Pattern.compile;
import static org.junit.Assert.assertTrue;

public class UUIDPidMinterTest {
    @Test
    public void testMintPid() throws Exception {

        PidMinter pidMinter = new UUIDPidMinter();

        String pid = pidMinter.mintPid();

        assertTrue("PID wasn't a UUID", compile(
                "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}").matcher(
                pid).find());

    }
}