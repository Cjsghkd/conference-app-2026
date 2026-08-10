// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_app-ios-kotlin",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "_app-ios-kotlin",
      type: .none,
      targets: ["_app-ios-kotlin"]
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
      name: "_app-ios-kotlin",
      dependencies: [
        .product(
          name: "FirebaseCrashlytics",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
