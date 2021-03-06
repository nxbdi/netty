/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.testsuite.transport.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.logging.InternalLogger;
import io.netty.logging.InternalLoggerFactory;
import io.netty.testsuite.transport.socket.SocketTestPermutation.Factory;
import io.netty.testsuite.util.TestUtils;
import io.netty.util.NetworkConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class AbstractSocketTest {

    private static final List<Entry<Factory<ServerBootstrap>, Factory<Bootstrap>>> COMBO =
            SocketTestPermutation.socket();

    @Rule
    public final TestName testName = new TestName();

    protected final InternalLogger logger = InternalLoggerFactory.getInstance(getClass());

    protected volatile ServerBootstrap sb;
    protected volatile Bootstrap cb;
    protected volatile InetSocketAddress addr;
    protected volatile Factory<Bootstrap> currentBootstrap;
    
    protected void run() throws Throwable {
        int i = 0;
        for (Entry<Factory<ServerBootstrap>, Factory<Bootstrap>> e: COMBO) {
            currentBootstrap = e.getValue();
            sb = e.getKey().newInstance();
            cb = e.getValue().newInstance();
            addr = new InetSocketAddress(
                    NetworkConstants.LOCALHOST, TestUtils.getFreePort());
            sb.localAddress(addr);
            cb.remoteAddress(addr);

            logger.info(String.format(
                    "Running: %s %d of %d", testName.getMethodName(), ++ i, COMBO.size()));
            try {
                Method m = getClass().getDeclaredMethod(
                        testName.getMethodName(), ServerBootstrap.class, Bootstrap.class);
                m.invoke(this, sb, cb);
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            } finally {
                sb.shutdown();
                cb.shutdown();
            }
        }
    }
}
