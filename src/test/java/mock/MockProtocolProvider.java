/*
 * Jicofo, the Jitsi Conference Focus.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mock;

import mock.muc.*;
import mock.xmpp.*;
import mock.xmpp.colibri.*;
import mock.xmpp.pubsub.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import org.jitsi.protocol.xmpp.*;
import org.jitsi.protocol.xmpp.colibri.*;

/**
 *
 * @author Pawel Domas
 */
public class MockProtocolProvider
    extends AbstractProtocolProviderService
{
    /**
     * The logger.
     */
    private final static Logger logger
        = Logger.getLogger(MockProtocolProvider.class);

    private final MockAccountID accountId;

    private RegistrationState registrationState
        = RegistrationState.UNREGISTERED;

    private static MockXmppConnectionImpl sharedMockConnection
        = new MockXmppConnectionImpl();

    private AddressedXmppConnection connection;

    private MockJingleOpSetImpl jingleOpSet;

    public MockProtocolProvider(MockAccountID accountId)
    {
        this.accountId = accountId;
    }

    @Override
    public void register(SecurityAuthority authority)
        throws OperationFailedException
    {
        if (jingleOpSet != null)
        {
            jingleOpSet.start();
        }

        setRegistrationState(
            RegistrationState.REGISTERED,
            RegistrationStateChangeEvent.REASON_NOT_SPECIFIED,
            null);
    }

    private void setRegistrationState(RegistrationState newState,
                                      int reasonCode,
                                      String reason)
    {
        RegistrationState oldState = getRegistrationState();

        this.registrationState = newState;

        fireRegistrationStateChanged(
            oldState, newState, reasonCode, reason);
    }

    @Override
    public void unregister()
        throws OperationFailedException
    {
        if (jingleOpSet != null)
        {
            jingleOpSet.stop();
        }

        setRegistrationState(
            RegistrationState.UNREGISTERED,
            RegistrationStateChangeEvent.REASON_NOT_SPECIFIED,
            null);
    }

    @Override
    public RegistrationState getRegistrationState()
    {
        return registrationState;
    }

    @Override
    public String getProtocolName()
    {
        return accountId.getProtocolName();
    }

    @Override
    public ProtocolIcon getProtocolIcon()
    {
        return null;
    }

    @Override
    public void shutdown()
    {
        try
        {
            unregister();
        }
        catch (OperationFailedException e)
        {
            logger.error(e, e);
        }
    }

    @Override
    public AccountID getAccountID()
    {
        return accountId;
    }

    @Override
    public boolean isSignalingTransportSecure()
    {
        return false;
    }

    @Override
    public TransportProtocol getTransportProtocol()
    {
        return null;
    }


    public void includeBasicTeleOpSet()
    {
        addSupportedOperationSet(
            OperationSetBasicTelephony.class,
            new MockBasicTeleOpSet(this));
    }

    public void includeMultiUserChatOpSet()
    {
        addSupportedOperationSet(
            OperationSetMultiUserChat.class,
            new MockMultiUserChatOpSet(this));
    }

    public void includeColibriOpSet()
    {
        addSupportedOperationSet(
            OperationSetColibriConference.class,
            new MockColibriOpSet(this));
    }

    public void includeJingleOpSet()
    {
        this.jingleOpSet = new MockJingleOpSetImpl(this);

        addSupportedOperationSet(
            OperationSetJingle.class,
            jingleOpSet);
    }

    public void includeSimpleCapsOpSet()
    {
        addSupportedOperationSet(
            OperationSetSimpleCaps.class,
            new MockSetSimpleCapsOpSet(accountId.getServerAddress()));
    }

    public void includeDirectXmppOpSet()
    {
        addSupportedOperationSet(
            OperationSetDirectSmackXmpp.class,
            new MockSmackXmppOpSet(this));
    }

    public void includeJitsiMeetTools()
    {
        addSupportedOperationSet(
            OperationSetJitsiMeetTools.class,
            new MockJitsiMeetTools(this));
    }

    public void includeSubscriptionOpSet()
    {
        addSupportedOperationSet(
            OperationSetSubscription.class,
            new MockSubscriptionOpSetImpl());
    }

    public OperationSetBasicTelephony getTelephony()
    {
        return getOperationSet(OperationSetBasicTelephony.class);
    }

    public MockXmppConnection getMockXmppConnection()
    {
        if (this.connection == null)
        {
            this.connection
                = new AddressedXmppConnection(
                        getOurJID(), sharedMockConnection);
        }
        return connection;
    }

    public String getOurJID()
    {
        return "mock-" + accountId.getAccountAddress();
    }

    public MockMultiUserChatOpSet getMockChatOpSet()
    {
        return (MockMultiUserChatOpSet)
            getOperationSet(OperationSetMultiUserChat.class);
    }

    public MockSubscriptionOpSetImpl getMockSubscriptionOpSet()
    {
        return (MockSubscriptionOpSetImpl)
            getOperationSet(OperationSetSubscription.class);
    }

    public MockSetSimpleCapsOpSet getMockCapsOpSet()
    {
        return (MockSetSimpleCapsOpSet)
            getOperationSet(OperationSetSimpleCaps.class);
    }

    public MockColibriOpSet getMockColibriOpSet()
    {
        return (MockColibriOpSet)
            getOperationSet(OperationSetColibriConference.class);
    }
}
