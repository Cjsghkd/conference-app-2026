// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackageDylib",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackageDylib",
      type: .dynamic,
      targets: ["KotlinMultiplatformLinkedPackageDylib"]
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
      name: "KotlinMultiplatformLinkedPackageDylib",
      dependencies: [
        .product(
          name: "FirebaseCrashlytics",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
