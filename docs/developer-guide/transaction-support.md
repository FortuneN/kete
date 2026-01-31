# Transaction Support

## Overview

This provider implements **transaction-aware event processing**. Events are only published after Keycloak transactions commit successfully. Events are discarded if a transaction rolls back.

## Implementation

### EventListenerTransaction Pattern

Uses `org.keycloak.events.EventListenerTransaction` to queue events during transaction processing. The actual implementation splits responsibilities between `ProviderFactory` and `Provider`:

**ProviderFactory.java** (transaction creation and callbacks):
```java
// ProviderFactory implements Consumer<Event>, BiConsumer<AdminEvent, Boolean>
public class ProviderFactory implements EventListenerProviderFactory, 
        BiConsumer<AdminEvent, Boolean>, Consumer<Event> {

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        // Create transaction with this factory as the callback handler
        var transaction = new EventListenerTransaction(this, this);
        
        // Enlist in Keycloak's transaction manager
        session.getTransactionManager().enlistAfterCompletion(transaction);
        
        return new Provider(transaction);
    }

    @Override
    public void accept(Event event) {
        // Called on commit - publish event to destinations
        sendMessage(event.getId(), event.getRealmId(), ...);
    }

    @Override
    public void accept(AdminEvent event, Boolean includeRepresentation) {
        // Called on commit - publish admin event to destinations
        sendMessage(event.getId(), event.getRealmId(), ...);
    }
}
```

**Provider.java** (event queueing):
```java
public class Provider implements EventListenerProvider {

    private EventListenerTransaction transaction;

    @Override
    public void onEvent(Event event) {
        // Queue event - NOT published yet
        transaction.addEvent(event);
    }
}
```

### Transaction Lifecycle

1. **Event Received**: When Keycloak fires an event during request processing, `Provider.onEvent()` is called
2. **Event Queued**: The event is added to `EventListenerTransaction` queue using `transaction.addEvent()`
3. **Transaction Processing**: Keycloak continues processing the request
4. **Commit or Rollback**:
   - **If transaction commits**: `EventListenerTransaction` calls `ProviderFactory.accept(Event)` to publish events
   - **If transaction rolls back**: `EventListenerTransaction` clears the queue without calling callbacks
5. **Async Publishing**: After commit, events are published asynchronously to destinations

## Real-World Scenarios

### Scenario 1: User Registration Failure

```
1. User registration starts
2. Email verification event queued
3. Database constraint violation occurs
4. Transaction rolls back
5.  Email is NOT sent (event was never published)
```

### Scenario 2: Admin Action with Error

```
1. Admin deletes user account
2. User deletion event queued  
3. delete succeeds in database
4. Transaction commits successfully
5.  Event published to all destinations
6. Notifications sent, audit logs created
```

### Scenario 3: Multi-Step Operation Failure

```
1. User updates profile (event queued)
2. User uploads avatar (event queued)
3. Avatar upload fails
4. Transaction rolls back
5.  Profile update event NOT published
6.  Avatar upload event NOT published
7. External systems stay consistent with database
```

## Testing Transaction Behavior

To test transaction rollback behavior:

```java
@Test
public void shouldNotPublishEventWhenTransactionRollsBack() {
    // Simulate Keycloak session with transaction that will rollback
    KeycloakSession session = mock(KeycloakSession.class);
    KeycloakTransactionManager txManager = mock(KeycloakTransactionManager.class);
    when(session.getTransactionManager()).thenReturn(txManager);
    
    // Create provider and trigger event
    Provider provider = new Provider(..., session);
    Event event = createTestEvent();
    provider.onEvent(event);
    
    // Simulate transaction rollback
    capturedTransaction.rollbackImpl();
    
    // Verify event was NOT published to destinations
    verify(mockDestination, never()).sendEvent(...);
}
```

## Migration Notes

If upgrading from a previous version without transaction support:

1. **Behavior Change**: Events are now published AFTER transaction commit instead of immediately
2. **Timing**: Small delay between event occurrence and publishing (typically milliseconds)
3. **No Configuration Needed**: Transaction support is automatic
