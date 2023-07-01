package me.alphamode.mclong.launch;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibraryFinder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Path libsPath;
    static Path findLibsPath() {
        if (libsPath == null) {
            libsPath = Path.of(System.getProperty("libraryDirectory","crazysnowmannonsense/cheezwhizz"));
            if (!Files.isDirectory(libsPath)) {
                throw new IllegalStateException("Missing libraryDirectory system property, cannot continue");
            }
        }
        return libsPath;
    }

    static Path getForgeLibraryPath(final String mcVersion, final String forgeVersion, final String forgeGroup) {
        Path forgePath = findLibsPath().resolve(get(forgeGroup, "mclong", "", "universal", mcVersion+"-"+forgeVersion));
        LOGGER.debug("Found forge path {} is {}", forgePath, pathStatus(forgePath));
        return forgePath;
    }

    static String pathStatus(final Path path) {
        return Files.exists(path) ? "present" : "missing";
    }

    static Path[] getMCPaths(final String mcVersion, final String mcpVersion, final String forgeVersion, final String forgeGroup, final String type) {
        Path srgMcPath = findLibsPath().resolve(get("net.minecraft", type, "", "srg", mcVersion+"-"+mcpVersion));
        Path mcExtrasPath = findLibsPath().resolve(get("net.minecraft", type, "", "extra", mcVersion+"-"+mcpVersion));
        Path patchedBinariesPath = findLibsPath().resolve(get(forgeGroup, "mclong", "", type, mcVersion+"-"+forgeVersion));
        LOGGER.debug("SRG MC at {} is {}", srgMcPath.toString(), pathStatus(srgMcPath));
        LOGGER.debug("MC Extras at {} is {}", mcExtrasPath.toString(), pathStatus(mcExtrasPath));
        LOGGER.debug("Forge patches at {} is {}", patchedBinariesPath.toString(), pathStatus(patchedBinariesPath));
        return new Path[] { patchedBinariesPath, mcExtrasPath, srgMcPath };
    }

    public static Path findPathForMaven(final String group, final String artifact, final String extension, final String classifier, final String version) {
        return findLibsPath().resolve(get(group, artifact, extension, classifier, version));
    }
    public static Path findPathForMaven(final String maven) {
        return findLibsPath().resolve(get(maven));
    }

    public static Path get(final String coordinate) {
        final String[] parts = coordinate.split(":");
        final String groupId = parts[0];
        final String artifactId = parts[1];
        final String classifier = parts.length > 3 ? parts[2] : "";
        final String[] versext = parts[parts.length-1].split("@");
        final String version = versext[0];
        final String extension = versext.length > 1 ? versext[1] : "";
        return get(groupId, artifactId, extension, classifier, version);
    }

    public static Path get(final String groupId, final String artifactId, final String extension, final String classifier, final String version)
    {
        final String fileName = artifactId + "-" + version +
                (!classifier.isEmpty() ? "-"+ classifier : "") +
                (!extension.isEmpty() ? "." + extension : ".jar");

        String[] groups = groupId.split("\\.");
        Path result = Paths.get(groups[0]);
        for (int i = 1; i < groups.length; i++) {
            result = result.resolve(groups[i]);
        }

        return result.resolve(artifactId).resolve(version).resolve(fileName);
    }
}
