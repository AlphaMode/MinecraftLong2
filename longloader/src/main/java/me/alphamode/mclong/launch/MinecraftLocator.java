package me.alphamode.mclong.launch;

import com.google.common.collect.ImmutableList;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MinecraftLocator implements ITransformationService {

    private CommonLaunch service;

    @Override
    public @NotNull String name() {
        return "minecraft";
    }

    @Override
    public void initialize(IEnvironment environment) {
        final String launchTarget = environment.getProperty(IEnvironment.Keys.LAUNCHTARGET.get()).orElse("MISSING");
        final Optional<ILaunchHandlerService> launchHandler = environment.findLaunchHandler(launchTarget);
        service = (CommonLaunch) launchHandler.orElseThrow();
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException {

    }

    @Override
    public List<Resource> completeScan(IModuleLayerManager layerManager) {
        MinecraftLongLaunchServiceDev.LocatedPaths mc = service.getMinecraftPaths();
        ImmutableList.Builder<SecureJar> mods = new ImmutableList.Builder<>();
        mods.add(SecureJar.from(mc.minecraftFilter(), mc.minecraftPaths().toArray(Path[]::new)));
        if (service instanceof MinecraftLongLaunchService) {
            mc.otherModPaths().forEach(paths -> {
                mods.add(SecureJar.from(mc.minecraftFilter(), paths.toArray(Path[]::new)));
            });
        }
        return List.of(new Resource(IModuleLayerManager.Layer.GAME, mods.build()));
    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of();
    }
}
