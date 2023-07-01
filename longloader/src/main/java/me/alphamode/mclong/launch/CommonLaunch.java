package me.alphamode.mclong.launch;

import com.mojang.logging.LogUtils;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public abstract class CommonLaunch implements ILaunchHandlerService {
    public record LocatedPaths(List<Path> minecraftPaths, BiPredicate<String, String> minecraftFilter, List<List<Path>> otherModPaths, List<Path> otherArtifacts) {}

    protected static final Logger LOGGER = LogUtils.getLogger();

    public boolean isProduction() {
        return false;
    }

    public boolean isData() {
        return false;
    }

    public abstract LocatedPaths getMinecraftPaths();

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {

    }

    protected String[] preLaunch(String[] arguments, ModuleLayer layer) {
//        URI uri;
//        try (var reader = layer.configuration().findModule("longloader").orElseThrow().reference().open()) {
//            uri = reader.find("log4j2.xml").orElseThrow();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Force the log4j2 configuration to be loaded from fmlloader
//        Configurator.reconfigure(ConfigurationFactory.getInstance().getConfiguration(LoggerContext.getContext(), ConfigurationSource.fromUri(uri)));

        return arguments;
    }

    protected final Map<String, List<Path>> getModClasses() {
        final String modClasses = Optional.ofNullable(System.getenv("MOD_CLASSES")).orElse("");

        record ExplodedModPath(String modid, Path path) {}
        // "a/b/;c/d/;" -> "modid%%c:\fish\pepper;modid%%c:\fish2\pepper2\;modid2%%c:\fishy\bums;modid2%%c:\hmm"
        final var modClassPaths = Arrays.stream(modClasses.split(File.pathSeparator))
                .map(inp -> inp.split("%%", 2))
                .map(splitString -> new ExplodedModPath(splitString.length == 1 ? "defaultmodid" : splitString[0], Paths.get(splitString[splitString.length - 1])))
                .collect(Collectors.groupingBy(ExplodedModPath::modid, Collectors.mapping(ExplodedModPath::path, Collectors.toList())));

        //final var explodedTargets = ((Map<String, List<ExplodedDirectoryLocator.ExplodedMod>>)arguments).computeIfAbsent("explodedTargets", a -> new ArrayList<>());
        //modClassPaths.forEach((modlabel,paths) -> explodedTargets.add(new ExplodedDirectoryLocator.ExplodedMod(modlabel, paths)));
        return modClassPaths;
    }
}
