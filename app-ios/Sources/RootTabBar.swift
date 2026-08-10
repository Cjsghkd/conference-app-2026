import AppShared
import ConferenceApp2026AppShared
import SwiftUI

/// Swift Export flattens only the package the exported module names, so the types it reaches in
/// :app-shared keep their fully qualified Kotlin package.
typealias RootTab = ExportedKotlinPackages.io.github.droidkaigi.confsched.app.RootTab

/// The native root tab bar, laid out over the Compose view controller. The view occupies the bar's
/// own area and nothing more, so every point outside it belongs to the Compose layer below.
struct RootTabBarView: View {
    let host: KaigiAppHost

    @Namespace private var indicator
    @State private var currentTab: RootTab?

    var body: some View {
        Group {
            if let currentTab {
                HStack(spacing: 0) {
                    ForEach(RootTab.allCases, id: \.self) { tab in
                        RootTabButton(
                            tab: tab,
                            isSelected: tab == currentTab,
                            indicator: indicator,
                            select: { host.selectTab(tab: tab) }
                        )
                    }
                }
                .padding(.horizontal, RootTabBarMetrics.innerPadding)
                .frame(maxWidth: RootTabBarMetrics.maxWidth)
                .frame(height: RootTabBarMetrics.height)
                .rootTabBarSurface()
                .padding(.horizontal, RootTabBarMetrics.outerPadding)
                .padding(.bottom, RootTabBarMetrics.bottomMargin)
                .animation(.snappy(duration: 0.25), value: currentTab)
            } else {
                // SwiftUI installs no task on a body that resolves to nothing, and the bar would
                // then never collect a tab to show. This keeps a node in the graph, taking no room.
                Color.clear.frame(width: 0, height: 0)
            }
        }
        .task {
            try? await collectCurrentTab()
        }
    }

    private func collectCurrentTab() async throws {
        for try await selection in host.currentTab.asAsyncSequence() {
            currentTab = selection?.tab
        }
    }
}

private struct RootTabButton: View {
    let tab: RootTab
    let isSelected: Bool
    let indicator: Namespace.ID
    let select: () -> Void

    var body: some View {
        Button(action: select) {
            Image(systemName: tab.symbolName)
                .font(.system(size: RootTabBarMetrics.iconSize))
                .foregroundStyle(isSelected ? Color.white : Color.primary)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background {
                    if isSelected {
                        Circle()
                            .fill(Color.accentColor)
                            .frame(
                                width: RootTabBarMetrics.indicatorSize,
                                height: RootTabBarMetrics.indicatorSize
                            )
                            .matchedGeometryEffect(id: "indicator", in: indicator)
                    }
                }
                .contentShape(.rect)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(tab.label)
        .accessibilityAddTraits(isSelected ? [.isButton, .isSelected] : .isButton)
    }
}

/// Mirrors `KaigiNavigationBarDefaults`, which the Compose bar lays itself out from. Compose `Dp`
/// and points coincide on iOS, so a scrollable's reserved `occupiedHeight` clears this bar exactly.
private enum RootTabBarMetrics {
    static let maxWidth: CGFloat = 300
    static let height: CGFloat = 56
    static let bottomMargin: CGFloat = 49
    static let outerPadding: CGFloat = 32
    static let innerPadding: CGFloat = 8
    static let indicatorSize: CGFloat = 40
    static let iconSize: CGFloat = 22
}

private extension View {
    /// Liquid Glass arrived in iOS 26 and the app deploys to iOS 16, so older systems take the
    /// closest material the SDK offers them.
    @ViewBuilder
    func rootTabBarSurface() -> some View {
        if #available(iOS 26.0, *) {
            glassEffect(.regular, in: .capsule)
        } else {
            background(.ultraThinMaterial, in: Capsule())
        }
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
