package io.github.elytra.copo.inventory;

import io.github.elytra.copo.entity.EntityAutomaton;
import io.github.elytra.copo.item.ItemModule;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotAutomatonModule extends Slot {
	private EntityAutomaton automaton;
	private int slot;
	public SlotAutomatonModule(EntityAutomaton automaton, int slot, int x, int y) {
		super(null, slot, x, y);
		this.automaton = automaton;
		this.slot = slot;
	}
	
	@Override
	public void putStack(ItemStack stack) {
		automaton.setModule(slot, stack);
		onSlotChanged();
	}
	
	@Override
	public ItemStack getStack() {
		return automaton.getModule(slot);
	}
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return stack.getMaxStackSize();
	}
	
	@Override
	public int getSlotStackLimit() {
		return 1;
	}
	
	@Override
	public ItemStack decrStackSize(int amount) {
		if (getStack() == null) return null;
		ItemStack stack = getStack();
		ItemStack result = stack.splitStack(amount);
		if (stack.stackSize <= 0) {
			putStack(null);
		}
		return result;
	}
	
	@Override
	public boolean isHere(IInventory inv, int slotIn) {
		return false;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemModule && !automaton.hasModule(((ItemModule)stack.getItem()).getType(stack));
	}
	
	@Override
	public void onSlotChanged() {
	}
}
