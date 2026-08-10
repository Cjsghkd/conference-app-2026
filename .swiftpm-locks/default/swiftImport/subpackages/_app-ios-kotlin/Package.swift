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
  ],
  targets: [
    .target(
      name: "_app-ios-kotlin",
      dependencies: [
      ]
    )
  ]
)
