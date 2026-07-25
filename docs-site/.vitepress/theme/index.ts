import DefaultTheme from 'vitepress/theme'
import './custom.css'
import { useRoute } from 'vitepress'
import { nextTick, onMounted, watch } from 'vue'
import mermaid from 'mermaid'
import elkLayouts from '@mermaid-js/layout-elk'

// Register the ELK layout engine on the shared mermaid singleton (the same instance
// vitepress-plugin-mermaid renders with). Diagrams opt in via a `config: { layout: elk }`
// front-matter block; the module dependency graph uses it because ELK routes edges with
// far fewer crossings than the default dagre layout.
if (typeof window !== 'undefined') {
  mermaid.registerLayoutLoaders(elkLayouts)
}

// Zoom for mermaid diagrams. The inline SVG is never mutated (so existing diagrams
// keep rendering exactly as before); each diagram gets a small button that opens an
// overlay containing a *clone* of the SVG with interactive pan / zoom. All zoom
// behaviour is confined to that overlay.

function ctrlButton(label: string, title: string) {
  const b = document.createElement('button')
  b.type = 'button'
  b.className = 'mermaid-zoom-ctrl'
  b.title = title
  b.setAttribute('aria-label', title)
  b.textContent = label
  return b
}

async function openOverlay(sourceSvg: SVGSVGElement) {
  const overlay = document.createElement('div')
  overlay.className = 'mermaid-zoom-overlay'

  const stage = document.createElement('div')
  stage.className = 'mermaid-zoom-stage'
  // Keep the id: mermaid scopes the SVG's internal <style> to `#<id>`, so removing
  // it would drop every fill/stroke and the diagram renders as black boxes.
  const clone = sourceSvg.cloneNode(true) as SVGSVGElement
  clone.style.maxWidth = 'none'
  clone.style.width = '100%'
  clone.style.height = '100%'
  stage.appendChild(clone)
  overlay.appendChild(stage)

  const controls = document.createElement('div')
  controls.className = 'mermaid-zoom-controls'
  const zoomIn = ctrlButton('+', 'Zoom in')
  const zoomOut = ctrlButton('−', 'Zoom out')
  const reset = ctrlButton('↻', 'Reset')
  const close = ctrlButton('✕', 'Close')
  controls.append(zoomIn, zoomOut, reset, close)
  overlay.appendChild(controls)

  document.body.appendChild(overlay)
  document.body.style.overflow = 'hidden'

  const { default: svgPanZoom } = await import('svg-pan-zoom')
  const instance = svgPanZoom(clone, {
    zoomEnabled: true,
    panEnabled: true,
    controlIconsEnabled: false,
    mouseWheelZoomEnabled: true,
    dblClickZoomEnabled: true,
    fit: true,
    center: true,
    minZoom: 0.3,
    maxZoom: 20,
  })

  const destroy = () => {
    try {
      instance.destroy()
    } catch {
      /* ignore */
    }
    overlay.remove()
    document.body.style.overflow = ''
    document.removeEventListener('keydown', onKey)
  }
  const onKey = (e: KeyboardEvent) => {
    if (e.key === 'Escape') destroy()
  }

  zoomIn.addEventListener('click', () => instance.zoomIn())
  zoomOut.addEventListener('click', () => instance.zoomOut())
  reset.addEventListener('click', () => {
    instance.resetZoom()
    instance.fit()
    instance.center()
  })
  close.addEventListener('click', destroy)
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) destroy()
  })
  document.addEventListener('keydown', onKey)
}

function addZoomButtons() {
  if (typeof window === 'undefined') return
  let tries = 0
  const run = () => {
    const containers = Array.from(
      document.querySelectorAll<HTMLElement>('.mermaid'),
    ).filter((c) => c.querySelector('svg'))
    // mermaid renders asynchronously — retry until the SVGs are in the DOM.
    if (containers.length === 0 && tries++ < 50) {
      window.setTimeout(run, 100)
      return
    }
    for (const c of containers) {
      if (c.dataset.zoomReady) continue
      c.dataset.zoomReady = '1'
      const btn = document.createElement('button')
      btn.className = 'mermaid-zoom-btn'
      btn.type = 'button'
      btn.title = 'Zoom'
      btn.setAttribute('aria-label', 'Zoom diagram')
      // magnifier glyph
      btn.textContent = '⌕'
      btn.addEventListener('click', (e) => {
        e.stopPropagation()
        const svg = c.querySelector('svg') as SVGSVGElement | null
        if (svg) openOverlay(svg)
      })
      c.appendChild(btn)
    }
  }
  run()
}

export default {
  extends: DefaultTheme,
  setup() {
    const route = useRoute()
    onMounted(() => nextTick(addZoomButtons))
    watch(
      () => route.path,
      () => nextTick(() => window.setTimeout(addZoomButtons, 120)),
    )
  },
}
