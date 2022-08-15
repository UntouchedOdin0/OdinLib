package me.untouchedodin0.odinlib.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.untouchedodin0.odinlib.OdinLib;
import me.untouchedodin0.odinlib.task.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class SchematicUtils {

    private static EditSession editSession;

    public static void paste(Location location, File file) {
        if (!file.exists()) {
            OdinLib.getInstance().getLogger().warning("Schematic file doesn't exist: " + file.getName());
            return;
        }

        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(file);
        BlockVector3 vector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        Task.asyncDelayed(() -> {
            if (clipboardFormat != null) {
                try (ClipboardReader clipboardReader = clipboardFormat.getReader(new FileInputStream(file))) {
                    World world = BukkitAdapter.adapt(Objects.requireNonNull(location.getWorld()));
                    if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
                        editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build();
                    }

                    Clipboard clipboard = clipboardReader.read();
                    ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);

                    Operation operation = clipboardHolder
                            .createPaste(editSession)
                            .to(vector)
                            .ignoreAirBlocks(true)
                            .build();

                    try {
                        Operations.complete(operation);
                        editSession.close();
                    } catch (WorldEditException worldEditException) {
                        worldEditException.printStackTrace();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
