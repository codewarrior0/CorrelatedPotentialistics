package io.github.elytra.copo.client.gui;

import java.io.IOException;
import java.util.Locale;

import com.google.common.collect.Lists;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.ClientProxy;
import io.github.elytra.copo.entity.EntityAutomaton.AutomatonStatus;
import io.github.elytra.copo.inventory.ContainerAutomaton;
import io.github.elytra.copo.network.SetAutomatonNameMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;

public class GuiAutomaton extends GuiVT implements GuiResponder {
	private static final ResourceLocation background = new ResourceLocation("correlatedpotentialistics", "textures/gui/container/automaton.png");
	private ContainerAutomaton container;
	private GuiTextField name;
	
	public GuiAutomaton(ContainerAutomaton inventorySlotsIn) {
		super(inventorySlotsIn);
		this.container = inventorySlotsIn;
		xSize = 176;
		ySize = 222;
	}

	@Override
	public void initGui() {
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		
		int btnX = x+(114-AutomatonStatus.VALUES.length*12);
		for (AutomatonStatus s : AutomatonStatus.VALUES) {
			GuiButtonExt btn = new GuiButtonExt((-s.ordinal())-30, btnX, y+31, 10, 10, "");
			if (s == AutomatonStatus.EXEC) {
				btn.enabled = false;
			}
			buttonList.add(btn);
			btnX += 12;
		}
		buttonList.add(new GuiButtonExt(-60, x+7, y+31, 10, 10, ""));
		buttonList.add(new GuiButtonExt(-59, x+19, y+31, 10, 10, ""));
		name = new GuiTextField(-55, fontRendererObj, x+6, y+5, 164, 10);
		name.setText(container.automaton.getName());
		name.setGuiResponder(this);
		super.initGui();
	}
	
	@Override
	protected int getXOffset() {
		return -80;
	}
	
	@Override
	protected int getYOffset() {
		return 43;
	}
	
	@Override
	protected int getScrollTrackX() {
		return 156;
	}
	
	@Override
	protected int getScrollTrackY() {
		return 44;
	}
	
	@Override
	protected int getScrollTrackHeight() {
		return 70;
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		name.updateCursorCounter();
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id <= -30 && button.id >= -60) {
			mc.playerController.sendEnchantPacket(container.windowId, button.id);
		} else {
			super.actionPerformed(button);
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(0, 0, 0, 0, 176, 222);
		GlStateManager.popMatrix();
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(background);
		int btnX = (117-AutomatonStatus.VALUES.length*12);
		for (AutomatonStatus s : AutomatonStatus.VALUES) {
			GlStateManager.color(1, 1, 1);
			drawTexturedModalRect(btnX, 34, 176, s.ordinal()*4, 4, 4);
			if (container.automaton.getStatus() == s) {
				drawTexturedModalRect(btnX-3, 31, 246, 246, 10, 10);
			}
			btnX += 12;
		}
		drawTexturedModalRect(10, 34, 180, container.automaton.isMuted() ? 4 : 0, 4, 4);
		drawTexturedModalRect(22, 34, 184, container.automaton.getFollowDistance()*4, 4, 4);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		GlStateManager.pushMatrix();
		GlStateManager.translate(-x, -y, 0);
		name.drawTextBox();
		GlStateManager.popMatrix();
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		GlStateManager.disableLighting();
		GlStateManager.color(1, 1, 1);
		mc.getTextureManager().bindTexture(background);
		int w = (int)(104*(container.automaton.getHealth()/container.automaton.getMaxHealth()));
		drawTexturedModalRect(8, 18, 0, 222, w, 9);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.ZERO, DestFactor.SRC_COLOR);
		int t = (int) (ClientProxy.ticks);
		drawTexturedModalRect(8, 18, t, 231, w, 9);
		GlStateManager.disableBlend();
		for (AutomatonStatus s : AutomatonStatus.VALUES) {
			if (buttonList.get(s.ordinal()).isMouseOver()) {
				GuiUtils.drawHoveringText(
						Lists.newArrayList(I18n.format("tooltip.correlatedpotentialistics.automaton.state."+s.name().toLowerCase(Locale.ROOT))),
						mouseX-((width-xSize)/2), mouseY-((height-ySize)/2), width, height, 80, fontRendererObj);
			}
		}
		if (buttonList.get(AutomatonStatus.VALUES.length+1).isMouseOver()) {
			GuiUtils.drawHoveringText(
					Lists.newArrayList(
							I18n.format("tooltip.correlatedpotentialistics.automaton.followDistance"),
							"\u00A77"+I18n.format("tooltip.correlatedpotentialistics.automaton.followDistance."+container.automaton.getFollowDistance())
						),
					mouseX-((width-xSize)/2), mouseY-((height-ySize)/2), width, height, 80, fontRendererObj);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		name.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		name.textboxKeyTyped(typedChar, keyCode);
		if (!name.isFocused()) {
			super.keyTyped(typedChar, keyCode);
		}
	}
	
	@Override
	protected String getTitle() {
		return "";
	}

	@Override
	public void setEntryValue(int id, boolean value) {
	}

	@Override
	public void setEntryValue(int id, float value) {
	}

	@Override
	public void setEntryValue(int id, String value) {
		if (id == -55) {
			CoPo.inst.network.sendToServer(new SetAutomatonNameMessage(container.windowId, value));
		}
	}


}