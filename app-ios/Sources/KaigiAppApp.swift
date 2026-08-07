import AppShared
import SwiftUI

@main
struct KaigiAppApp: App {
    private let appGraph = IosAppGraphKt.createIosAppGraph()

    init() {
        appGraph.appInitializer.initialize()
    }

    var body: some Scene {
        WindowGroup {
            KaigiAppView(appGraph: appGraph).ignoresSafeArea()
        }
    }
}

private struct KaigiAppView: UIViewControllerRepresentable {
    let appGraph: IosAppGraph

    func makeUIViewController(context: Context) -> UIViewController {
        KaigiAppViewController_iosKt.kaigiAppViewController(appGraph: appGraph)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
