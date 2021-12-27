package net.permutated.pylons.inventory.container;

import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.RegistryObject;
import net.permutated.pylons.network.NetworkDispatcher;
import net.permutated.pylons.network.PacketButtonClicked;
import net.permutated.pylons.tile.AbstractPylonTile;

import javax.annotation.Nullable;

public abstract class AbstractPylonContainer extends AbstractContainerMenu {

    @Nullable // should only be accessed from server
    private final AbstractPylonTile tileEntity;
    protected final String ownerName;
    protected final BlockPos blockPos;

    protected AbstractPylonContainer(@Nullable MenuType<?> containerType, int windowId, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super(containerType, windowId);

        blockPos = packetBuffer.readBlockPos();
        Level world = playerInventory.player.getCommandSenderWorld();

        tileEntity = (AbstractPylonTile) world.getBlockEntity(blockPos);
        IItemHandler wrappedInventory = new InvWrapper(playerInventory);

        if (tileEntity != null) {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                for (int slot = 0; slot < AbstractPylonTile.SLOTS; slot++) {
                    addSlot(new SlotItemHandler(handler, slot, 8 + slot * 18, 48));
                }
            });
        }

        int nameLength = packetBuffer.readInt();
        ownerName = packetBuffer.readUtf(nameLength);

        registerPlayerSlots(wrappedInventory);
    }

    protected abstract RegistryObject<Block> getBlock();

    protected int getInventorySize() {
        return AbstractPylonTile.SLOTS;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean shouldRenderRange() {
        return false;
    }

    public Component getRangeComponent() {
        var range = tileEntity != null ? tileEntity.getSelectedRange() : 0;
        return new TextComponent(String.format("%dx%d", range, range));
    }

    public void sendRangePacket(Button button) {
        NetworkDispatcher.INSTANCE.sendToServer(new PacketButtonClicked(blockPos));
    }

    @Override
    public boolean stillValid(Player playerEntity) {
        if (tileEntity != null) {
            Level world = tileEntity.getLevel();
            if (world != null) {
                ContainerLevelAccess callable = ContainerLevelAccess.create(world, tileEntity.getBlockPos());
                return stillValid(callable, playerEntity, getBlock().get());
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        int inventorySize = getInventorySize();

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            if (index < inventorySize) {
                if (!this.moveItemStackTo(stack, inventorySize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, inventorySize, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    public void registerPlayerSlots(IItemHandler wrappedInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new SlotItemHandler(wrappedInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new SlotItemHandler(wrappedInventory, i, 8 + i * 18, 142));
        }
    }
}
