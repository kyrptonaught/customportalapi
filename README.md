



# Custom Portal Api
Library mod allowing developers to easily create portals to their custom dimensions. These custom portals will function exactly like nether portals except being fully customizable. You can control the frame block, portal block or tinting of the default, ignition source, and destination and more!

|![Some example of portals](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-10-05_04.02.08.png)| ![](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-11_15.01.14.png) |
|----------------------------|--|
|     ![p](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-15_17.06.44.png)                       |![p](https://raw.githubusercontent.com/kyrptonaught/customportalapi/main/images/2020-11-15_17.07.38.png)  |

# Usage: 
## Versions: 
| **1.16** |![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fs3.us-east-2.amazonaws.com%2Fmaven.kyrptonaught.dev%2Fnet%2Fkyrptonaught%2Fcustomportalapi%2Fmaven-metadata.xml&style=for-the-badge&versionSuffix=1.16) |
|--|--|
| **1.17** |![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fs3.us-east-2.amazonaws.com%2Fmaven.kyrptonaught.dev%2Fnet%2Fkyrptonaught%2Fcustomportalapi%2Fmaven-metadata.xml&style=for-the-badge&versionSuffix=1.17)|
| **1.18** |![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fs3.us-east-2.amazonaws.com%2Fmaven.kyrptonaught.dev%2Fnet%2Fkyrptonaught%2Fcustomportalapi%2Fmaven-metadata.xml&style=for-the-badge&versionSuffix=-1.18)|

Add the repository to your build.gradle.
```java
maven {url = "https://maven.kyrptonaught.dev"}
  ```
Add the dependency. 
```java
  modImplementation 'net.kyrptonaught:customportalapi:<version>'
  include 'net.kyrptonaught:customportalapi:<version>'
  ```

Now onto creating and registering the portal itself, this is very simple thanks to the [CustomPortalBuilder](https://github.com/kyrptonaught/customportalapi/blob/1.17/src/main/java/net/kyrptonaught/customportalapi/api/CustomPortalBuilder.java) class. We will make use of this in your mod initializer.

The following is a very simple portal that will take us to the end, and is lit by right clicking the frame with an Eye of Ender.
```java
CustomPortalBuilder.beginPortal()  
        .frameBlock(Blocks.DIAMOND_BLOCK)  
        .lightWithItem(Items.ENDER_EYE)  
        .destDimID(new Identifier("the_end"))  
        .tintColor(45,65,101)  
        .registerPortal();
  ```

A nether portal would be registered as follows: 
```java
CustomPortalBuilder.beginPortal()  
        .frameBlock(Blocks.OBSIDIAN)  
        .destDimID(new Identifier("the_nether"))  
        .tintColor(131, 66, 184)  
        .registerPortal();
  ```

CustomPortalBuilder is filled with plenty of methods to customize the functionality of your portal, all of which are documented in the class.

Some noteworthy methods to mention:

 - lightWithWater/Item/Fluid - These allow you to control how the portal is lit. 
 - onlyLightInOverworld - Only allow the portal to be used in the overworld to your destination of choice
 - flatPortal - Flat Portal similar to the End or the Twilight Forest portal.
