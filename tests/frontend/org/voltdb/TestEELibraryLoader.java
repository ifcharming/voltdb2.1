/* This file is part of VoltDB.
 * Copyright (C) 2008-2011 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.voltdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class TestEELibraryLoader
{
    @Test
    public void testLoader() {
        final VoltDB.Configuration configuration = new VoltDB.Configuration();
        configuration.m_noLoadLibVOLTDB = true;
        MockVoltDB mockvolt = new MockVoltDB();
        mockvolt.shouldIgnoreCrashes = true;
        VoltDB.replaceVoltDBInstanceForTest(mockvolt);
        mockvolt.m_noLoadLib = true;
        assertFalse(EELibraryLoader.loadExecutionEngineLibrary(false));
        assertEquals(0, mockvolt.getCrashCount());
        assertFalse(EELibraryLoader.loadExecutionEngineLibrary(true));
        assertEquals(1, mockvolt.getCrashCount());
        VoltDB.initialize(configuration);
        assertFalse(EELibraryLoader.loadExecutionEngineLibrary(true));
        assertEquals(1, mockvolt.getCrashCount());
    }
}
