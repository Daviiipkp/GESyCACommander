package org.daviipkp.gesyca.DTOs;

import java.util.List;

public record CommandInfo (
        String cmd,
        List<Integer> machines,
        String delay,
        String pass
    ) {}
