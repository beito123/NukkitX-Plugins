# Sit

![image](https://cldup.com/knMDAcnuoy.png)

## Commands

```
/sit - you sit down here!
```

__To stand up, Jump or run the command again.__

## API

You can let entities like npc sit down anywhere!

```java
import com.gmx.mattcha.sit.SitAPI;

public class MainClass extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        // Listen events
        this.getServer().getPluginManager().registerEvents(this, this);

        Server.getInstance().getLogger().notice("Enabled Example Plugin!");
    }

    @EventHandler
    public void onEntityTap(PlayerInteractEntityEvent event) {
        Entity entity = event.getEntity();

        if (SitAPI.getInstance().hasSat(entity)) {
            SitAPI.getInstance().standupEntity(entity);
            return;
        }
        
        // Sit down the entity
        SitAPI.getInstance().sitEntity(entity);
        
        // Or you adjust offsets...
        // SitAPI.getInstance().sitEntity(entity, 
        //    entity.add(0, 0.5, 0),
        //    new Vector3f(0, 3F, 0)
        //);
    }
}
```

## Maven Repo

Maybe you can use with the following codes. 
(I did test only gradle version, so please tell me if fail. sorry...)

```xml
<repository>
  <url>https://beito123.github.io/NukkitX-Plugins/</url>
</repository>

<dependency>
  <groupId>com.gmx.mattcha</groupId>
  <artifactId>Sit</artifactId>
  <version>2.2.0</version>
</dependency>
```

### Gradle version

```gradle
repositories {
    // Others
    maven {
        url 'https://beito123.github.io/NukkitX-Plugins/'
    }
}

dependencies {
    implementation "com.gmx.mattcha:Sit:2.2.0"
}
```
