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
    .package(path: "subpackages/_app-ios-kotlin"),
    .package(path: "subpackages/_app-shared"),
    .package(path: "subpackages/_app_shared")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_app-ios-kotlin", package: "_app-ios-kotlin"),
        .product(name: "_app-shared", package: "_app-shared"),
        .product(name: "_app_shared", package: "_app_shared")
      ]
    )
  ]
)
