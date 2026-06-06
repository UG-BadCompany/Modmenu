package com.badcompany.modmenu.commands;

import java.util.List;

public interface Command {
    String name();
    String description();
    String usage();
    void execute(List<String> args);
}
