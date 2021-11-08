## Changelog
**0.0.1-beta 46**
Added the ability to set custom inPortal sounds and afterTP sounds. Also fixed portal ending up outside the worldBorder

**0.0.1-beta 45**
Flat Portals are now fully functional.

**0.0.1-beta 44**
Implement fully custom Portal Placing logic for the destination portal, no longer uses modified vanilla nether portal code.

**0.0.1-beta 43**
Improve the lighting of portals.

**0.0.1-beta 42**
Begin rework of server side only component.

**0.0.1-beta 41**
Redo the detection of fluid ignited portals, should be more reliable. 

**0.0.1-beta 40**
Completely redo portal linking. They should always link together now. All existing portals will not link properly

**0.0.1-beta 33**
Fix buckets placing the liquid when creating a portal

**0.0.1-beta 32**
Fix crash

**0.0.1-beta 31**
Fix portals not lighting

**0.0.1-beta 30**
Make PortalLinks more safe, prevents some crashes too

**0.0.1-beta 29**
Fixes an infinite loop when searching for a portalbase(thanks #17, Jack-Papel). Also makes CustomPortalBlock:getPortalBase non-static, allowing custom portal blocks to overwrite its logic

**0.0.1-beta 28**
Fix portal nausea effect sometimes flashing back to the purple nether portal instance

**0.0.1-beta 27**
Rename the modid to what it should've been in the first place. This does break existing portals but after a relight they should be fine

**0.0.1-beta 26**
Remove Mod Menu from the dev environment 

**0.0.1-beta 25**
Further improves and simplifies the previous

**0.0.1-beta 24**
Now calls vanilla teleport methods instead of our own modified vanilla methods. Fixes compat with other mods notably Immersive Portals

**0.0.1-beta 23**
More work on syncing and better support for DataPack portals and ServerCustomPortals

**0.0.1-beta 22**
Fixes beta 21

**0.0.1-beta 21**
Adds portal syncing from server to client, requiring only the server to have the portal registered.
Also adds the client side support for restoring features when playing on a server with the server side only version of CustomPortalApi

**0.0.1-beta 20**
Really adds the ability to force a size for the portal this time.

**0.0.1-beta 19**
Adds the ability to force a size for the portal.

**0.0.1-beta 18**
Actually Adds support for custom fluid providers(buckets)

**0.0.1-beta 17**
Adds support for custom fluid providers(buckets)

**0.0.1-beta 16**
Adds PortalIgnitionSource

**0.0.1-beta 15**
Fixes crash on dedicated servers

**0.0.1-beta 14**
Adds error messages if registered portal contains null blocks.
Fix for a load order issue

**0.0.1-beta 13**
Fix for rgb colors being wrong

**0.0.1-beta 12**
Fix issue with duplicate POI's

**0.0.1-beta 11**
Full support for RGB color tinting!


