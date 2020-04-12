package com.gmx.mattcha.sit.entity;

/*
* Sit
*
* Copyright (c) 2020 beito
*
* This software is released under the GPLv3.
* https://www.gnu.org/licenses/gpl-3.0.en.html
*/

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityRideable;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;

public class Chair extends Entity implements EntityRideable {

    public int NETWORK_ID = EntityItem.NETWORK_ID;
    public Vector3f MountedOffset = new Vector3f();

    private EntityMetadata defaultProperties = new EntityMetadata()
            .putLong(DATA_FLAGS, (1L << DATA_FLAG_NO_AI) |
                    (1L << DATA_FLAG_INVISIBLE));

    public Chair(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_NO_AI);
        this.setDataFlag(DATA_FLAGS, DATA_FLAG_INVISIBLE);

        if (namedTag.exist("remove")) {
            this.close();
        }
    }

    @Override
    public boolean canCollide() {
        return false;
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        namedTag.putByte("remove", 1); //Remove Flag
    }

    @Override
    public Vector3f getMountedOffset(Entity entity) {
        return this.MountedOffset;
    }
}
