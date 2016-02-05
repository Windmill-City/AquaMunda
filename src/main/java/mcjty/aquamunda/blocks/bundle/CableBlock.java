package mcjty.aquamunda.blocks.bundle;

import mcjty.aquamunda.blocks.generic.GenericBlock;
import mcjty.aquamunda.blocks.generic.GenericTE;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

public class CableBlock extends GenericBlock {

    public CableBlock(Material material, String name, Class<? extends GenericTE> clazz, Class<? extends ItemBlock> itemBlockClass) {
        super(material, name, clazz, itemBlockClass);
    }

    @Override
    public MetaUsage getMetaUsage() {
        return MetaUsage.NONE;
    }
}
