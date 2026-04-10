# Weapon Personalities Mod
**Minecraft 1.12.2 — Forge 14.23.5.2855**

Makes every weapon class feel completely different to use — rewarding skilled play
with combos, backstabs, stagger, and range bonuses instead of spam-clicking.

---

## Building

### Prerequisites
| Tool | Minimum version |
|------|-----------------|
| JDK  | 8 (Java 8)      |
| Gradle | 4.10.3 (bundled via wrapper) |

### Steps

```bash
# 1. Clone / download the mod folder
cd weapon-personalities-mod

# 2. Set up the Forge workspace (downloads Minecraft, applies MCP mappings)
./gradlew setupDecompWorkspace

# 3. (Optional) Generate IDE project files
./gradlew eclipse    # Eclipse
./gradlew idea       # IntelliJ IDEA

# 4. Build the mod JAR
./gradlew build

# Output: build/libs/weaponpersonalities-1.0.0.jar
```

Drop the JAR into your `.minecraft/mods/` folder alongside **Forge 1.12.2-14.23.5.2855**.

---

## Weapon Personalities

### Sword — Balanced Fighter
| Mechanic | Detail |
|----------|--------|
| Combo stacks | Up to 3 consecutive hits — each adds +15% damage |
| Combo finisher | 3rd hit: extra knockback burst + magic particle ring |
| Combo reset | Miss or wait > 2 s resets the chain |

### Axe — Heavy Breaker
| Mechanic | Detail |
|----------|--------|
| Shield disable | Hitting a blocking player disables their shield for 5 s |
| Stagger | 35% chance to apply Slowness II for 2 s |
| Sound | Deep low-pitch impact sound on every hit |

### Spear — Reach Weapon
| Mechanic | Detail |
|----------|--------|
| Extended range | Attacks at ~4 block range (vs 3 for swords) |
| Wind-up | Must hold attack for ~0.5 s for full damage |
| Range bonus | +3 damage when striking at maximum range |

### Dagger — Fast Assassin
| Mechanic | Detail |
|----------|--------|
| Backstab | Hitting from behind multiplies damage × 2 |
| Sprint bonus | First hit after sprinting deals +2.5 bonus damage |
| Sound | Sharp high-pitch snappy sound on every hit |

---

## Mastery System
Each weapon type tracks how many times it has been used:
- Every **100 hits** unlocks the next mastery level (max level 5).
- Each level grants **+0.05 flat damage**.
- Mastery data is stored in the player's NBT and survives restarts.
- Max-level hits trigger a golden sparkle particle burst.

---

## Configuration

Edit `config/weaponpersonalities.cfg` after the first launch:

```properties
# Sword
S:comboMultiplierPerStack=0.15
I:comboMaxStacks=3
I:comboResetTicks=40
D:comboFinisherKnockback=1.5

# Axe
D:shieldDisableDamageBonus=5.0
I:shieldDisableDurationTicks=100
D:staggerChance=0.35

# Spear
D:rangeBonusDamage=3.0
I:windUpTicks=10

# Dagger
D:backstabMultiplier=2.0
D:sprintBonusDamage=2.5

# Mastery
B:enabled=true
I:hitsPerLevel=100
I:maxLevel=5
D:damageBonus=0.05
```

All values are validated on load and clamped to safe ranges.

---

## Code Structure

```
src/main/java/com/weaponpersonalities/
├── WeaponPersonalitiesMod.java        Main mod class, lifecycle hooks
├── config/
│   └── ModConfig.java                 Forge config – all tunable values
├── systems/
│   ├── WeaponType.java                Enum + detection logic (tag / class / name)
│   ├── ComboSystem.java               Sword combo tracking & mastery progress
│   └── WeaponBehaviorHandler.java     Per-weapon-type combat mechanics
├── effects/
│   └── EffectsSystem.java             Particles & sounds (all types)
└── events/
    └── CombatEventHandler.java        Forge event subscriptions
```

Each system is isolated — adding a new weapon type only requires:
1. Adding a case to `WeaponType.java`
2. Adding a `handleMyWeapon()` in `WeaponBehaviorHandler.java`
3. Wiring it in the `switch` in `handleAttack()`

---

## Compatibility
- Works with any mod that extends `ItemSword` or `ItemAxe`.
- Custom spears/daggers: use `WeaponType.tag(stack, WeaponType.SPEAR)` or name the
  item registry path to include "spear"/"dagger".
- No conflicts with other combat overhaul mods as long as they don't cancel
  `LivingHurtEvent` at HIGH priority.
