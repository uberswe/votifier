<h1 align="center">Votifier</h1>

<p align="center">
  A server-side Minecraft mod that receives votes from server list websites like
  <a href="https://createmodservers.com">createmodservers.com</a> and rewards players with configurable commands.
</p>

<p align="center">
  <a href="https://github.com/uberswe/votifier/actions/workflows/build.yml"><img src="https://github.com/uberswe/votifier/actions/workflows/build.yml/badge.svg" alt="Build"></a>
  <a href="https://github.com/uberswe/votifier/releases/latest"><img src="https://img.shields.io/github/v/release/uberswe/votifier?include_prereleases&sort=semver&logo=github" alt="GitHub Release"></a>
  <a href="https://github.com/uberswe/votifier/blob/main/LICENSE.txt"><img src="https://img.shields.io/badge/license-MIT-blue" alt="License"></a>
</p>

---

## Features

- **Votifier v1 support** — classic RSA-encrypted vote protocol
- **NuVotifier v2 support** — modern HMAC-SHA256 signed vote protocol
- **Pending rewards** — votes are stored and commands execute when the player joins
- **Configurable commands** — run any command with `{player}` placeholder on vote
- **Auto-generated keys** — RSA keypair and v2 token are created on first run
- **Multi-loader** — supports Forge, NeoForge, and Fabric across multiple Minecraft versions

## Side

This is a **server-side only** mod. It does not need to be installed on the client.

## Supported Versions

| Branch | Minecraft | Loaders | Java |
|--------|-----------|---------|------|
| `mc/1.21.1` | 1.21.1 | NeoForge, Fabric | 21 |
| `mc/1.20.1` | 1.20.1 | Forge, Fabric | 17 |
| `mc/1.19.2` | 1.19.2 | Forge, Fabric | 17 |
| `main` / `mc/1.18.2` | 1.18.2 | Forge, Fabric | 17 |

## Installation

1. Install the mod loader for your Minecraft version (NeoForge, Forge, or Fabric)
2. For Fabric: install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop the Votifier `.jar` for your loader into your server's `mods/` folder
4. Start the server — config files and RSA keys are generated automatically
5. Copy the public key and token from the server log into your server list website

## Configuration

Config files are located at `config/votifier/`.

### `config.json`

| Option | Default | Description |
|---|---|---|
| `host` | `0.0.0.0` | Address to bind the vote listener to |
| `port` | `8192` | Port to listen for incoming votes |
| `token` | *(auto-generated)* | NuVotifier v2 HMAC token |
| `commands` | `["say {player} voted on createmodservers.com!"]` | Commands to run when a vote is received |

The `{player}` placeholder is replaced with the voter's username.

### Other files

| File | Description |
|---|---|
| `public.pem` | RSA public key — paste this into your server list website |
| `private.pem` | RSA private key — used to decrypt v1 votes |
| `pending_votes.json` | Pending vote rewards for offline players |

## How It Works

1. Register your server on a server list website (e.g. [createmodservers.com](https://createmodservers.com))
2. Paste your **public key** (from `config/votifier/public.pem` or the server log) and set the Votifier **port** on the website
3. For NuVotifier v2: also paste the **token** from `config/votifier/config.json` or the server log
4. When a player votes, the website sends the vote to your server
5. If the player is online, the configured commands run immediately
6. If the player is offline, the vote is saved and commands run when they next join

## Building from Source

```bash
git clone https://github.com/uberswe/votifier.git
cd votifier
git checkout mc/1.18.2   # or mc/1.19.2, mc/1.20.1, mc/1.21.1
./gradlew build
```

JARs are produced per loader:
- `forge/build/libs/` — Forge JAR (1.18.2–1.20.1)
- `neoforge/build/libs/` — NeoForge JAR (1.21.1)
- `fabric/build/libs/` — Fabric JAR

## Project Structure

This is a multi-loader project following the [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template) pattern:

```
common/    - Shared code (vote server, config, RSA, storage)
forge/     - Forge entry point and events (1.18.2–1.20.1)
neoforge/  - NeoForge entry point and events (1.21.1)
fabric/    - Fabric entry point and events
```

## License

This project is licensed under the MIT License. See [LICENSE.txt](LICENSE.txt) for details.
