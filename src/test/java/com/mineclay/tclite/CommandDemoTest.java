package com.mineclay.tclite;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
public class CommandDemoTest {

    @Mock
    CommandSender sender;
    @Mock
    PluginCommand cmd;
    CommandDemo command = new CommandDemo();

    void call(String commandLine) {
        String[] s = commandLine.split(" ");
        String label = s[0];
        String[] args = Arrays.copyOfRange(s, 1, s.length);

        command.onCommand(sender, cmd, label, args);
    }

    void complete(String commandLine) {
        String[] s = commandLine.split(" ", -1);
        String label = s[0];
        String[] args = Arrays.copyOfRange(s, 1, s.length);

        List<String> strings = command.onTabComplete(sender, cmd, label, args);
        System.out.println("strings = " + strings);
    }

    @Test
    public void test() {

        MockitoAnnotations.openMocks(this);

        lenient().when(cmd.getPlugin()).thenReturn(null);

        lenient().when(sender.getName()).thenReturn("TestPlayer");
        lenient().doAnswer(invocation -> {
            System.out.println((String) invocation.getArgument(0));
            return null;
        }).when(sender).sendMessage(anyString());
        lenient().when(sender.hasPermission(anyString())).thenReturn(true);

        call("demo parser-test https://www.google.com");

        call("demo");
        call("demo subCommand");
        call("demo 2");

        complete("demo su");
        complete("demo subCommand ");
        complete("demo subCommand 1 ");
        complete("demo subCommand 1 a");
    }
}
