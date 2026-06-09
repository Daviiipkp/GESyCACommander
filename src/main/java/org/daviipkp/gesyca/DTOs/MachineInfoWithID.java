package org.daviipkp.gesyca.DTOs;

public record MachineInfoWithID (
        String hostname,
        String ipAddress,
        int ID,
        String macAddress
    ) {}
