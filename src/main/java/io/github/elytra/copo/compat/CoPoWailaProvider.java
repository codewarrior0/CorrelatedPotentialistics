package io.github.elytra.copo.compat;

import java.util.List;

import io.github.elytra.copo.CoPo;
import io.github.elytra.copo.block.BlockWirelessEndpoint;
import io.github.elytra.copo.block.BlockWirelessEndpoint.Kind;
import io.github.elytra.copo.helper.Numbers;
import io.github.elytra.copo.item.ItemDrive;
import io.github.elytra.copo.item.ItemMemory;
import io.github.elytra.copo.tile.TileEntityController;
import io.github.elytra.copo.tile.TileEntityDriveBay;
import io.github.elytra.copo.tile.TileEntityInterface;
import io.github.elytra.copo.tile.TileEntityMemoryBay;
import io.github.elytra.copo.tile.TileEntityNetworkMember;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CoPoWailaProvider implements IWailaDataProvider {

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound nbt, World world, BlockPos pos) {
		if (te instanceof TileEntityController) {
			TileEntityController tec = (TileEntityController)te;
			nbt.setInteger("Energy", tec.getEnergyStored(EnumFacing.UP));
			nbt.setInteger("MaxEnergy", tec.getMaxEnergyStored(EnumFacing.UP));
			if (tec.error && tec.errorReason != null) {
				nbt.setString("ErrorReason", tec.errorReason);
			} else if (tec.booting) {
				nbt.setInteger("BootTicks", tec.bootTicks);
			}
		}
		if (te instanceof TileEntityNetworkMember) {
			nbt.setLong("EnergyPerTick", ((TileEntityNetworkMember) te).getEnergyConsumedPerTick());
			nbt.setBoolean("HasController", ((TileEntityNetworkMember) te).hasStorage());
		}
		if (te instanceof TileEntityDriveBay) {
			TileEntityDriveBay tedb = (TileEntityDriveBay)te;
			int totalBytesUsed = 0;
			int totalMaxBytes = 0;
			int driveCount = 0;
			for (ItemStack is : tedb) {
				driveCount++;
				if (is.getItemDamage() != 4) {
					totalBytesUsed += ((ItemDrive)is.getItem()).getKilobitsUsed(is)/8;
					totalMaxBytes += ((ItemDrive)is.getItem()).getMaxKilobits(is)/8;
				}
			}

			int totalBytesPercent = (int)(((double)totalBytesUsed/(double)totalMaxBytes)*100);
			
			nbt.setInteger("DriveCount", driveCount);
			nbt.setInteger("BytesUsed", totalBytesUsed);
			nbt.setInteger("MaxBytes", totalMaxBytes);
			nbt.setInteger("BytesPercent", totalBytesPercent);
		}
		if (te instanceof TileEntityMemoryBay) {
			TileEntityMemoryBay temb = (TileEntityMemoryBay)te;
			int totalMaxBytes = 0;
			int memoryCount = 0;
			for (int i = 0; i < 12; i++) {
				if (temb.hasMemoryInSlot(i)) {
					memoryCount++;
					ItemStack is = temb.getMemoryInSlot(i);
					if (is.getItem() instanceof ItemMemory) {
						totalMaxBytes += ((ItemMemory)is.getItem()).getMaxBits(is)/8;
					}
				}
			}

			nbt.setInteger("MemoryCount", memoryCount);
			nbt.setInteger("MaxBytes", totalMaxBytes);
		}
		return nbt;
	}

	@Override
	public List<String> getWailaBody(ItemStack stack, List<String> body, IWailaDataAccessor access, IWailaConfigHandler config) {
		NBTTagCompound nbt = access.getNBTData();
		if (access.getBlock() == CoPo.controller) {
			if (nbt.hasKey("ErrorReason")) {
				body.add("\u00A7c"+I18n.format("tooltip.correlatedpotentialistics.controller_error."+nbt.getString("ErrorReason")));
			} else if (nbt.hasKey("BootTicks") && nbt.getInteger("Energy") >= nbt.getInteger("EnergyPerTick")) {
				int bootTicks = nbt.getInteger("BootTicks");
				if (bootTicks < 0) {
					body.add("\u00A7a"+I18n.format("tooltip.correlatedpotentialistics.controller_booting.hard"));
				} else {
					body.add("\u00A7a"+I18n.format("tooltip.correlatedpotentialistics.controller_booting"));
				}
				int seconds;
				if (bootTicks >= 0) {
					seconds = (100-bootTicks)/20;
				} else {
					seconds = ((bootTicks*-1)+100)/20;
				}
				if (seconds == 1) {
					body.add("\u00A7a"+I18n.format("tooltip.correlatedpotentialistics.controller_boot_eta_one"));
				} else {
					body.add("\u00A7a"+I18n.format("tooltip.correlatedpotentialistics.controller_boot_eta", seconds));
				}
			}
			body.add(I18n.format("tooltip.correlatedpotentialistics.controller_consumption_rate", nbt.getInteger("EnergyPerTick")));
			body.add(I18n.format("tooltip.correlatedpotentialistics.controller_energy_buffer", nbt.getInteger("Energy"), nbt.getInteger("MaxEnergy")));
		} else if (access.getTileEntity() instanceof TileEntityNetworkMember) {
			if (nbt.getBoolean("HasController")) {
				body.add(I18n.format("tooltip.correlatedpotentialistics.member_consumption_rate", nbt.getInteger("EnergyPerTick")));
			} else {
				body.add("\u00A7c"+I18n.format("tooltip.correlatedpotentialistics.no_controller"));
			}
		}
		if (access.getTileEntity() instanceof TileEntityDriveBay) {
			body.add(I18n.format("tooltip.correlatedpotentialistics.drive_count", nbt.getInteger("DriveCount")));
			body.add(I18n.format("tooltip.correlatedpotentialistics.bytes_used", Numbers.humanReadableBytes(nbt.getInteger("BytesUsed")*1024), Numbers.humanReadableBytes(nbt.getInteger("MaxBytes")*1024), nbt.getInteger("BytesPercent")));
		} else if (access.getTileEntity() instanceof TileEntityInterface) {
			TileEntityInterface tei = (TileEntityInterface)access.getTileEntity();
			EnumFacing side = access.getSide();
			body.add(I18n.format("tooltip.correlatedpotentialistics.side", I18n.format("direction.correlatedpotentialistics."+side.getName())));
			body.add(I18n.format("tooltip.correlatedpotentialistics.mode", I18n.format("tooltip.correlatedpotentialistics.iface.mode_"+tei.getModeForFace(side).getName())));
		} else if (access.getTileEntity() instanceof TileEntityMemoryBay) {
			body.add(I18n.format("tooltip.correlatedpotentialistics.memory_count", nbt.getInteger("MemoryCount")));
			body.add(I18n.format("tooltip.correlatedpotentialistics.bytes_available", Numbers.humanReadableBytes(nbt.getInteger("MaxBytes"))));
		}
		return body;
	}

	@Override
	public List<String> getWailaHead(ItemStack stack, List<String> head, IWailaDataAccessor access, IWailaConfigHandler config) {
		return head;
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor access, IWailaConfigHandler config) {
		if (access.getBlock() == CoPo.controller) {
			return new ItemStack(access.getBlock(), 1, access.getMetadata());
		} else if (access.getBlock() == CoPo.wireless_endpoint) {
			return new ItemStack(access.getBlock(), 1, access.getBlockState().getValue(BlockWirelessEndpoint.kind) == Kind.RECEIVER ? 0 : 1);
		} else {
			return new ItemStack(access.getBlock());
		}
	}

	@Override
	public List<String> getWailaTail(ItemStack stack, List<String> tail, IWailaDataAccessor access, IWailaConfigHandler config) {
		return tail;
	}

}
