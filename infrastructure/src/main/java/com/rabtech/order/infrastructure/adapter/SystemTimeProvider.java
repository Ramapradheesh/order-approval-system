
package com.rabtech.order.infrastructure.adapter;

import com.rabtech.order.application.port.TimeProvider;

import java.time.Instant;

public class SystemTimeProvider implements TimeProvider {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
