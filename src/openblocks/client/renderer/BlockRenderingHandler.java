package openblocks.client.renderer;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import openblocks.Log;
import openblocks.OpenBlocks;
import openblocks.client.renderer.tileentity.OpenRenderHelper;
import openblocks.common.block.OpenBlock;
import openblocks.common.block.OpenBlock.BlockRotationMode;
import openblocks.common.tileentity.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.Maps;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class BlockRenderingHandler implements ISimpleBlockRenderingHandler {

	private final Map<Block, TileEntity> inventoryTileEntities;
	private final Map<Block, IBlockRenderer> blockRenderers;

	public BlockRenderingHandler() {
		inventoryTileEntities = Maps.newIdentityHashMap();
		blockRenderers = Maps.newIdentityHashMap();
		
		blockRenderers.put(OpenBlocks.Blocks.path, new BlockPathRenderer());

		TileEntityLightbox teLightbox = new TileEntityLightbox();
		inventoryTileEntities.put(OpenBlocks.Blocks.lightbox, teLightbox);
		
		TileEntityLightbox teTank = new TileEntityLightbox();
		inventoryTileEntities.put(OpenBlocks.Blocks.tank, teTank);

		TileEntityTarget teTarget = new TileEntityTarget();
		teTarget.setEnabled(true);
		inventoryTileEntities.put(OpenBlocks.Blocks.target, teTarget);

		TileEntityGrave teGrave = new TileEntityGrave();
		inventoryTileEntities.put(OpenBlocks.Blocks.grave, teGrave);

		TileEntityFlag teFlag = new TileEntityFlag();
		inventoryTileEntities.put(OpenBlocks.Blocks.flag, teFlag);

		TileEntityTrophy teTrophy = new TileEntityTrophy();
		inventoryTileEntities.put(OpenBlocks.Blocks.trophy, teTrophy);

		TileEntityBearTrap teBearTrap = new TileEntityBearTrap();
		inventoryTileEntities.put(OpenBlocks.Blocks.bearTrap, teBearTrap);

		TileEntitySprinkler teSprinkler = new TileEntitySprinkler();
		inventoryTileEntities.put(OpenBlocks.Blocks.sprinkler, teSprinkler);

		TileEntityVacuumHopper teHopper = new TileEntityVacuumHopper();
		inventoryTileEntities.put(OpenBlocks.Blocks.vacuumHopper, teHopper);

		TileEntityCannon teCannon = new TileEntityCannon();
		teCannon.disableLineRender();
		inventoryTileEntities.put(OpenBlocks.Blocks.cannon, teCannon);

		TileEntityBigButton teButton = new TileEntityBigButton();
		inventoryTileEntities.put(OpenBlocks.Blocks.bigButton, teButton);

		TileEntityFan teFan = new TileEntityFan();
		inventoryTileEntities.put(OpenBlocks.Blocks.fan, teFan);

		TileEntityVillageHighlighter teVillageHighlighter = new TileEntityVillageHighlighter();
		inventoryTileEntities.put(OpenBlocks.Blocks.villageHighlighter, teVillageHighlighter);

		TileEntityAutoAnvil teAutoAnvil = new TileEntityAutoAnvil();
		inventoryTileEntities.put(OpenBlocks.Blocks.autoAnvil, teAutoAnvil);

		TileEntityAutoEnchantmentTable teAutoTable = new TileEntityAutoEnchantmentTable();
		inventoryTileEntities.put(OpenBlocks.Blocks.autoEnchantmentTable, teAutoTable);

		TileEntityRopeLadder teRopeLadder = new TileEntityRopeLadder();
		inventoryTileEntities.put(OpenBlocks.Blocks.ropeLadder, teRopeLadder);
	}

	@Override
	public int getRenderId() {
		return OpenBlocks.renderId;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		
		/**
		 * Deal with special block rendering handlers
		 */
		if (blockRenderers.containsKey(block)) {
			blockRenderers.get(block).renderInventoryBlock(block, metadata, modelID, renderer);
			return;
		}

		/** get special TE renderers */
		TileEntity te = inventoryTileEntities.get(block);
		if (te instanceof OpenTileEntity) {
			((OpenTileEntity)te).prepareForInventoryRender(block, metadata);
		}
		
		try {
			final World world = Minecraft.getMinecraft().theWorld;
			if (world != null) {
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
				if (te != null) {
					/** render special TE renderers */
					// te.worldObj = world;
					GL11.glTranslated(-0.5, -0.5, -0.5);
					TileEntityRenderer.instance.renderTileEntityAt(te, 0.0D, 0.0D, 0.0D, 0.0F);
				} else {
					/** render standard openblockblock renderers */
					// TODO: Fix lighting on these
					if (block instanceof OpenBlock) {
						ForgeDirection direction = ((OpenBlock)block).getRenderDirection();
						rotateFacesOnRenderer((OpenBlock)block, direction, renderer);
					}
					GL11.glEnable(GL11.GL_LIGHTING);
					RenderHelper.enableGUIStandardItemLighting();
					OpenRenderHelper.renderCube(
							block.getBlockBoundsMinX()-0.5,
							block.getBlockBoundsMinY()-0.5,
							block.getBlockBoundsMinZ()-0.5,
							block.getBlockBoundsMaxX()-0.5,
							block.getBlockBoundsMaxY()-0.5,
							block.getBlockBoundsMaxZ()-0.5, block, null);
					resetFacesOnRenderer(renderer);
				}
			}
		} catch (Exception e) {
			Log.severe(e, "Error during block '%s' rendering", block.getUnlocalizedName());
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		/* deal with custom block renderers */
		if (blockRenderers.containsKey(block)) { 
			return blockRenderers.get(block).renderWorldBlock(world, x, y, z, block, modelId, renderer);
			
		/* deal with standard openblock rendering */
		}else if (!inventoryTileEntities.containsKey(block)) {
			if (block instanceof OpenBlock) {
				int metadata = world.getBlockMetadata(x, y, z);
				ForgeDirection rotation = ForgeDirection.getOrientation(metadata);
				rotateFacesOnRenderer((OpenBlock)block, rotation, renderer);
			}
			renderer.renderStandardBlock(block, x, y, z);
			resetFacesOnRenderer(renderer);
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

	private void rotateFacesOnRenderer(OpenBlock block, ForgeDirection rotation, RenderBlocks renderer) {
		BlockRotationMode mode = block.getRotationMode();
		//TODO: clever rotation stuff here
		// I guess we'll need to make some kind of texture manager that we can retrieve from OpenBlock
		// for when we've got something that switches the texture for a side based on a value in the tile entity
		// I dunno, needs thinking about. Is this what you were thinking of, nevercast?
		// renderer.uvRotateTop = 1;
		
	}
	
	private void resetFacesOnRenderer(RenderBlocks renderer) {
		renderer.uvRotateTop = 0;
		renderer.uvRotateBottom = 0;
		renderer.uvRotateEast = 0;
		renderer.uvRotateNorth = 0;
		renderer.uvRotateSouth = 0;
		renderer.uvRotateTop = 0;
		renderer.uvRotateWest = 0;
	}
	
}
