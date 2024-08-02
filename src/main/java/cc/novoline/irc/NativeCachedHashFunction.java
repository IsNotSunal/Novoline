package cc.novoline.irc;

import cc.novoline.Novoline;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.skidunion.irc.util.DigestUtils;
import net.skidunion.irc.util.IHashFunction;
import net.skidunion.security.annotations.Protect;
import org.jetbrains.annotations.NotNull;

public class NativeCachedHashFunction implements IHashFunction {

    private final NativeHashFunction hashFunction;
    private final Object2ObjectArrayMap<String, String> cache;

    public NativeCachedHashFunction() {
        hashFunction = new NativeHashFunction();
        cache = new Object2ObjectArrayMap<>();
    }

    @NotNull
    @Override
    public String hash(@NotNull String s) {
        return cache.computeIfAbsent(s, input -> {
            String hash = hashFunction.hash(input);
            cache.put(s, input);
            return hash;
        });
    }

    @Protect.Virtualize
    private static class NativeHashFunction {
        public String hash(String input) {
            if(Novoline.getInstance() == null
                    || Novoline.getInstance().getProtection() == null
                    || !Novoline.getInstance().getProtection().isPassed()) {
                return null;
            }

            return DigestUtils.INSTANCE.sha256Hex(Integer.toBinaryString(input.hashCode()));
        }
    }
}
