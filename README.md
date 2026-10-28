# CineFly

A lightweight client-side cinematic free-camera mod for Minecraft Java Edition 1.21.11 with Fabric.

## Current controls

- **Toggle CineFly:** configurable in Minecraft's Controls menu (default: `O`).
- **Move:** `WASD`.
- **Rise / descend:** `Space` / `Left Shift`.
- **Look around:** mouse.
- **Move faster:** `Left Ctrl`.

When enabled, CineFly hides the HUD and switches to a separate camera. The player remains in place. Press the toggle key again to restore the player camera and HUD.

## Build

Use the **Build mod** workflow in GitHub Actions. The compiled JAR is uploaded as a workflow artifact.

## Notes

This is a client-side filming tool. It does not provide server-side flight or bypass server movement rules. The camera can move freely for local filming; multiplayer servers may restrict what can be seen or loaded.
