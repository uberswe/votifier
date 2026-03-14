#!/usr/bin/env bash
set -euo pipefail

# Verify built JARs contain expected class files and loader metadata.
# Reads settings.gradle to determine which loaders are active on this branch.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

REQUIRED_CLASSES=(
    "com/uberswe/votifier/Votifier.class"
    "com/uberswe/votifier/VotifierServer.class"
    "com/uberswe/votifier/VotifierConfig.class"
    "com/uberswe/votifier/RSAKeyManager.class"
    "com/uberswe/votifier/VoteStorage.class"
    "com/uberswe/votifier/Vote.class"
    "com/uberswe/votifier/Constants.class"
)

errors=0

check_jar() {
    local jar="$1"
    local loader="$2"
    local jar_contents

    echo "  Checking $jar ..."

    if [ ! -f "$jar" ]; then
        echo "  ERROR: JAR not found: $jar"
        errors=$((errors + 1))
        return
    fi

    jar_contents=$(jar tf "$jar")

    # Check required class files
    for class in "${REQUIRED_CLASSES[@]}"; do
        if ! echo "$jar_contents" | grep -q "^${class}$"; then
            echo "  ERROR: Missing class $class in $jar"
            errors=$((errors + 1))
        fi
    done

    # Check loader-specific metadata
    case "$loader" in
        forge)
            if ! echo "$jar_contents" | grep -q "^META-INF/mods.toml$"; then
                echo "  ERROR: Missing META-INF/mods.toml in $jar"
                errors=$((errors + 1))
            else
                local mod_id
                mod_id=$(unzip -p "$jar" META-INF/mods.toml 2>/dev/null)
                if ! echo "$mod_id" | grep -q "votifier"; then
                    echo "  ERROR: mods.toml does not contain 'votifier' mod ID"
                    errors=$((errors + 1))
                fi
            fi
            ;;
        neoforge)
            if ! echo "$jar_contents" | grep -q "^META-INF/neoforge.mods.toml$"; then
                echo "  ERROR: Missing META-INF/neoforge.mods.toml in $jar"
                errors=$((errors + 1))
            else
                local mod_id
                mod_id=$(unzip -p "$jar" META-INF/neoforge.mods.toml 2>/dev/null)
                if ! echo "$mod_id" | grep -q "votifier"; then
                    echo "  ERROR: neoforge.mods.toml does not contain 'votifier' mod ID"
                    errors=$((errors + 1))
                fi
            fi
            ;;
        fabric)
            if ! echo "$jar_contents" | grep -q "^fabric.mod.json$"; then
                echo "  ERROR: Missing fabric.mod.json in $jar"
                errors=$((errors + 1))
            else
                local mod_json
                mod_json=$(unzip -p "$jar" fabric.mod.json 2>/dev/null)
                if ! echo "$mod_json" | grep -q "votifier"; then
                    echo "  ERROR: fabric.mod.json does not contain 'votifier' mod ID"
                    errors=$((errors + 1))
                fi
                if ! echo "$mod_json" | grep -q "server"; then
                    echo "  ERROR: fabric.mod.json does not declare a server entrypoint"
                    errors=$((errors + 1))
                fi
            fi
            ;;
    esac
}

find_jar() {
    local dir="$1"
    if [ -d "$dir" ]; then
        find "$dir" -maxdepth 1 -name '*.jar' ! -name '*-sources.jar' | head -1
    fi
}

# Read settings.gradle to determine active loaders
settings_file="$PROJECT_ROOT/settings.gradle"
if [ ! -f "$settings_file" ]; then
    echo "ERROR: settings.gradle not found at $settings_file"
    exit 1
fi

checked=0

# Check each loader
for loader in forge neoforge fabric; do
    if grep -q "include.*'$loader'" "$settings_file"; then
        echo "Loader '$loader' is included in settings.gradle"
        jar=$(find_jar "$PROJECT_ROOT/$loader/build/libs")
        if [ -n "$jar" ]; then
            check_jar "$jar" "$loader"
            checked=$((checked + 1))
        else
            echo "  WARNING: No JAR found for $loader (build may not produce one on this branch)"
        fi
    else
        echo "Loader '$loader' is not included in settings.gradle, skipping"
    fi
done

echo ""
if [ "$checked" -eq 0 ]; then
    echo "ERROR: No JARs were checked!"
    exit 1
fi

if [ "$errors" -gt 0 ]; then
    echo "FAILED: $errors error(s) found"
    exit 1
fi

echo "OK: All $checked JAR(s) verified successfully"
