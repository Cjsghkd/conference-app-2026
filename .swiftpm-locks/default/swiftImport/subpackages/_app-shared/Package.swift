// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_app-shared",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "_app-shared",
      type: .none,
      targets: ["_app-shared"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/firebase/firebase-ios-sdk",
      from: "12.0.0"
    )
  ],
  targets: [
    .target(
      name: "_app-shared",
      dependencies: [
        .product(
          name: "FirebaseCrashlytics",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
