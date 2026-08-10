// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_app_shared",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "_app_shared",
      type: .none,
      targets: ["_app_shared"]
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
      name: "_app_shared",
      dependencies: [
        .product(
          name: "FirebaseCrashlytics",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
