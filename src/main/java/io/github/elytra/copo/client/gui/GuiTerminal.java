package io.github.elytra.copo.client.gui;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.IBMFontRenderer;
import io.github.elytra.copo.client.gui.shell.GuiTerminalShell;
import io.github.elytra.copo.helper.Numbers;
import io.github.elytra.copo.inventory.ContainerTerminal;
import io.github.elytra.copo.inventory.ContainerTerminal.CraftingAmount;
import io.github.elytra.copo.inventory.ContainerTerminal.CraftingTarget;
import io.github.elytra.copo.inventory.ContainerTerminal.SlotVirtual;
import io.github.elytra.copo.inventory.ContainerTerminal.SortMode;
import io.github.elytra.copo.network.SetSearchQueryServerMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiTerminal extends GuiContainer {
	private static final ResourceLocation background = new ResourceLocation("correlatedpotentialistics", "textures/gui/container/terminal.png");

	private ContainerTerminal container;
	private GuiTextField searchField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 0, 0, 85, 8);
	private String lastSearchQuery = "";
	private GuiButtonExt sortDirection;
	private GuiButtonExt sortMode;
	private GuiButtonExt craftingTarget;
	private GuiButtonExt craftingAmount;
	private GuiButtonExt clearGrid;
	private GuiButtonExt focusByDefault;
	private GuiButtonExt jeiSync;
	
	private String lastJeiQuery;
	
	public GuiTerminal(ContainerTerminal container) {
		super(container);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setTextColor(-1);
		searchField.setFocused(container.searchFocusedByDefault);
		this.container = container;
		xSize = 256;
		ySize = 222;
		if (container.status.isEmpty()) {
			if (Math.random() == 0.5) {
				container.status.add("Dis is one half.");
			} else {
				container.status.add("Ready.");
			}
		}
		lastJeiQuery = CoPo.inst.jeiQueryReader.get();
	}

	protected boolean hasStatusLine() {
		return true;
	}
	
	protected boolean hasSearchAndSort() {
		return true;
	}
	
	protected ResourceLocation getBackground() {
		return background;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
		mc.getTextureManager().bindTexture(getBackground());
		drawTexturedModalRect(0, 0, 0, 0, 256, 222);
		if (container.terminal.supportsDumpSlot()) {
			drawTexturedModalRect(17, 153, 200, 224, 32, 32);
		}
		GlStateManager.popMatrix();
	}
	
	protected String getTitle() {
		return I18n.format("gui.correlatedpotentialistics.terminal");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(getTitle(), 8, 6, 0x404040);
		if (hasStatusLine()) {
			String lastLine = Strings.nullToEmpty(container.status.get(container.status.size()-1));
			if (lastLine.length() > 32) {
				lastLine = lastLine.substring(0, 32)+"...";
			}
			int left = 68+container.playerInventoryOffsetX;
			int top = 90+container.playerInventoryOffsetY;
			int right = 162+68+container.playerInventoryOffsetX;
			int bottom = 11+89+container.playerInventoryOffsetY;
			
			drawRect(left, top, right, bottom, 0xFF006D4B);
			IBMFontRenderer.drawString(left+2, top+1, lastLine, 0x00DBAD);
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.disableDepth();
		GlStateManager.scale(0.5f, 0.5f, 1);
		for (Slot slot : inventorySlots.inventorySlots) {
			if (slot instanceof SlotVirtual) {
				SlotVirtual sv = ((SlotVirtual)slot);
				if (sv.getCount() > 0) {
					String str = Numbers.humanReadableItemCount(sv.getCount());
					int x = sv.xDisplayPosition*2;
					int y = sv.yDisplayPosition*2;
					x += (32-mc.fontRendererObj.getStringWidth(str));
					y += (32-mc.fontRendererObj.FONT_HEIGHT);
					mc.fontRendererObj.drawStringWithShadow(str, x, y, -1);
				}
			}
		}
		GlStateManager.popMatrix();
		GlStateManager.enableDepth();

		int u = 232;
		if (container.rows <= container.slotsTall) {
			u += 12;
		}
		int y = 18;
		GlStateManager.color(1, 1, 1);
		mc.getTextureManager().bindTexture(getBackground());
		drawTexturedModalRect(getScrollTrackX(), getScrollTrackY()+y+(scrollKnobY-6), u, 241, 12, 15);

		GlStateManager.pushMatrix();
		GlStateManager.translate(-(width - xSize) / 2, -(height - ySize) / 2, 0);
		if (container.hasCraftingMatrix) {
			drawTexturedModalRect(clearGrid.xPosition+2, clearGrid.yPosition+2, 0, 222, 2, 10);
			drawTexturedModalRect(craftingTarget.xPosition+2, craftingTarget.yPosition+2, container.craftingTarget.ordinal()*8, 232, 8, 8);
			drawTexturedModalRect(craftingAmount.xPosition+2, craftingAmount.yPosition+2, container.craftingAmount.ordinal()*8, 240, 8, 8);			
		}
		
		if (hasSearchAndSort()) {
			drawTexturedModalRect(sortDirection.xPosition+2, sortDirection.yPosition+2, container.sortAscending ? 0 : 8, 248, 8, 8);
			drawTexturedModalRect(sortMode.xPosition+2, sortMode.yPosition+2, 16+(container.sortMode.ordinal()*8), 248, 8, 8);
			drawTexturedModalRect(focusByDefault.xPosition+2, focusByDefault.yPosition+2, container.searchFocusedByDefault ? 192 : 184, 240, 8, 8);
			if (jeiSync != null) {
				drawTexturedModalRect(jeiSync.xPosition+2, jeiSync.yPosition+2, container.jeiSyncEnabled ? 192 : 184, 248, 8, 8);
			}
			searchField.drawTextBox();
			if (sortMode.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlatedpotentialistics.sortmode"),
						"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.sortmode."+container.sortMode.lowerName)
					), mouseX, mouseY);
			}
			if (sortDirection.isMouseOver()) {
				String str = (container.sortAscending ? "ascending" : "descending");
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlatedpotentialistics.sortdirection"),
						"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.sortdirection."+str)
					), mouseX, mouseY);
			}
			if (focusByDefault.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlatedpotentialistics.focus_search_by_default."+container.searchFocusedByDefault)
					), mouseX, mouseY);
			}
			if (jeiSync != null && jeiSync.isMouseOver()) {
				drawHoveringText(Lists.newArrayList(
						I18n.format("tooltip.correlatedpotentialistics.jei_sync."+container.jeiSyncEnabled)
					), mouseX, mouseY);
			}
			if (container.hasCraftingMatrix) {
				if (craftingAmount.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(
							I18n.format("tooltip.correlatedpotentialistics.crafting_amount"),
							"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.crafting.only_shift_click"),
							"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.crafting_amount."+container.craftingAmount.lowerName)
						), mouseX, mouseY);
				}
				if (craftingTarget.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(
							I18n.format("tooltip.correlatedpotentialistics.crafting_target"),
							"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.crafting.only_shift_click"),
							"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.crafting_target."+container.craftingTarget.lowerName)
						), mouseX, mouseY);
				}
				if (clearGrid.isMouseOver()) {
					drawHoveringText(Lists.newArrayList(I18n.format("tooltip.correlatedpotentialistics.clear_crafting_grid")), mouseX, mouseY);
				}
			}
		}
		GlStateManager.popMatrix();

	}

	private boolean draggingScrollKnob = false;
	private int scrollKnobY = 6;
	private int ticksSinceLastQueryChange = 0;

	@Override
	public void initGui() {
		super.initGui();
		int x = (width - xSize) / 2;
		x += getXOffset();
		int y = (height - ySize) / 2;
		y += getYOffset();
		if (hasSearchAndSort()) {
			searchField.xPosition = x+143;
			searchField.yPosition = y+6;
			buttonList.add(sortDirection = new GuiButtonExt(0, x+236, y+4, 12, 12, ""));
			buttonList.add(sortMode = new GuiButtonExt(1, x+128, y+4, 12, 12, ""));
			buttonList.add(focusByDefault = new GuiButtonExt(5, x+114, y+4, 12, 12, ""));
			if (CoPo.inst.jeiAvailable) {
				buttonList.add(jeiSync = new GuiButtonExt(6, x-getXOffset()+getJeiSyncX(), y-getYOffset()+getJeiSyncY(), 12, 12, ""));
			}
		}
		if (container.hasCraftingMatrix) {
			buttonList.add(craftingAmount = new GuiButtonExt(2, x+51, y+99, 12, 12, ""));
			buttonList.add(craftingTarget = new GuiButtonExt(3, x+51, y+113, 12, 12, ""));
			buttonList.add(clearGrid = new GuiButtonExt(4, x+61, y+37, 6, 14, ""));
		}
	}
	
	protected int getYOffset() {
		return 0;
	}
	
	protected int getXOffset() {
		return 0;
	}
	
	protected int getJeiSyncX() {
		return 236;
	}
	
	protected int getJeiSyncY() {
		return 130;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			mc.playerController.sendEnchantPacket(container.windowId, container.sortAscending ? -2 : -1);
			container.sortAscending = !container.sortAscending;
		} else if (button.id == 1) {
			switch (container.sortMode) {
				case QUANTITY:
					container.sortMode = SortMode.MOD_MINECRAFT_FIRST;
					mc.playerController.sendEnchantPacket(container.windowId, -4);
					break;
				case MOD_MINECRAFT_FIRST:
					container.sortMode = SortMode.MOD;
					mc.playerController.sendEnchantPacket(container.windowId, -5);
					break;
				case MOD:
					container.sortMode = SortMode.NAME;
					mc.playerController.sendEnchantPacket(container.windowId, -6);
					break;
				case NAME:
					container.sortMode = SortMode.QUANTITY;
					mc.playerController.sendEnchantPacket(container.windowId, -3);
					break;
			}
		} else if (button.id == 2) {
			switch (container.craftingAmount) {
				case ONE:
					container.craftingAmount = CraftingAmount.STACK;
					mc.playerController.sendEnchantPacket(container.windowId, -11);
					break;
				case STACK:
					container.craftingAmount = CraftingAmount.MAX;
					mc.playerController.sendEnchantPacket(container.windowId, -12);
					break;
				case MAX:
					container.craftingAmount = CraftingAmount.ONE;
					mc.playerController.sendEnchantPacket(container.windowId, -10);
					break;
			}
		} else if (button.id == 3) {
			switch (container.craftingTarget) {
				case INVENTORY:
					container.craftingTarget = CraftingTarget.NETWORK;
					mc.playerController.sendEnchantPacket(container.windowId, -21);
					break;
				case NETWORK:
					container.craftingTarget = CraftingTarget.INVENTORY;
					mc.playerController.sendEnchantPacket(container.windowId, -20);
					break;

			}
		} else if (button.id == 4) {
			mc.playerController.sendEnchantPacket(container.windowId, -128);
		} else if (button.id == 5) {
			mc.playerController.sendEnchantPacket(container.windowId, container.searchFocusedByDefault ? -25 : -24);
			container.searchFocusedByDefault = !container.searchFocusedByDefault;
		} else if (button.id == 6) {
			mc.playerController.sendEnchantPacket(container.windowId, container.jeiSyncEnabled ? -27 : -26);
			container.jeiSyncEnabled = !container.jeiSyncEnabled;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (container.rows > container.slotsTall) {
			int dWheel = Mouse.getDWheel()/container.rows;
			if (dWheel != 0) {
				scrollKnobY = Math.max(Math.min(getScrollTrackHeight()-9, scrollKnobY-dWheel), 6);
				mc.playerController.sendEnchantPacket(container.windowId, Math.round(((scrollKnobY-6)/(float)(getScrollTrackHeight()-9))*(container.rows-container.slotsTall)));
			}
		} else {
			scrollKnobY = 6;
		}
		if (hasSearchAndSort()) {
			searchField.updateCursorCounter();
			if (container.jeiSyncEnabled) {
				String jeiQuery = CoPo.inst.jeiQueryReader.get();
				if (!Objects.equal(jeiQuery, lastJeiQuery)) {
					lastJeiQuery = jeiQuery;
					searchField.setText(jeiQuery);
				}
			}
			if (!Objects.equal(searchField.getText(), lastSearchQuery)) {
				lastSearchQuery = searchField.getText();
				ticksSinceLastQueryChange = 0;
				if (scrollKnobY != 6) {
					scrollKnobY = 6;
					mc.playerController.sendEnchantPacket(container.windowId, 0);
				}
			}
			ticksSinceLastQueryChange++;
			if (ticksSinceLastQueryChange == 4) {
				new SetSearchQueryServerMessage(container.windowId, lastSearchQuery).sendToServer();
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (searchField.isFocused()) {
			if (keyCode == Keyboard.KEY_ESCAPE) {
				mc.thePlayer.closeScreen();
			} else {
				searchField.textboxKeyTyped(typedChar, keyCode);
				if (container.jeiSyncEnabled) {
					CoPo.inst.jeiQueryUpdater.accept(searchField.getText());
				}
			}
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	protected int getScrollTrackX() {
		return 236;
	}
	
	protected int getScrollTrackY() {
		return 0;
	}
	
	protected int getScrollTrackHeight() {
		return 110;
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
		int left = 68+container.playerInventoryOffsetX;
		int top = 90+container.playerInventoryOffsetY;
		int right = 162+68+container.playerInventoryOffsetX;
		int bottom = 11+89+container.playerInventoryOffsetY;
		if (hasStatusLine() && mouseButton == 0
				&& mouseX >= x+left && mouseX <= x+right
				&& mouseY >= y+top && mouseY <= y+bottom) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiTerminalShell(this, container));
		}
		
		int width = 12;
		int height = getScrollTrackHeight();
		x += getScrollTrackX();
		y += getScrollTrackY();
		y += 18;
		if (mouseButton == 0
				&& mouseX >= x && mouseX <= x+width
				&& mouseY >= y && mouseY <= y+height) {
			draggingScrollKnob = true;
			mouseClickMove(mouseX, mouseY, mouseButton, 0);
			return;
		}
		if (hasSearchAndSort()) {
			if (mouseButton == 1
					&& mouseX >= searchField.xPosition && mouseX <= searchField.xPosition+searchField.width
					&& mouseY >= searchField.yPosition && mouseY <= searchField.yPosition+searchField.height) {
				searchField.setText("");
				if (container.jeiSyncEnabled) {
					CoPo.inst.jeiQueryUpdater.accept("");
				}
			}					 
			searchField.mouseClicked(mouseX, mouseY, mouseButton);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
		if (draggingScrollKnob && container.rows > container.slotsTall) {
			int y = (height - ySize) / 2;
			y += getScrollTrackY();
			scrollKnobY = Math.max(Math.min(getScrollTrackHeight()-9, (mouseY-24)-y), 6);
			int s = Math.round(((scrollKnobY+12)/(float)(getScrollTrackHeight()-9))*(container.rows-container.slotsTall));
			mc.playerController.sendEnchantPacket(container.windowId, s);
		}
		super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			draggingScrollKnob = false;
		}
		super.mouseReleased(mouseX, mouseY, mouseButton);
	}

	public void updateSearchQuery(String query) {
		lastSearchQuery = query;
		if (hasSearchAndSort()) {
			searchField.setText(query);
			if (container.jeiSyncEnabled) {
				CoPo.inst.jeiQueryUpdater.accept(query);
			}
		}
	}

	public void addLine(String line) {
		container.status.add(line);
	}

	public void focusSearch() {
		if (searchField != null) {
			searchField.setFocused(true);
		}
	}

}
