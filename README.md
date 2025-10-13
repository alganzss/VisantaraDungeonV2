# VDungeon Plugin

Plugin Minecraft untuk membuat sistem dungeon dengan hologram sebagai entrance. Setiap dungeon memiliki world terpisah dan player tidak bisa melihat player lain di dalam dungeon.

## 📋 Fitur

- ✅ Create dungeon dengan world terpisah otomatis
- ✅ Hologram interaktif untuk masuk dungeon
- ✅ System isolasi player (invisible satu sama lain di dungeon)
- ✅ Teleport kembali ke lokasi awal saat exit
- ✅ Multiple dungeon support
- ✅ Customizable hologram text dengan color codes
- ✅ Persistent data (tersimpan saat restart)

## 📁 Struktur Folder

```
VDungeon/
├── src/main/java/com/yourname/vdungeon/
│   ├── VDungeon.java                    # Main class
│   ├── commands/
│   │   └── VDungeonCommand.java         # Command handler
│   ├── listeners/
│   │   ├── HologramListener.java        # Hologram click handler
│   │   └── PlayerVisibilityListener.java # Player visibility handler
│   ├── managers/
│   │   ├── DungeonManager.java          # Dungeon management
│   │   ├── HologramManager.java         # Hologram management
│   │   └── PlayerManager.java           # Player data management
│   └── models/
│       ├── Dungeon.java                 # Dungeon model
│       └── Hologram.java                # Hologram model
├── src/main/resources/
│   ├── plugin.yml                       # Plugin metadata
│   └── config.yml                       # Configuration file
└── pom.xml                              # Maven configuration
```

## 🚀 Instalasi

### Requirement
- Spigot/Paper 1.19.4 atau lebih tinggi
- Java 8 atau lebih tinggi
- Maven (untuk compile)

### Compile Plugin

1. Clone atau download source code
2. Buka terminal di folder plugin
3. Jalankan command:
```bash
mvn clean package
```
4. File JAR akan ada di folder `target/VDungeon-1.0.0.jar`
5. Copy file JAR ke folder `plugins/` server Anda
6. Restart server

## 📝 Commands

### Dungeon Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/vdungeon create <nama>` | Membuat dungeon baru | `vdungeon.create` |
| `/vdungeon delete <nama>` | Menghapus dungeon | `vdungeon.delete` |
| `/vdungeon play <nama>` | Masuk ke dungeon | `vdungeon.play` |
| `/vdungeon exit` | Keluar dari dungeon | `vdungeon.exit` |
| `/vdungeon list` | List semua dungeon | `vdungeon.list` |
| `/vdungeon setspawn <nama>` | Set spawn point dungeon | `vdungeon.setspawn` |

### Hologram Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/vdungeon hologram create <id> <dungeon> <text...>` | Buat hologram | `vdungeon.hologram.create` |
| `/vdungeon hologram delete <id>` | Hapus hologram | `vdungeon.hologram.delete` |
| `/vdungeon hologram list` | List semua hologram | `vdungeon.hologram.list` |

## 🎮 Cara Penggunaan

### 1. Membuat Dungeon

```
/vdungeon create Boss1
```

Ini akan:
- Create world baru bernama `Boss1_dungeon`
- Set spawn point default
- Generate world dengan type NORMAL

### 2. Set Spawn Point Dungeon

```
/vdungeon setspawn Boss1
```

Berdiri di lokasi yang Anda inginkan sebagai spawn point, lalu jalankan command di atas.

### 3. Membuat Hologram Entrance

```
/vdungeon hologram create portal1 Boss1 &e&lKLIK_UNTUK_MASUK &7Boss_Dungeon_Level_1
```

**Catatan:**
- Gunakan `_` untuk spasi dalam text
- Gunakan color codes Minecraft (`&a`, `&e`, `&c`, dll)
- Hologram akan muncul di lokasi player saat ini

### 4. Masuk ke Dungeon

Ada 2 cara:
- **Klik hologram** yang sudah dibuat
- **Gunakan command** `/vdungeon play Boss1`

### 5. Keluar dari Dungeon

```
/vdungeon exit
```

Player akan teleport kembali ke lokasi sebelum masuk dungeon.

## 🎨 Color Codes

Gunakan color codes Minecraft untuk styling text:

| Code | Color | Code | Color |
|------|-------|------|-------|
| `&0` | Black | `&8` | Dark Gray |
| `&1` | Dark Blue | `&9` | Blue |
| `&2` | Dark Green | `&a` | Green |
| `&3` | Dark Aqua | `&b` | Aqua |
| `&4` | Dark Red | `&c` | Red |
| `&5` | Dark Purple | `&d` | Light Purple |
| `&6` | Gold | `&e` | Yellow |
| `&7` | Gray | `&f` | White |

### Format Codes
- `&l` - Bold
- `&m` - Strikethrough
- `&n` - Underline
- `&o` - Italic
- `&r` - Reset

## ⚙️ Configuration

Edit `plugins/VDungeon/config.yml`:

```yaml
settings:
  hide-players-in-dungeon: true    # Sembunyikan player lain di dungeon
  teleport-back-on-exit: true      # Teleport balik saat exit
  world-type: NORMAL               # Type world dungeon
  world-environment: NORMAL        # Environment world

hologram:
  default-lines:
    - "&e&lCLICK TO ENTER"
    - "&7Dungeon Portal"
  line-spacing: 0.25               # Jarak antar baris hologram

dungeon:
  default-spawn-y: 100
  disable-daylight-cycle: true
  disable-weather-cycle: true
  fixed-time: 6000                 # 6000 = siang
```

## 🔐 Permissions

### Admin Permissions
```yaml
vdungeon.*                  # Semua permission
vdungeon.create             # Create dungeon
vdungeon.delete             # Delete dungeon
vdungeon.setspawn           # Set spawn point
vdungeon.hologram.*         # Semua hologram commands
vdungeon.hologram.create    # Create hologram
vdungeon.hologram.delete    # Delete hologram
```

### Player Permissions
```yaml
vdungeon.play               # Masuk dungeon
vdungeon.exit               # Exit dungeon
vdungeon.list               # List dungeon
vdungeon.hologram.list      # List hologram
```

## 📦 Data Storage

Plugin menyimpan data di:
- `plugins/VDungeon/dungeons.yml` - Data dungeon
- `plugins/VDungeon/holograms.yml` - Data hologram
- `plugins/VDungeon/config.yml` - Konfigurasi

## 🔧 Troubleshooting

### Hologram tidak muncul
1. Check apakah dungeon sudah dibuat
2. Pastikan world tidak unload
3. Reload plugin atau restart server

### Player masih bisa melihat player lain di dungeon
1. Check `config.yml`, pastikan `hide-players-in-dungeon: true`
2. Restart server
3. Check apakah ada plugin lain yang conflict

### World dungeon tidak generate
1. Check console untuk error
2. Pastikan folder `worlds/` bisa diwrite
3. Check RAM server cukup

### Player tidak bisa exit dungeon
1. Gunakan command `/vdungeon exit`
2. Jika stuck, teleport manual dengan `/tp`

## 🤝 Support

Jika ada bug atau pertanyaan:
1. Check documentation ini dulu
2. Check console untuk error messages
3. Test dengan plugin minimal (disable plugin lain)

## 📄 License

Plugin ini free untuk digunakan dan dimodifikasi.

## 🔄 Update Log

### Version 1.0.0
- Initial release
- Basic dungeon system
- Hologram entrance
- Player isolation feature
- Multi-dungeon support
