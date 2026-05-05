# 🟣 Twitch Chat in Minecraft

A Minecraft mod that displays your Twitch chat directly in the game.  
Only you see the messages — other players on the server see nothing.

---

## ✅ What is this for?

You're streaming Minecraft and want to read Twitch chat without alt-tabbing.  
The mod shows messages directly in the Minecraft chat in real time.

```
[mod] viewer123: hello streamer!
[vip] coolguy: gg wp
fanboy99: when is the next stream?
```

---

## 📦 Versions

| Minecraft Version | Loader | Java |
|------------------|--------|------|
| 26.1 | Fabric Loader 0.18.5 | Java 25 |
| 1.21.1 | NeoForge 21.1.172 | Java 21 |

---

## 🔧 Installation

1. Install **Fabric** or **NeoForge** for your Minecraft version
2. Download **Fabric API** and put it in the `mods/` folder
3. Copy the mod file `twitchchat-*.jar` into the `mods/` folder
4. Launch the game

---

## 🎮 Commands

| Command | Description |
|---------|-------------|
| `/twitch channel <name>` | Set and save the Twitch channel |
| `/twitch start` | Start receiving chat |
| `/twitch stop` | Stop |
| `/twitch reload` | Reconnect to the channel |
| `/twitch autostart on/off` | Auto-connect when joining a world |
| `/twitch sound <nick>` | Play a sound when your nick is mentioned |
| `/twitch language ru/en` | Interface language |
| `/twitch status` | Show current status |
| `/twitch help` | List all commands |

> `/twich` also works — in case of a typo.

---

## 🚀 Quick Start

```
/twitch channel yourchannel
/twitch start
```

---

## 🏷️ Badges

The mod automatically shows the user's role in chat:

| Badge | Role |
|-------|------|
| `[streamer]` | Channel broadcaster |
| `[mod]` | Moderator |
| `[vip]` | VIP |
| `[sub]` | Subscriber |
| `[✓]` | Twitch Partner |
| `[prime]` | Prime subscription |
| `[founder]` | Channel founder |
| `[bits]` | Bits cheerer |
| *(empty)* | Regular viewer |

---

## 🔔 Mention Sound

Set your nickname to get a sound notification when someone mentions you:

```
/twitch sound yournick
```

When your nick is mentioned in Twitch chat, a sound will play and the message will be highlighted in game.

---

## 💾 Settings

All settings are saved to `config/twitchchat.json`.  
Channel, autostart, language and mention nick persist across game restarts.

---

## ❓ How it works

The mod connects to Twitch IRC anonymously — **no token or login required**.  
Messages are shown **only to you** — other players see nothing.  
The mod reads chat but **cannot write** to Twitch.

---

## ⚠️ Requirements

- Client-side only — do not install on the server
- Internet connection required
- Channel must be **public**

---

## 📄 License

MIT — do whatever you want.

Author: [@boing_lover](https://twitch.tv/boing_lover)
