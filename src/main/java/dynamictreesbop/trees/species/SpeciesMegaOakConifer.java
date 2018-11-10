package dynamictreesbop.trees.species;

import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.items.Seed;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenBush;
import com.ferreusveritas.dynamictrees.systems.featuregen.FeatureGenConiferTopper;
import com.ferreusveritas.dynamictrees.trees.SpeciesRare;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import biomesoplenty.api.biome.BOPBiomes;
import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.common.block.BlockBOPMushroom;
import dynamictreesbop.DynamicTreesBOP;
import dynamictreesbop.ModContent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;

public class SpeciesMegaOakConifer extends SpeciesRare {
		
	public SpeciesMegaOakConifer(TreeFamily treeFamily) {
		super(new ResourceLocation(DynamicTreesBOP.MODID, "megaoakconifer"), treeFamily, ModContent.oakConiferLeavesProperties);
		
		setBasicGrowingParameters(0.3f, 32.0f, 7, 7, 1.0f);
		
		setSoilLongevity(14);
		
		envFactor(Type.COLD, 0.75f);
		envFactor(Type.HOT, 0.5f);
		envFactor(Type.PLAINS, 1.05f);
		envFactor(Type.FOREST, 1.05f);
		
		addAcceptableSoil(BOPBlocks.grass, BOPBlocks.dirt);
		
		setupStandardSeedDropping();
		
		//Add species features
		addGenFeature(new FeatureGenConiferTopper(getLeavesProperties()));//Make a topper for this conifer tree
		addGenFeature(new FeatureGenBush(this));//Generate undergrowth
	}
	
	@Override
	public boolean isBiomePerfect(Biome biome) {
		return biome == BOPBiomes.temperate_rainforest.orNull();
	}
	
	@Override
	protected int[] customDirectionManipulation(World world, BlockPos pos, int radius, GrowSignal signal, int probMap[]) {
		EnumFacing originDir = signal.dir.getOpposite();
		
		// Alter probability map for direction change
		probMap[0] = 0; // Down is always disallowed for spruce
		probMap[1] = signal.isInTrunk() ? getUpProbability(): 0;
		probMap[2] = probMap[3] = probMap[4] = probMap[5] = // Only allow turns when we aren't in the trunk(or the branch is not a twig and step is odd)
				!signal.isInTrunk() || (signal.isInTrunk() && signal.numSteps % 2 == 1 && radius > 1) ? 2 : 0;
		probMap[originDir.ordinal()] = 0; // Disable the direction we came from
		probMap[signal.dir.ordinal()] += signal.isInTrunk() ? 0 : signal.numTurns == 1 ? 2 : 1; // Favor current travel direction 
		
		return probMap;
	}
	
	@Override
	protected EnumFacing newDirectionSelected(EnumFacing newDir, GrowSignal signal) {
		if(signal.isInTrunk() && newDir != EnumFacing.UP){ // Turned out of trunk
			signal.energy /= 3.0f;
		}
		return newDir;
	}
	
	@Override
	public float getEnergy(World world, BlockPos pos) {
		long day = world.getTotalWorldTime() / 24000L;
		int month = (int)day / 30;//Change the hashs every in-game month
		
		return super.getEnergy(world, pos) * biomeSuitability(world, pos) + (coordHashCode(pos.up(month)) % 8);//Vary the height energy by a psuedorandom hash function
	}
	
	public int coordHashCode(BlockPos pos) {
		return CoordUtils.coordHashCode(pos, 2);
	}
	
	@Override
	public ItemStack getSeedStack(int qty) {
		return getFamily().getCommonSpecies().getSeedStack(qty);
	}
	
	@Override
	public Seed getSeed() {
		return getFamily().getCommonSpecies().getSeed();
	}
	
	@Override
	public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
		if(super.rot(world, pos, neighborCount, radius, random, rapid)) {
			if(radius > 4 && TreeHelper.isRooty(world.getBlockState(pos.down())) && world.getLightFor(EnumSkyBlock.SKY, pos) < 4) {
				world.setBlockState(pos, BOPBlocks.mushroom.getDefaultState().withProperty(BlockBOPMushroom.VARIANT, BlockBOPMushroom.MushroomType.BLUE_MILK_CAP));//Change branch to a mushroom
			}
			return true;
		}
		return false;
	}
	
}
