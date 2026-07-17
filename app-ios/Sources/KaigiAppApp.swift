import AppShared
import SwiftUI

@main
struct KaigiAppApp: App {
    var body: some Scene {
        WindowGroup {
            KaigiAppView().ignoresSafeArea()
        }
    }
}

private struct KaigiAppView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        KaigiAppViewController_iosKt.kaigiAppViewController(appGraph: IosAppGraphKt.createIosAppGraph())
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
