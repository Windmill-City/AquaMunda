package mcjty.aquamunda.blocks.cooker;

import mcjty.aquamunda.blocks.generic.GenericTE;
import mcjty.aquamunda.fluid.FluidSetup;
import mcjty.aquamunda.hosemultiblock.IHoseConnector;
import mcjty.aquamunda.varia.BlockTools;
import mcjty.aquamunda.varia.NBTHelper;
import mcjty.immcraft.api.cable.ICableSubType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.Fluid;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class CookerTE extends GenericTE implements IHoseConnector, ITickable {

    public static final int INPUT_PER_TICK = 3;
    public static final int MAX_AMOUNT = 1000;
    public static final int TICKS_PER_OPERATION = 20;

    private int amount = 0;
    private float temperature = 20;
    private int counter = 0;

    private Set<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

    @Override
    public boolean canConnect(EnumFacing blockSide) {
        if (blockSide == EnumFacing.UP || blockSide == EnumFacing.DOWN) {
            return false;
        }
        return !connections.contains(blockSide);
    }

    @Override
    public int connect(EnumFacing blockSide, int networkId, ICableSubType subType) {
        markDirty();
        if (!connections.contains(blockSide)) {
            connections.add(blockSide);
            return blockSide.ordinal();
        }
        return -1;
    }

    @Override
    public Vec3d getConnectorLocation(int connectorId, EnumFacing rotation) {
        EnumFacing side = EnumFacing.values()[connectorId];
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        switch (side) {
            case DOWN:
                return new Vec3d(xCoord+.5f, yCoord, zCoord+.5f);
            case UP:
                return new Vec3d(xCoord+.5f, yCoord+1, zCoord+.5f);
            case NORTH:
                return new Vec3d(xCoord+.5f, yCoord+.1f, zCoord);
            case SOUTH:
                return new Vec3d(xCoord+.5f, yCoord+.1f, zCoord+1);
            case WEST:
                return new Vec3d(xCoord, yCoord+.1f, zCoord+.5f);
            case EAST:
                return new Vec3d(xCoord+1, yCoord+.1f, zCoord+.5f);
            default:
                return new Vec3d(xCoord, yCoord, zCoord);
        }
    }

    public float getTemperature() {
        return temperature;
    }

    @Override
    public void disconnect(int connectorId) {
        EnumFacing side = EnumFacing.values()[connectorId];
        connections.remove(side);
        markDirty();
    }

    @Override
    public int extract(int amount) {
        return 0;
    }

    @Override
    public Fluid getSupportedFluid() {
        return FluidSetup.freshWater;
    }

    @Override
    public int getMaxExtractPerTick() {
        return 0;
    }

    @Override
    public int getMaxInsertPerTick() {
        return INPUT_PER_TICK;
    }

    @Override
    public int getEmptyLiquidLeft() {
        return MAX_AMOUNT - amount;
    }

    @Override
    public int insert(Fluid fluid, int a) {
        int inserted = Math.min(MAX_AMOUNT - amount, a);
        amount += inserted;
        return inserted;
    }

    @Override
    public float getFilledPercentage() {
        return 100.0f * amount / MAX_AMOUNT;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            if (amount <= 0) {
                // We have no liquid so we cool
                if (temperature > 20) {
                    temperature--;
                    markDirty();
                }
                return;
            }
            counter--;
            if (counter <= 0) {
                counter = TICKS_PER_OPERATION;

                if (isHot()) {
                    if (temperature < 100) {
                        temperature += (125.0f - getFilledPercentage()) / 50.0f;
                    }
                } else {
                    if (temperature > 20) {
                        temperature -= (125.0f - getFilledPercentage()) / 50.0f;
                    }
                }
            }
            markDirty();
        }
    }

    private boolean isHot() {
        return BlockTools.isHot(getWorld(), getPos().down());
    }


    private static Random random = new Random();

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        amount = tagCompound.getInteger("amount");
        temperature = tagCompound.getFloat("temperature");
        counter = tagCompound.getInteger("counter");
        connections.clear();
        for (EnumFacing direction : EnumFacing.VALUES) {
            if (tagCompound.hasKey("c" + direction.ordinal())) {
                connections.add(direction);
            }
        }
    }

    @Override
    public void writeToNBT(NBTHelper helper) {
        super.writeToNBT(helper);
        helper
                .set("amount", amount)
                .set("temperature", temperature)
                .set("counter", counter);
        for (EnumFacing direction : EnumFacing.VALUES) {
            if (connections.contains(direction)) {
                helper.set("c" + direction.ordinal(), true);
            }
        }
    }
}
