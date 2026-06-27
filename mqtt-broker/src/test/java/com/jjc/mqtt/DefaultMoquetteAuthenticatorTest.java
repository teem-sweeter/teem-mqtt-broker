package com.jjc.mqtt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultMoquetteAuthenticatorTest {

    @SuppressWarnings("unchecked")
    private final ObjectProvider<ConnectedClients> connectedClientsProvider = mock(ObjectProvider.class);

    @SuppressWarnings("unchecked")
    private final ObjectProvider<com.jjc.mqtt.monitor.MonitorService> monitorServiceProvider = mock(ObjectProvider.class);

    @Test
    void checkValid_shouldAcceptValidCredentials() {
        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider
        );

        assertTrue(auth.checkValid("client-1", "admin", "pass123".getBytes()));
    }

    @Test
    void checkValid_shouldRejectInvalidCredentials() {
        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider
        );

        assertFalse(auth.checkValid("client-1", "admin", "wrong".getBytes()));
        assertFalse(auth.checkValid("client-1", "wrong", "pass123".getBytes()));
    }

    @Test
    void checkValid_shouldAllowAnonymousWhenEnabled() {
        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", true,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider
        );

        assertTrue(auth.checkValid("client-1", null, null));
        assertTrue(auth.checkValid("client-1", "", new byte[0]));
    }

    @Test
    void checkValid_shouldRejectAnonymousWhenDisabled() {
        DefaultMoquetteAuthenticator auth = new DefaultMoquetteAuthenticator(
                "admin", "pass123", false,
                DuplicateClientIdStrategy.REJECT_NEW,
                connectedClientsProvider, monitorServiceProvider
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
                connectedClientsProvider, monitorServiceProvider
        );

        assertFalse(auth.checkValid("client-1", "admin", "pass123".getBytes()));
    }
}
