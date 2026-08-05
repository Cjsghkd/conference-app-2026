#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
store="${SWIFTPM_IMPORT_CACHE:-$HOME/.cache/droidkaigi-conference-app-2026/swiftpm-import}"

usage() {
  echo "Usage: scripts/link-swiftpm-cache.sh [--store <dir>] [--install-hook]" >&2
  exit 1
}

install_hook=false
while [ $# -gt 0 ]; do
  case "$1" in
    --store) [ $# -ge 2 ] || usage; store="$2"; shift 2 ;;
    --install-hook) install_hook=true; shift ;;
    *) usage ;;
  esac
done

# ln -s resolves a relative target from the link's own directory, so the store must be absolute.
case "$store" in
  /*) ;;
  *) store="$PWD/$store" ;;
esac
store_root="$store"

if [ "$(uname -s)" != "Darwin" ]; then
  echo "Swift Package Manager import runs on macOS only; nothing to link." >&2
  exit 0
fi

if [ "$install_hook" = true ]; then
  common_dir="$(git -C "$root" rev-parse --git-common-dir)"
  case "$common_dir" in /*) ;; *) common_dir="$root/$common_dir" ;; esac
  hook="$common_dir/hooks/post-checkout"

  if [ -e "$hook" ]; then
    echo "$hook already exists; add the call to it by hand." >&2
    exit 1
  fi

  mkdir -p "$(dirname "$hook")"
  cat > "$hook" <<'HOOK'
#!/bin/sh
# git runs this in the new working tree after `git worktree add` and `git clone`, passing the
# all-zero ref as the previous HEAD. Ordinary checkouts pass a real ref and are skipped.
case "$1" in *[!0]*) exit 0 ;; esac
[ "$3" = "1" ] || exit 0
[ -x ./scripts/link-swiftpm-cache.sh ] || exit 0

# The hook's exit status becomes the exit status of the git command, so a failure here must not
# fail `git worktree add`.
./scripts/link-swiftpm-cache.sh || echo "Run scripts/link-swiftpm-cache.sh by hand to link this working tree." >&2
exit 0
HOOK
  chmod +x "$hook"
  echo "Installed $hook"
  echo "Every working tree added from this clone links itself from now on."
  exit 0
fi

# SwiftPM resolves against the store, so working trees that share one must agree on the resolution.
# Bucketing by the manifests and the lock file keeps a branch that changes dependencies out of the
# bucket the others use, which would otherwise rewrite their Package.resolved.
manifests="$(find "$root/.swiftpm-locks" \( -name Package.swift -o -name Package.resolved \) 2>/dev/null | LC_ALL=C sort)"
if [ -z "$manifests" ]; then
  echo "No SwiftPM manifest under .swiftpm-locks; nothing to link." >&2
  exit 0
fi
bucket="$(printf '%s\n' "$manifests" | xargs cat | shasum -a 256 | cut -c1-12)"
store="$store/$bucket"

created=()
adopted=()
kept=()

link() {
  local relative="$1" name="$2"
  local target="$root/$relative"
  local shared="$store/$name"

  if [ -L "$target" ]; then
    local current
    current="$(readlink "$target")"
    if [ "$current" = "$shared" ]; then
      kept+=("$relative")
      return
    fi
    # A link into another bucket is this working tree's own, left over from an earlier dependency set.
    case "$current" in
      "$store_root"/*) rm "$target" ;;
      *) echo "$relative already links to $current; remove it and re-run." >&2; exit 1 ;;
    esac
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
