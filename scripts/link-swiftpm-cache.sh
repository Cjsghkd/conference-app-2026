#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"

# Every working tree of a clone shares this directory, and deleting the clone takes the store with
# it. git keeps nothing else in there that a name of ours could collide with.
common_dir="$(git -C "$root" rev-parse --git-common-dir 2>/dev/null || true)"
case "$common_dir" in
  "") common_dir="$HOME/Library/Caches/droidkaigi-conference-app-2026" ;;
  /*) ;;
  *) common_dir="$root/$common_dir" ;;
esac
store="${SWIFTPM_IMPORT_CACHE:-$common_dir/swiftpm-import}"

usage() {
  echo "Usage: scripts/link-swiftpm-cache.sh [--store <dir>] [--install-hook | --gc]" >&2
  exit 1
}

install_hook=false
gc=false
while [ $# -gt 0 ]; do
  case "$1" in
    --store) [ $# -ge 2 ] || usage; store="$2"; shift 2 ;;
    --install-hook) install_hook=true; shift ;;
    --gc) gc=true; shift ;;
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

if [ "$gc" = true ]; then
  [ -d "$store_root" ] || { echo "No store at $store_root."; exit 0; }

  # git has no hook for `git worktree remove`, so buckets are collected by asking which ones the
  # working trees that still exist point at.
  live=""
  while IFS= read -r line; do
    case "$line" in worktree\ *) ;; *) continue ;; esac
    tree="${line#worktree }"
    for relative in \
      ".swiftpm-locks/default/swiftPMCheckout" \
      "app-shared/build/kotlin/swiftPMCheckout"
    do
      [ -L "$tree/$relative" ] || continue
      destination="$(readlink "$tree/$relative")"
      case "$destination" in
        "$store_root"/*) live="$live $(basename "$(dirname "$destination")")" ;;
      esac
    done
  done < <(git -C "$root" worktree list --porcelain)

  removed=0
  for bucket_dir in "$store_root"/*/; do
    [ -d "$bucket_dir" ] || continue
    name="$(basename "$bucket_dir")"
    case " $live " in
      *" $name "*) echo "  in use: $name ($(du -sh "$bucket_dir" | cut -f1))" ;;
      *) echo "  removing: $name ($(du -sh "$bucket_dir" | cut -f1))"; rm -rf "$bucket_dir"; removed=$((removed + 1)) ;;
    esac
  done
  echo "Removed $removed bucket(s) from $store_root."
  exit 0
fi

# SwiftPM resolves against the store, so working trees that share one must agree on what they
# declare. The digest covers the manifests only: Package.resolved is rewritten by resolution, and
# keying on it would move a working tree to another bucket whenever a version moved.
manifests="$(find "$root/.swiftpm-locks" -name Package.swift 2>/dev/null | LC_ALL=C sort)"
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
      # The bucket may have been collected since; a dangling link fails the build with a
      # missing-header error far from the cause.
      mkdir -p "$shared"
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

# The def and ld files name paths inside the checkouts, and the task producing them tracks only the
# manifests, so Gradle keeps them even once an emptied bucket has taken those paths away. Left
# behind, they fail the build at the linker with a missing file far from the cause. The derived data
# goes with them: it is built from the same checkouts and is cheap to rebuild.
if [ -z "$(ls -A "$store/app-shared-checkout" 2>/dev/null)" ]; then
  rm -rf "$root/app-shared/build/kotlin/swiftImportDefs" \
    "$root/app-shared/build/kotlin/swiftImportLdDump" \
    "$root/app-shared/build/kotlin/swiftImportClangDump" \
    "$root/app-shared/build/kotlin/swiftImportDd"
fi

echo "Shared store: $store"
for path in "${adopted[@]:-}"; do [ -n "$path" ] && echo "  moved into the store: $path"; done
for path in "${created[@]:-}"; do [ -n "$path" ] && echo "  linked: $path"; done
for path in "${kept[@]:-}"; do [ -n "$path" ] && echo "  already linked: $path"; done
