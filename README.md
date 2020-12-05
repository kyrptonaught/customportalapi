

# Custom Portal Api
Library mod allowing developers to easily create portals to their custom dimensions. These custom portals will function exactly like nether portals except being fully customizable. You can control the frame block, portal block or tinting of the default, ignition source, and destination

The lib is hosted on [Bintray](https://bintray.com/kyrptonaught/customportalapi/customportalapi)
 [ ![Download](https://api.bintray.com/packages/kyrptonaught/customportalapi/customportalapi/images/download.svg) ](https://bintray.com/kyrptonaught/customportalapi/customportalapi/_latestVersion) 

|![Some example of portals](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-10-05_04.02.08.png)| ![](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-11_15.01.14.png) |
|----------------------------|--|
|     ![p](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-15_17.06.44.png)                       |![p](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-15_17.07.38.png)  |

# Usage: 
**beta16 added new methods, deprecating the old ones. They will be removed eventually**

Add the repository to your build.gradle 
```java
maven { url "https://dl.bintray.com/kyrptonaught/customportalapi" }
  ```
Add the dependency
```java
  modImplementation 'net.kyrptonaught:customportalapi:<version>'
  include 'net.kyrptonaught:customportalapi:<version>'
  ```

Now onto registering the portal itself, all we need to do is call one of the overloads of CustomPortalApiRegistry.addPortal in your mod initializer

The most basic form is 
```java
 CustomPortalApiRegistry.addPortal(Block frameBlock, Identifier dimID, int r, int g, int b)
  ```
  We need to specify:
 - a frameBlock: this is the block used as the frame of the
   portal(nether portal being Blocks.Obsidian)  
 - a dimID: the identifier for the dimension
 - r, g, b- integer(0-255) specifying the tint color used for tinting the portal block itself, the teleportation overlay, and the particles
 
A nether portal would be registered as follows: 
```java
CustomPortalApiRegistry.addPortal(Blocks.OBSIDIAN, new Identifier("the_nether"), 131, 66, 184);
  ```
Next we have 
```java
 CustomPortalApiRegistry.addPortal(Block frameBlock, PortalIgnitionSource ignitionSource, Identifier dimID, int r, int g, int b) 
  ```
  This allows us to specify a custom ignition source.~~currently only Blocks.Fire and Blocks.WATER(aether portal style) is supported, but will be updated later with full support for all blocks and items~~
  As of beta16, PortalIgnitionSource has been introduced, allowing any item/fluid or fire to be specified as the source for igniting the portal.
  
  **PortalIgnitionSource**
  
  PortalIgnitionSource features some presets and methods to create a custom ignition source.
  As of now the only block allowed is fire. I would consider more as requested, but I'd like to keep it hardcoded to a limited number of blocks for performance reasons mostly. Rather than every single block being placed to check if it's a valid portal ignition source and attempting to light a portal, I'd rather just a set few. All the usages are static.
  
 Fire and Water have fields usable like so: 
  `PortalIgnitionSource.FIRE` and `PortalIgnitionSource.WATER`
  
  ```java
  PortalIgnitionSource.ItemUseSource(Item item)
  ``` 
   for items.
  ```java
PortalIgnitionSource.FluidSource(Fluid fluid)
  ``` 
   for fluids.
 
  ```java
PortalIgnitionSource.CustomSource(Identifier ignitionSourceID)
  ``` 
  Lastly we have a custom source. The ignitionSourceID should be unique to prevent overlapping. The identifier should feature your modid, and a uniqie id. This is a completely custom source with no functionality by default allowing you to get as creative as you want. You also then need to trigger the custom activation attempt, when desired. The result should be saved for use in your activation attempt, Like so:
   ```java
  PortalPlacer.attemptPortalLight(World world, BlockPos portalPos, BlockPos framePos, PortalIgnitionSource ignitionSource)
  ``` 
  
  -returns true if ignition was successful, false if not.
  
 -portalPos - pos of an air block inside the desired portal.
 
 -framePos - pos of one of the frame blocks for the desired portal.
 
 -ignitionSource - the custom source used before. 
 
 The registered portal, and the activation attempt must have the same frame block, and ignitionSource in order for a successful light
 For example, here is how fire does it: 
  ```java
 PortalPlacer.attemptPortalLight(world, pos, pos.down(), PortalIgnitionSource.FIRE)
  ``` 

Last we have 
```java
 CustomPortalApiRegistry.addPortal(Block frameBlock, PortalIgnitionSource ignitionSource, CustomPortalBlock portalBlock, Identifier dimID, int r, int g, int b)
  ```
  This one allows us to specify a custom block to be used as the portal block itself. Your custom portal block must extend CustomPortalBlock, and you cannot override onEntityCollision or getStateForNeighborUpdate as these are crucial to the portal functioning. (I suppose you could override them and just call super but that may not work as intended in all scenarios).


