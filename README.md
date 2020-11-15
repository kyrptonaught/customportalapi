
# Custom Portal Api
Library mod allowing developers to easily create portals to their custom dimensions. These custom portals will function exactly like nether portals except being fully customizable. You can control the frame block, portal block or tinting of the default, ignition source, and destination

The lib is hosted on [Bintray](https://bintray.com/kyrptonaught/customportalapi/customportalapi)
 [ ![Download](https://api.bintray.com/packages/kyrptonaught/customportalapi/customportalapi/images/download.svg) ](https://bintray.com/kyrptonaught/customportalapi/customportalapi/_latestVersion) 

|![Some example of portals](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-10-05_04.02.08.png)| ![](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-11_15.01.14.png) |
|----------------------------|--|
|     ![p](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-15_17.06.44.png)                       |![p](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-15_17.07.38.png)  |

# Usage:
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
 CustomPortalApiRegistry.addPortal(Block frameBlock, Identifier dimID, int portalColor)
  ```
  We need to specify:
 - a frameBlock: this is the block used as the frame of the
   portal(nether portal being Blocks.Obsidian)  
 - a dimID: the identifier for the dimension
 - a portalColor, this is an integer specifying the tint color used for tinting the portal block itself
 
A nether portal would be registered as follows: 
```java
CustomPortalApiRegistry.addPortal(Blocks.OBSIDIAN, new Identifier("the_nether"), DyeColor.PURPLE.getMaterialColor().color);
  ```
Next we have 
```java
 CustomPortalApiRegistry.addPortal(Block frameBlock, Block ignitionBlock, Identifier dimID, int portalColor) 
  ```
  This allows us to specify a custom ignition source, currently only Blocks.Fire and Blocks.WATER(aether portal style) is supported, but will be updated later with full support for all blocks and items
Last we have 
```java
 CustomPortalApiRegistry.addPortal(Block frameBlock, Block ignitionBlock, CustomPortalBlock portalBlock, Identifier dimID, int portalColor)
  ```
  This one allows us to specify a custom block to be used as the portal block itself. Your custom portal block must extend CustomPortalBlock, and you cannot override onEntityCollision or getStateForNeighborUpdate as these are crucial to the portal functioning. (I suppose you could override them and just call super but that may not work as intended in all scenarios).


