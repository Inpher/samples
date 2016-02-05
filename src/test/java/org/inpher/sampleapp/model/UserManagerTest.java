/*
 * Copyright 2015 Inpher, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.inpher.sampleapp.model;

import org.inpher.clientapi.InpherClient;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

/**
 * TODO: delete this class
 * Created by sduc on 2/3/16.
 */
public class UserManagerTest {

    private static final String dummyUsername = "user";
    private static final String dummyPassword = "password";

    @Test
    public void testLoginUsingNullUsernameOrPasswordShouldReturnFalse() throws Exception {

        InpherClient cli = mock(InpherClient.class);
        UserManager um = new UserManager(cli);

        assertFalse(um.login(null, dummyPassword));
        assertFalse(um.login(dummyUsername, null));

        assertFalse(um.register(null, dummyPassword));
        assertFalse(um.register(dummyUsername, null));
    }

    @Test
    public void testLoginUsingEmptyUserNameOrPasswordShouldReturnFalse() throws Exception {

        InpherClient cli = mock(InpherClient.class);
        UserManager um = new UserManager(cli);

        assertFalse(um.login("", dummyPassword));
        assertFalse(um.login(dummyUsername, ""));

        assertFalse(um.register("", dummyPassword));
        assertFalse(um.register(dummyUsername, ""));

    }


    @Test
    public void testValidLoginShouldCallInpherClientLoginUser() throws Exception {

        InpherClient cli = mock(InpherClient.class);
        UserManager um = new UserManager(cli);

        um.login(dummyUsername, dummyPassword);

        //verify(cli, times(1)).loginUser(new InpherUser(dummyUsername, dummyPassword));

    }

}