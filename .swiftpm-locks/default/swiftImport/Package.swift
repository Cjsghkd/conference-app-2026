// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/_app-ios-kotlin")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_app-ios-kotlin", package: "_app-ios-kotlin")
      ]
    )
  ]
)
