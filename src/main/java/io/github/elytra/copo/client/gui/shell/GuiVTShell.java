package io.github.elytra.copo.client.gui.shell;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.client.ClientProxy;
import io.github.elytra.copo.client.gui.GuiVT;
import io.github.elytra.copo.inventory.ContainerVT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;

public class GuiVTShell extends GuiScreen {
	public GuiVT guiVt;
	public ContainerVT container;
	private static final ResourceLocation terminal = new ResourceLocation("correlatedpotentialistics", "textures/gui/terminal.png");
	private static final ResourceLocation terminal_overlay = new ResourceLocation("correlatedpotentialistics", "textures/gui/terminal_overlay.png");
	public Program program = new CommandInterpreter(this);
	
	public GuiVTShell(GuiVT guiVt, ContainerVT container) {
		this.guiVt = guiVt;
		this.container = container;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		int xSize = 256;
		int ySize = 144;
		GlStateManager.pushMatrix();
			GlStateManager.translate((width - xSize) / 2, (height - ySize) / 2, 0);
			Minecraft.getMinecraft().getTextureManager().bindTexture(terminal);
			drawTexturedModalRect(0, 0, 0, 0, 256, 144);
			int rows = 15;
			int cols = 51;
			GlStateManager.pushMatrix();
				GlStateManager.translate(13, 12, 0);
				program.render(rows, cols);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
				ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
				GL11.glScissor((int)((((width - xSize)/2f)+10)*res.getScaleFactor()), (int)((((height - ySize)/2f)+10.375f)*res.getScaleFactor()), (int)(((cols*4.5f)+5f)*res.getScaleFactor()), (int)((rows*8)+3.5f)*res.getScaleFactor());
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
				GlStateManager.translate(0, (ClientProxy.ticks%244)-100, 16);
				int c = CoPo.proxy.getColor("other", 64) & 0x00FFFFFF;
				drawGradientRect(11, 0, 244, 100, c, c | 0x44000000);
				GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
			
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			
			GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 32);
				Minecraft.getMinecraft().getTextureManager().bindTexture(terminal_overlay);
				drawTexturedModalRect(0, 0, 0, 0, 256, 144);
			GlStateManager.popMatrix();
			
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		program.keyTyped(typedChar, keyCode);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		program.update();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}
	
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	public void addLine(String line) {
		container.status.add(line);
	}

}

