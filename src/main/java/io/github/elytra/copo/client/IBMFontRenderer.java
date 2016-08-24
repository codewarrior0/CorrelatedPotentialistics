package io.github.elytra.copo.client;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class IBMFontRenderer {
	private static final ResourceLocation BIOS = new ResourceLocation("correlatedpotentialistics", "textures/gui/bios.png");
	private static final String CP437 =
			  "\0☺☻♥♦♣♠•◘○◙♂♀♪♫☼►◄↕‼¶§▬↨↑↓→←∟↔▲▼"
			+ " !\"#$%&'()*+,-./0123456789:;<=>?"
			+ "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"
			+ "`abcdefghijklmnopqrstuvwxyz{|}~⌂"
			+ "ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜ¢£¥₧ƒ"
			+ "áíóúñÑªº¿⌐¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐"
			+ "└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀"
			+ "αβΓπΣσμτΦΘΩδ∞φε∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u00a0";
	
	// Common substitutes as seen at https://en.wikipedia.org/wiki/Code_page_437#Notes
	private static final Map<Character, Character> substitutes = ImmutableMap.<Character, Character>builder()
			.put('ß', 'β') // Sharp S = beta
			.put('Π', 'π') // Pi = pi
			.put('∏', 'π') // N-ary Product = pi
			.put('∑', 'Σ') // N-ary Summation = Sigma
			.put('µ', 'μ') // Micro = mu
			.put('Ω', 'Ω') // Ohm = Omega
			.put('ð', 'δ') // eth = delta
			.put('∂', 'δ') // Partial Derivative = delta
			.put('∅', 'φ') // Empty Set = phi
			.put('ϕ', 'φ') // Phi Symbol = phi
			.put('⌀', 'φ') // Diameter Sign = phi 
			.put('ø', 'φ') // Lowercase O with Stroke = phi
			.put('∈', 'ε') // Element Of = epsilon
			.put('€', 'ε') // Euro = epsilon
			.build();
	
	public static final int DIM_WHITE = 0xFFA8A8A8;
	
	public static void drawStringInverseVideo(int x, int y, String str, int color) {
		// this is not copied from vanilla, to be clear
		// this is just black magic
		GlStateManager.enableDepth();
		
			GlStateManager.depthMask(true);
			GlStateManager.colorMask(false, false, false, false);
			
			GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 1);
				drawString(x, y, str, 0);
			GlStateManager.popMatrix();
			
			GlStateManager.depthMask(false);
			GlStateManager.colorMask(true, true, true, true);
			
			GlStateManager.pushMatrix();
				GlStateManager.scale(0.5f, 0.5f, 1);
				Gui.drawRect(x*2, y*2, (x*2)+(str.length()*9), (y*2)+16, color);
			GlStateManager.popMatrix();
			
		GlStateManager.disableDepth();
	}
	public static void drawString(int x, int y, String str, int color) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(BIOS);
		x*=2;
		y*=2;
		GlStateManager.pushMatrix();
		GlStateManager.color(((color >> 16)&0xFF)/255f, ((color >> 8)&0xFF)/255f, (color&0xFF)/255f);
		GlStateManager.scale(0.5, 0.5, 1);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (substitutes.containsKey(c)) {
				//c = substitutes.get(c);
			}
			int pos = CP437.indexOf(c);
			if (pos == -1) continue;
			int u = (pos%32)*9;
			int v = (pos/32)*16;
			Gui.drawModalRectWithCustomSizedTexture(x+(i*9), y, u, v, 9, 16, 288, 147);
		}
		GlStateManager.popMatrix();
	}

	public static boolean canRender(char c) {
		return substitutes.containsKey(c) || CP437.contains(Character.toString(c));
	}
}
