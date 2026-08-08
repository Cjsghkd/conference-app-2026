import AppShared
import SwiftUI

@main
struct KaigiAppApp: App {
    private let appGraph = IosAppGraphKt.createIosAppGraph(
        swiftPackageLicensesJson: swiftPackageLicensesJson()
    )

    init() {
        appGraph.appInitializer.initialize()
    }

    var body: some Scene {
        WindowGroup {
            KaigiAppView(appGraph: appGraph).ignoresSafeArea()
        }
    }
}

/// The export written into the bundle by `scripts/generate-swift-package-licenses.py`. An empty
/// string leaves the licenses screen with the Kotlin dependencies alone, which is what a build
/// missing that phase would show.
private func swiftPackageLicensesJson() -> String {
    guard let url = Bundle.main.url(forResource: "swift-package-licenses", withExtension: "json"),
          let json = try? String(contentsOf: url, encoding: .utf8)
    else {
        return ""
    }
    return json
}

private struct KaigiAppView: UIViewControllerRepresentable {
    let appGraph: IosAppGraph

    func makeUIViewController(context: Context) -> UIViewController {
        KaigiAppViewController_iosKt.kaigiAppViewController(appGraph: appGraph)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
