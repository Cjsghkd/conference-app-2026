import AppShared
import ConferenceApp2026AppShared
import SwiftUI
import UIKit

/// Swift Export flattens only the package the exported module names, so the types it reaches in
/// :app-shared keep their fully qualified Kotlin package.
typealias RootTab = ExportedKotlinPackages.io.github.droidkaigi.confsched.app.RootTab
typealias RootTabBarPalette = ExportedKotlinPackages.io.github.droidkaigi.confsched.app.RootTabBarPalette

/// The root tab bar, laid out over the Compose view controller. UIKit sizes the bar to itself, so
/// the view occupies the bar's own area and every point outside it belongs to the Compose layer.
struct RootTabBarView<Selections: AsyncSequence, Palettes: AsyncSequence>: View
where Selections.Element == RootTab?, Palettes.Element == RootTabBarPalette? {
    let currentTab: Selections
    let palette: Palettes
    let select: (RootTab) -> Void

    @State private var selection: RootTab?
    @State private var appearance: RootTabBarPalette?

    var body: some View {
        SystemTabBar(currentTab: selection, palette: appearance, onSelect: select)
            .task {
                try? await collectCurrentTab()
            }
            .task {
                try? await collectPalette()
            }
    }

    private func collectCurrentTab() async throws {
        for try await tab in currentTab {
            selection = tab
        }
    }

    private func collectPalette() async throws {
        for try await palette in palette {
            appearance = palette
        }
    }
}

private struct SystemTabBar: UIViewRepresentable {
    let currentTab: RootTab?
    let palette: RootTabBarPalette?
    let onSelect: (RootTab) -> Void

    func makeUIView(context: Context) -> UITabBar {
        let bar = UITabBar()
        bar.delegate = context.coordinator
        bar.items = RootTab.allCases.enumerated().map { index, tab in
            UITabBarItem(title: tab.label, image: UIImage(systemName: tab.symbolName), tag: index)
        }
        return bar
    }

    func updateUIView(_ bar: UITabBar, context: Context) {
        context.coordinator.onSelect = onSelect
        // The selection arrives before the palette: the navigator holds a tab from the start, while
        // the palette waits for the first composition. Showing the bar in between would put the
        // system tint on it for a frame, so it waits for both.
        bar.isHidden = currentTab == nil || palette == nil
        // A hidden bar keeps the selection it had, so returning from a detail screen does not
        // replay the selection animation.
        if let index = currentTab.flatMap({ RootTab.allCases.firstIndex(of: $0) }) {
            bar.selectedItem = bar.items?[index]
        }
        if let palette {
            bar.tintColor = UIColor(argb: palette.accentArgb)
            // The theme is the app's own, not the system's, so the bar's material follows it
            // rather than the device's appearance setting.
            bar.overrideUserInterfaceStyle = palette.isDark ? .dark : .light
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    final class Coordinator: NSObject, UITabBarDelegate {
        var onSelect: ((RootTab) -> Void)?

        func tabBar(_ tabBar: UITabBar, didSelect item: UITabBarItem) {
            guard RootTab.allCases.indices.contains(item.tag) else { return }
            onSelect?(RootTab.allCases[item.tag])
        }
    }
}

private extension UIColor {
    convenience init(argb: Int64) {
        func channel(_ shift: Int64) -> CGFloat {
            CGFloat((argb >> shift) & 0xFF) / 255
        }
        self.init(red: channel(16), green: channel(8), blue: channel(0), alpha: channel(24))
    }
}

private extension RootTab {
    /// The Compose bar names each destination with a Material `ImageVector`, which does not cross
    /// the framework boundary; the SF Symbol standing for the same destination lives here.
    var symbolName: String {
        switch self {
        case .Timetable: return "calendar"
        case .EventMap: return "map"
        case .Favorites: return "heart.fill"
        case .About: return "info.circle"
        case .ProfileCard: return "person.crop.circle"
        default: return "questionmark"
        }
    }
}
