package com.gmx.mattcha.roto.entity;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class EntityNPC extends EntityHuman {

    public EntityNPC(FullChunk chunk, CompoundTag nbt, Skin skin) {
        super(chunk, nbt);

        this.skin = skin;
    }

    @Override
    protected void initEntity() {
        this.skin = new Skin();
        super.initEntity();
    }

    @Override
    public int getNetworkId() {
        return -1;
    }
}
