package net.permutated.pylons.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import net.permutated.pylons.ModRegistry;

public class InterdictionPylonContainer extends AbstractPylonContainer {

    public InterdictionPylonContainer(int windowId, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super(ModRegistry.INTERDICTION_PYLON_CONTAINER.get(), windowId, playerInventory, packetBuffer);
    }

    @Override
    protected RegistryObject<Block> getBlock() {
        return ModRegistry.INTERDICTION_PYLON;
    }

    @Override
    public boolean shouldRenderRange() {
        return true;
    }
}
