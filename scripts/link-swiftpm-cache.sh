#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
store="${SWIFTPM_IMPORT_CACHE:-$HOME/.cache/droidkaigi-conference-app-2026/swiftpm-import}"

while [ $# -gt 0 ]; do
  case "$1" in
    --store) store="$2"; shift 2 ;;
    *) echo "Usage: scripts/link-swiftpm-cache.sh [--store <dir>]" >&2; exit 1 ;;
  esac
done

if [ "$(uname -s)" != "Darwin" ]; then
  echo "Swift Package Manager import runs on macOS only; nothing to link." >&2
  exit 0
fi

created=()
adopted=()
kept=()

link() {
  local relative="$1" name="$2"
  local target="$root/$relative"
  local shared="$store/$name"

  if [ -L "$target" ]; then
    if [ "$(readlink "$target")" = "$shared" ]; then
      kept+=("$relative")
      return
    fi
    echo "$relative already links to $(readlink "$target"); remove it and re-run." >&2
    exit 1
  fi

  if [ -e "$target" ]; then
    if [ -d "$shared" ] && [ -n "$(ls -A "$shared")" ]; then
      echo "$relative holds local output and $shared is not empty; delete one of them and re-run." >&2
      exit 1
    fi
    rm -rf "$shared"
    mkdir -p "$(dirname "$shared")"
    mv "$target" "$shared"
    adopted+=("$relative")
  else
    mkdir -p "$shared"
  fi

  mkdir -p "$(dirname "$target")"
  ln -s "$shared" "$target"
  created+=("$relative")
}

# The `default` segment is the packageResolvedSynchronization identifier that
# app-shared/build.gradle.kts leaves at its default value.
link ".swiftpm-locks/default/swiftPMCheckout" "umbrella-checkout"
link "app-shared/build/kotlin/swiftPMCheckout" "app-shared-checkout"
link "app-shared/build/kotlin/swiftImportDd" "app-shared-derived-data"

echo "Shared store: $store"
for path in "${adopted[@]:-}"; do [ -n "$path" ] && echo "  moved into the store: $path"; done
for path in "${created[@]:-}"; do [ -n "$path" ] && echo "  linked: $path"; done
for path in "${kept[@]:-}"; do [ -n "$path" ] && echo "  already linked: $path"; done
echo
echo "Sync one working tree at a time: xcodebuild does not support concurrent use of a single derived data directory."
