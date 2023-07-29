package me.alphamode.mclong.launch;

import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import cpw.mods.modlauncher.api.ServiceRunner;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class MinecraftLongLaunchService extends CommonLaunch {
    public static final String VERSION = "1.20.1-0.0.0-1.20.1-big";
    public static final String VERSION_MCP = "1.20.1-20230612.114412";
    @Override
    public String name() {
        return "minecraftlong";
    }

    @Override
    public LocatedPaths getMinecraftPaths() {
        var mc = LibraryFinder.findPathForMaven("net.minecraft", "client", "", "srg", VERSION_MCP);
        var mcextra = LibraryFinder.findPathForMaven("net.minecraft", "client", "", "extra", VERSION_MCP);
        var mcstream = Stream.<Path>builder().add(mc).add(mcextra);
        var modstream = Stream.<List<Path>>builder();

        processMCStream(mcstream, modstream);

        return new LocatedPaths(mcstream.build().toList(), (a,b) -> true, modstream.build().toList(), List.of());
    }

    @Override
    public void configureTransformationClassLoader(ITransformingClassLoaderBuilder builder) {

    }

    @Override
    public ServiceRunner launchService(String[] arguments, ModuleLayer layer) {
        return () -> {
            var args = preLaunch(arguments, layer);

            Class.forName(layer.findModule("client").orElseThrow(),"net.minecraft.client.main.Main").getMethod("main", String[].class).invoke(null, (Object)args);
        };
    }

    protected void processMCStream(Stream.Builder<Path> mc, Stream.Builder<List<Path>> mods) {
        var forgepatches = LibraryFinder.findPathForMaven("net.minecraftforge", "mclong", "", "client", VERSION);
        var forgejar = LibraryFinder.findPathForMaven("net.minecraftforge", "mclong", "", "universal", VERSION);
        mc.add(forgepatches);
        mods.add(List.of(forgejar));
    }
}
