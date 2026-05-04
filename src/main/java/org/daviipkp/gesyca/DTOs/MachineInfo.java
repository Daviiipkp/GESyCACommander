package org.daviipkp.gesyca.DTOs;

public record MachineInfo(
        String hostname,
        String ipAddress,
        int ID,
        String macAddress
    ) {}
