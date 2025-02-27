package com.mineclay.tclite.command;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public interface ArgParser<T> {

    ArgParser<String> STRING = new ArgParser<String>() {
        @Override
        public @Nullable String getName() {
            return "string";
        }

        @Override
        public @NotNull String parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return arg;
        }

        @Override
        public @NotNull String asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return arg;
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return Collections.emptyList();
        }

        @Override
        public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return Collections.emptyList();
        }
    };

    ArgParser<Integer> INTEGER = new ArgParser<Integer>() {
        @Override
        public @Nullable String getName() {
            return "integer";
        }

        @Override
        public @NotNull Integer parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                return error("Invalid integer");
            }
        }

        @Override
        public @NotNull Integer asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return parse(ctx, arg);
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return Collections.emptyList();
        }

        @Override
        public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return Collections.emptyList();
        }
    };

    ArgParser<Double> DOUBLE = new ArgParser<Double>() {
        @Override
        public @Nullable String getName() {
            return "double";
        }

        @Override
        public @NotNull Double parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            try {
                return Double.parseDouble(arg);
            } catch (NumberFormatException e) {
                return error("Invalid double");
            }
        }

        @Override
        public @NotNull Double asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return parse(ctx, arg);
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return Collections.emptyList();
        }

        @Override
        public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return Collections.emptyList();
        }
    };

    ArgParser<Boolean> BOOLEAN = new ArgParser<Boolean>() {
        @Override
        public @Nullable String getName() {
            return "ture/false";
        }

        @Override
        public @NotNull Boolean parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false")) {
                return Boolean.parseBoolean(arg);
            } else {
                return error("Invalid boolean");
            }
        }

        @Override
        public @NotNull Boolean asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return parse(ctx, arg);
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            List<String> candidates = new ArrayList<>();
            candidates.add("true");
            candidates.add("false");
            String pre = arg.toLowerCase(Locale.ROOT);
            return candidates.stream().filter(c -> c.toLowerCase(Locale.ROOT).startsWith(pre)).collect(Collectors.toList());
        }

        @Override
        public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return complete(ctx, arg);
        }
    };

    ArgParser<Player> ONLINE_PLAYER = new ArgParser<Player>() {
        @Override
        public @Nullable String getName() {
            return "player";
        }

        @Override
        public @NotNull Player parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            Player match = Bukkit.getPlayerExact(arg);
            if (match == null) {
                return error("Player not found");
            } else {
                return match;
            }
        }

        @Override
        public @NotNull Player asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return parse(ctx, arg);
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            String pre = arg.toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(pre)).collect(Collectors.toList());
        }

        @Override
        public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return complete(ctx, arg);
        }
    };

    ArgParser<String> OFFLINE_PLAYER = new ArgParser<String>() {
        @Override
        public @Nullable String getName() {
            return "player";
        }

        @Override
        public @NotNull String parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return arg;
        }

        @Override
        public @NotNull String asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return arg;
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            String pre = arg.toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(pre)).collect(Collectors.toList());
        }

        @Override
        public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return complete(ctx, arg);
        }
    };

    ArgParser<Material> MATERIAL = new ArgParser<Material>() {
        @Override
        public @Nullable String getName() {
            return "material";
        }

        @Override
        public @NotNull Material parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            Material match = Material.matchMaterial(arg);
            if (match == null) {
                return error("Material not found");
            } else {
                return match;
            }
        }

        @Override
        public @NotNull Material asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return parse(ctx, arg);
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            String pre = arg.toLowerCase(Locale.ROOT);
            return Arrays.stream(Material.values()).map(Material::name).filter(m -> m.toLowerCase(Locale.ROOT).startsWith(pre)).collect(Collectors.toList());
        }

        @Override
        public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return complete(ctx, arg);
        }
    };

    ArgParser<Enchantment> ENCHANTMENT = new ArgParser<Enchantment>() {
        @Override
        public @Nullable String getName() {
            return "enchantment";
        }

        @Override
        public @NotNull Enchantment parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            Enchantment match = Enchantment.getByName(arg);
            if (match == null) {
                return error("Enchantment not found");
            } else {
                return match;
            }
        }

        @Override
        public @NotNull Enchantment asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return parse(ctx, arg);
        }

        @Override
        public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            String pre = arg.toLowerCase(Locale.ROOT);
            return Arrays.stream(Enchantment.values()).map(Enchantment::getName).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(pre)).collect(Collectors.toList());
        }

        @Override
        public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
            return complete(ctx, arg);
        }
    };

    static <T extends Enum<T>> @NotNull ArgParser<T> parseEnum(Class<T> clazz) {
        String simpleName = clazz.getSimpleName();
        return new ArgParser<T>() {
            @Override
            public @Nullable String getName() {
                return Joiner.on("|").join(clazz.getEnumConstants());
            }

            @Override
            public @NotNull T parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
                try {
                    return Enum.valueOf(clazz, arg);
                } catch (IllegalArgumentException e) {
                    return error("Invalid " + simpleName);
                }
            }

            @Override
            public @NotNull T asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
                return parse(ctx, arg);
            }

            @Override
            public @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
                String pre = arg.toLowerCase(Locale.ROOT);
                return Arrays.stream(clazz.getEnumConstants()).map(Enum::name).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(pre)).collect(Collectors.toList());
            }

            @Override
            public @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
                return complete(ctx, arg);
            }
        };
    }

    @Nullable String getName();

    @NotNull T parse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal;

    /**
     * @param ctx ctx
     * @param arg arg
     * @return parsed value
     * @throws CommandSignal if not supported, CommandSignal.NOT_IMPLEMENTED
     */
    default @NotNull T asyncParse(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
        throw CommandSignal.NOT_IMPLEMENTED;
    }

    @NotNull Iterable<String> complete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal;

    /**
     * <p>implement this method to support async completion</p>
     * <p><b>CAUTION</b></p>
     * <p>all of the {@link ArgParser} prior to this should implement {@link #asyncParse(CommandContext, String)} to make async completion works</p>
     *
     * @param ctx ctx
     * @param arg arg
     * @return complete result
     * @throws CommandSignal if not supported, CommandSignal.NOT_IMPLEMENTED
     */
    default @NotNull Iterable<String> asyncComplete(@NotNull CommandContext ctx, @NotNull String arg) throws CommandSignal {
        throw CommandSignal.NOT_IMPLEMENTED;
    }

    default T error(String error) throws CommandSignal {
        throw new CommandSignal(error);
    }
}
