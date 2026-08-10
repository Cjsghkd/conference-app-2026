import AppShared
import SwiftUI

/// The native root tab bar, laid out over the Compose view controller. The view occupies the bar's
/// own area and nothing more, so every point outside it belongs to the Compose layer below.
struct RootTabBarView: View {
    let navigator: RootTabNavigator

    @Namespace private var indicator
    @State private var currentTab: RootTab?

    var body: some View {
        Group {
            if let currentTab {
                HStack(spacing: 0) {
                    ForEach(RootTab.entries, id: \.self) { tab in
                        RootTabButton(
                            tab: tab,
                            isSelected: tab == currentTab,
                            indicator: indicator,
                            select: { navigator.select(tab: tab) }
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
            let collector = RootTabCollector { currentTab = $0 }
            try? await navigator.currentTab.collect(collector: collector)
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

private final class RootTabCollector: NSObject, Kotlinx_coroutines_coreFlowCollector {
    private let onEmit: (RootTab?) -> Void

    init(onEmit: @escaping (RootTab?) -> Void) {
        self.onEmit = onEmit
    }

    func emit(value: Any?) async throws {
        let tab = value as? RootTab
        await MainActor.run { onEmit(tab) }
    }
}

private extension RootTab {
    /// The Compose bar names each destination with a Material `ImageVector`, which does not cross
    /// the framework boundary; the SF Symbol standing for the same destination lives here.
    var symbolName: String {
        switch self {
        case .timetable: return "calendar"
        case .eventmap: return "map"
        case .favorites: return "heart.fill"
        case .about: return "info.circle"
        case .profilecard: return "person.crop.circle"
        default: return "questionmark"
        }
    }
}
