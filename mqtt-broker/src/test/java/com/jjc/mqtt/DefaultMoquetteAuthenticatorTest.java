package com.jjc.mqtt;

import io.moquette.broker.subscriptions.Topic;
import com.jjc.mqtt.monitor.ClientControlProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultMoquetteAuthenticatorTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<ConnectedClients> connectedClientsProvider = mock(ObjectProvider.class);

    @SuppressWarnings("unchecked")
    private final ObjectProvider<com.jjc.mqtt.monitor.MonitorService> monitorServiceProvider = mock(ObjectProvider.class);

    @SuppressWarnings("unchecked")
    private final ObjectProvider<com.jjc.mqtt.monitor.ClientControlProvider> clientControlProvider = mock(ObjectProvider.class);

    @Test
    void checkValid_shouldAcceptValidCredentials() {
        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider, clientControlProvider
        );

        assertTrue(auth.checkValid("client-1", "admin", "pass123".getBytes()));
    }

    @Test
    void checkValid_shouldRejectInvalidCredentials() {
        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider, clientControlProvider
        );

        assertFalse(auth.checkValid("client-1", "admin", "wrong".getBytes()));
        assertFalse(auth.checkValid("client-1", "wrong", "pass123".getBytes()));
    }

    @Test
    void checkValid_shouldAllowAnonymousWhenEnabled() {
        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", true,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider, clientControlProvider
        );

        assertTrue(auth.checkValid("client-1", null, null));
        assertTrue(auth.checkValid("client-1", "", new byte[0]));
    }

    @Test
    void checkValid_shouldRejectAnonymousWhenDisabled() {
        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider, clientControlProvider
        );

        assertFalse(auth.checkValid("client-1", null, null));
    }

    @Test
    void checkValid_shouldRejectDuplicateClientIdWhenStrategyIsReject() {
        ConnectedClients connectedClients = mock(ConnectedClients.class);
        when(connectedClientsProvider.getIfAvailable()).thenReturn(connectedClients);
        when(connectedClients.contains("client-1")).thenReturn(true);

        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider, clientControlProvider
        );

        assertFalse(auth.checkValid("client-1", "admin", "pass123".getBytes()));
    }

    @Test
    void canWrite_shouldRestrictPublishWhenSendDisabled() {
        ClientControlProvider ccp = mock(ClientControlProvider.class);
        when(clientControlProvider.getIfAvailable()).thenReturn(ccp);
        when(ccp.isSendDisabled("test-client")).thenReturn(true);
        when(ccp.isSendDisabled("other-client")).thenReturn(false);

        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider, clientControlProvider
        );

        Topic topic = Topic.asTopic("test/topic");
        assertFalse(auth.canWrite(topic, "admin", "test-client"));
        assertTrue(auth.canWrite(topic, "admin", "other-client"));
    }

    @Test
    void canRead_shouldAllowSubscribeEvenWhenReceiveDisabled() {
        ClientControlProvider ccp = mock(ClientControlProvider.class);
        when(clientControlProvider.getIfAvailable()).thenReturn(ccp);
        when(ccp.isReceiveDisabled("test-client")).thenReturn(true);
        when(ccp.isReceiveDisabled("other-client")).thenReturn(false);

        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider, clientControlProvider
        );

        Topic topic = Topic.asTopic("test/topic");
        assertTrue(auth.canRead(topic, "admin", "test-client"));
        assertTrue(auth.canRead(topic, "admin", "other-client"));
    }

}
